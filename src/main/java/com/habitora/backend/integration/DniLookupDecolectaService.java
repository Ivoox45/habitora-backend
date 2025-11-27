package com.habitora.backend.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.annotation.JsonProperty;

@Service
@Slf4j
public class DniLookupDecolectaService {

    @Value("${external.decolecta.token:}")
    private String token;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.decolecta.com")
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();

    public String getNombrePorDni(String dni) {
        if (dni == null || dni.isBlank()) {
            log.warn("DNI vacío o nulo");
            return null;
        }
        
        try {
            log.info("Consultando DNI: {} en Decolecta", dni);
            
            var requestSpec = restClient.get()
                    .uri(uri -> uri.path("/v1/reniec/dni").queryParam("numero", dni).build());
            
            // Agregar token si está configurado
            if (token != null && !token.isBlank()) {
                requestSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            }
            
            var response = requestSpec.retrieve().toEntity(DecolectaDniResponse.class);
            var body = response.getBody();
            
            if (body == null) {
                log.warn("Respuesta vacía de Decolecta para DNI: {}", dni);
                return null;
            }
            
            log.info("Respuesta recibida: nombres={}, apellidoPaterno={}, apellidoMaterno={}", 
                body.getNombres(), body.getApellidoPaterno(), body.getApellidoMaterno());
            
            String full = (n(body.getNombres()) + " " + n(body.getApellidoPaterno()) + " " + n(body.getApellidoMaterno())).trim();
            
            if (full.isBlank()) {
                log.warn("Nombre completo vacío después de concatenar para DNI: {}", dni);
                return null;
            }
            
            log.info("Nombre completo encontrado: {}", full);
            return full;
            
        } catch (Exception e) {
            log.error("Error al consultar DNI {} en Decolecta: {}", dni, e.getMessage(), e);
            return null;
        }
    }

    private static String n(String s) { return s == null ? "" : s; }

    public static class DecolectaDniResponse {
        @JsonProperty("numero")
        private String numero;
        
        @JsonProperty("nombres")
        private String nombres;
        
        @JsonProperty("apellido_paterno")
        private String apellidoPaterno;
        
        @JsonProperty("apellido_materno")
        private String apellidoMaterno;

        public String getNumero() { return numero; }
        public String getNombres() { return nombres; }
        public String getApellidoPaterno() { return apellidoPaterno; }
        public String getApellidoMaterno() { return apellidoMaterno; }

        public void setNumero(String v) { this.numero = v; }
        public void setNombres(String v) { this.nombres = v; }
        public void setApellidoPaterno(String v) { this.apellidoPaterno = v; }
        public void setApellidoMaterno(String v) { this.apellidoMaterno = v; }
    }
}