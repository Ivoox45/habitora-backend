package com.habitora.backend.configuration.app;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        // ----------- SECURITY SCHEME PARA BEARER TOKEN en header Authorization -----------
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", bearerScheme)
                )
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearerAuth")
                )
                .info(new Info()
                        .title("Habitora Backend API")
                        .version("v1")
                        .contact(new Contact()
                                .name("Habitora Team")
                                .email("soporte@habitora.com")
                        )
                );
    }
}
