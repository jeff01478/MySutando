package com.john.mysutando.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.john.mysutando.enums.GuildFeature;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireFeature {
    GuildFeature value();
    String guildIdParam() default "guildId";
}
