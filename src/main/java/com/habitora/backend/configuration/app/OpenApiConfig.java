package com.habitora.backend.configuration.app;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Habitora Backend API")
                        .version("v1")
                        .contact(new Contact().name("Habitora Team").email("soporte@habitora.com"))
                );
    }

}
