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

        String[] allowedOrigins;

        if (environment.equalsIgnoreCase("prod")) {

            allowedOrigins = new String[]{
                    "https://habitora.app",

                    // Railway backend
                    "https://habitora-backend-develpment.up.railway.app",
                    "http://habitora-backend-develpment.up.railway.app"
            };

        } else {

            // 🔥 Modo desarrollo (para ti y tu amigo)
            allowedOrigins = new String[]{
                    "http://localhost:5173",
                    "http://localhost:*",
                    "http://127.0.0.1:5173",
                    "http://127.0.0.1:*",
                    "http://localhost:8080",
                    "http://127.0.0.1:8080"
            };
        }

        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(allowedOrigins)
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}
