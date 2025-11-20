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
                    // Dominio final real
                    "https://habitora.app",

                    // Backend Railway (importante!)
                    "https://habitora-backend-production.up.railway.app",
                    "https://habitora-backend-development.up.railway.app",

                    // Swagger UI si se carga desde Railway
                    "http://habitora-backend-production.up.railway.app",
                    "http://habitora-backend-development.up.railway.app"
            };

        } else {

            // Modo desarrollo
            allowedOrigins = new String[]{
                    "http://localhost:5173",          // Vite
                    "http://127.0.0.1:5173",
                    "http://localhost:8080",          // Swagger local
                    "http://127.0.0.1:8080"
            };
        }

        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(allowedOrigins)
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}
