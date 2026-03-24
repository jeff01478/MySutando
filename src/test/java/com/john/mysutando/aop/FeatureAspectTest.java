package com.john.mysutando.aop;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.john.mysutando.annotation.RequireFeature;
import com.john.mysutando.config.GuildFeatureConfig;
import com.john.mysutando.enums.GuildFeature;

import net.dv8tion.jda.api.entities.Guild;

class FeatureAspectTest {

    private GuildFeatureConfig guildFeatureConfig;
    private FeatureAspect featureAspect;

    @BeforeEach
    void setUp() {
        guildFeatureConfig = mock(GuildFeatureConfig.class);
        featureAspect = new FeatureAspect(guildFeatureConfig);
    }

    @Test
    // 驗證當 guild 已開啟指定功能時，AOP 不會阻擋方法執行。
    void checkFeature_allowsCallWhenGuildFeatureEnabled() throws NoSuchMethodException {
        Guild guild = mock(Guild.class);
        when(guild.getId()).thenReturn("guild-1");
        when(guildFeatureConfig.hasFeature("guild-1", GuildFeature.RECORD_MESSAGE)).thenReturn(true);

        JoinPoint joinPoint = buildJoinPoint(
            DummyTarget.class.getDeclaredMethod("recordGuild", Guild.class),
            new Object[]{guild}
        );

        assertDoesNotThrow(() -> featureAspect.checkFeature(joinPoint));
    }

    @Test
    // 驗證當 guild 未開啟指定功能時，AOP 會拋出功能停用錯誤。
    void checkFeature_rejectsCallWhenGuildFeatureDisabled() throws NoSuchMethodException {
        Guild guild = mock(Guild.class);
        when(guild.getId()).thenReturn("guild-1");
        when(guildFeatureConfig.hasFeature("guild-1", GuildFeature.RECORD_MESSAGE)).thenReturn(false);

        JoinPoint joinPoint = buildJoinPoint(
            DummyTarget.class.getDeclaredMethod("recordGuild", Guild.class),
            new Object[]{guild}
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> featureAspect.checkFeature(joinPoint)
        );

        assertEquals("伺服器無法使用此功能: RECORD_MESSAGE", exception.getMessage());
    }

    @Test
    // 驗證當方法參數中無法解析 guildId 時，AOP 會回報設定錯誤。
    void checkFeature_rejectsCallWhenGuildIdCannotBeResolved() throws NoSuchMethodException {
        JoinPoint joinPoint = buildJoinPoint(
            DummyTarget.class.getDeclaredMethod("recordWithNoGuild", String.class),
            new Object[]{"hello"}
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> featureAspect.checkFeature(joinPoint)
        );

        assertEquals("系統設定錯誤：找不到 Guild ID", exception.getMessage());
    }

    private JoinPoint buildJoinPoint(Method method, Object[] args) {
        JoinPoint joinPoint = mock(JoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(args);
        when(signature.getMethod()).thenReturn(method);
        when(signature.getParameterNames()).thenReturn(new String[]{"guild", "message"});

        return joinPoint;
    }

    static class DummyTarget {
        @RequireFeature(GuildFeature.RECORD_MESSAGE)
        void recordGuild(Guild guild) {
        }

        @RequireFeature(GuildFeature.RECORD_MESSAGE)
        void recordWithNoGuild(String message) {
        }
    }
}

