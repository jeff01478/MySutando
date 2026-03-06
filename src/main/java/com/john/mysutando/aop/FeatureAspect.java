package com.john.mysutando.aop;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import com.john.mysutando.annotation.RequireFeature;
import com.john.mysutando.config.GuildFeatureConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class FeatureAspect {
    private final GuildFeatureConfig guildFeatureConfig;

    @Before("@annotation(com.john.mysutando.annotation.RequireFeature)")
    public void checkFeature(JoinPoint joinPoint) {
        log.info("start checkFeature");
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireFeature annotation = method.getAnnotation(RequireFeature.class);

        String guildId = resolveGuildId(joinPoint, annotation.guildIdParam());

        if (guildId == null) {
            log.warn("方法 {} 標記了 @RequireFeature 但找不到 guildId", method.getName());
            throw new IllegalArgumentException("系統設定錯誤：找不到 Guild ID");
        }

        if (!guildFeatureConfig.hasFeature(guildId, annotation.value())) {
            throw new IllegalArgumentException("伺服器無法使用此功能: " + annotation.value());
        }
    }

    private String resolveGuildId(JoinPoint joinPoint, String targetParamName) {
        Object[] args = joinPoint.getArgs();
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();

        // 優先從 JDA 物件找 guildId
        for (Object arg : args) {
            if (arg instanceof Member member) {
                return member.getGuild().getId();
            } else if (arg instanceof TextChannel channel) {
                return channel.getGuild().getId();
            } else if (arg instanceof Guild guild) {
                return guild.getId();
            }
        }

        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equals(targetParamName) && args[i] instanceof String guildId) {
                return guildId;
            }
        }

        return null;
    }
}
