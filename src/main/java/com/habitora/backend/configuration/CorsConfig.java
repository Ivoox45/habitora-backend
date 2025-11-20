package com.habitora.backend.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Value("${app.env:local}")
    private String environment;

    @Bean
    public WebMvcConfigurer corsConfigurer() {

        final String[] originPatterns;

        if (environment.equalsIgnoreCase("prod")) {

            originPatterns = new String[]{
                    // Dominio del frontend (cuando exista)
                    "https://habitora.app",

                    // Dominio REAL del backend
                    "https://habitora-backend-production.up.railway.app",
                    "http://habitora-backend-production.up.railway.app"
            };

        } else {

            // Modo desarrollo (tú y tu amigo)
            originPatterns = new String[]{
                    "http://localhost:*",
                    "http://127.0.0.1:*"
            };
        }

        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns(originPatterns)
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}
