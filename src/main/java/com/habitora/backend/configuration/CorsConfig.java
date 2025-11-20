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

            originPatterns = new String[] {
                    // Frontend producción (Vercel)
                    "https://habitora-frontend.vercel.app",

                    // Opcional — dominio propio futuro
                    "https://habitora.app",

                    // Backend Railway (por si algún cliente externo hace consultas)
                    "https://habitora-backend-production.up.railway.app",
                    "http://habitora-backend-production.up.railway.app"
            };

        } else {
            // Desarrollo local
            originPatterns = new String[] {
                    "http://localhost:*",
                    "http://127.0.0.1:*",

                    // Para probar también contra el frontend de Vercel si quieres
                    "https://habitora-frontend.vercel.app",
                    "https://habitora.app",
                    "https://habitora-backend-production.up.railway.app",
                    "http://habitora-backend-production.up.railway.app"
            };
        }

        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns(originPatterns)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}
