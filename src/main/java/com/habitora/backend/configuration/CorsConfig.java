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

                    // Dominio final (cuando lo tengas)
                    "https://habitora.app",

                    // ⚠️ TU BACKEND REAL DE RAILWAY (con el error de escritura)
                    "https://habitora-backend-develpment.up.railway.app",

                    // Swagger UI de Railway (por si lo usa)
                    "http://habitora-backend-develpment.up.railway.app"
            };

        } else {

            // --- MODO DESARROLLO ---
            allowedOrigins = new String[]{
                    "http://localhost:5173",
                    "http://127.0.0.1:5173",
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
