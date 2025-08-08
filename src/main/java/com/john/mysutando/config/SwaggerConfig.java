package com.john.mysutando.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI swagger() {
        return new OpenAPI()
            .info(new Info().title("DC鴨肉羹替身Application API")
                .description("")
                .version("v1")
                .license(new License().name("Apache").url("http://springdoc.org")));
    }
}
