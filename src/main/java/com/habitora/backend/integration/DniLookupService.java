package com.habitora.backend.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class DniLookupService {

    @Value("${external.apisnetpe.token:}")
    private String token;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.decolecta.com")
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();

    public String getNombrePorDni(String dni) {
        if (dni == null || dni.isBlank()) {
            log.warn("DNI is null or blank");
            return null;
        }
        if (token == null || token.isBlank()) {
            log.error("Token is null or blank. Check application.properties: external.apisnetpe.token");
            return null;
        }

        try {
            log.info("Calling API for DNI: {}", dni);
            // Endpoint: /v1/reniec/dni?numero=<dni>
            // Header: Authorization: Bearer <token>
            var response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/v1/reniec/dni").queryParam("numero", dni).build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .toEntity(DniResponse.class);

            var body = response.getBody();
            log.info("API Response body: {}", body);
            if (body == null) return null;
            // API returns: first_name, first_last_name, second_last_name, full_name
            // Use full_name directly or compose: "first_name first_last_name second_last_name"
            String fullName = body.getFullName();
            log.info("Full name from API: {}", fullName);
            if (fullName != null && !fullName.isBlank()) return fullName;
            
            // Fallback: compose from parts
            String firstName = nullToEmpty(body.getFirstName());
            String firstLastName = nullToEmpty(body.getFirstLastName());
            String secondLastName = nullToEmpty(body.getSecondLastName());
            String composed = (firstName + " " + firstLastName + " " + secondLastName).trim();
            log.info("Composed name: {}", composed);
            return composed.isBlank() ? null : composed;
        } catch (Exception e) {
            log.error("Error calling DNI lookup API for DNI: {}", dni, e);
            return null;
        }
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }

    public static class DniResponse {
        @com.fasterxml.jackson.annotation.JsonProperty("first_name")
        private String firstName;
        
        @com.fasterxml.jackson.annotation.JsonProperty("first_last_name")
        private String firstLastName;
        
        @com.fasterxml.jackson.annotation.JsonProperty("second_last_name")
        private String secondLastName;
        
        @com.fasterxml.jackson.annotation.JsonProperty("full_name")
        private String fullName;
        
        @com.fasterxml.jackson.annotation.JsonProperty("document_number")
        private String documentNumber;

        public String getFirstName() { return firstName; }
        public String getFirstLastName() { return firstLastName; }
        public String getSecondLastName() { return secondLastName; }
        public String getFullName() { return fullName; }
        public String getDocumentNumber() { return documentNumber; }

        public void setFirstName(String v) { this.firstName = v; }
        public void setFirstLastName(String v) { this.firstLastName = v; }
        public void setSecondLastName(String v) { this.secondLastName = v; }
        public void setFullName(String v) { this.fullName = v; }
        public void setDocumentNumber(String v) { this.documentNumber = v; }
    }
}