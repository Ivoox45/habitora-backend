package com.habitora.backend.integration.whatsapp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Servicio de integración con WhatsApp Business API (Meta/Facebook).
 * Permite enviar mensajes de texto a través de la API de WhatsApp Cloud.
 * Solo se activa si whatsapp.enabled=true
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "whatsapp.enabled", havingValue = "true", matchIfMissing = false)
public class WhatsAppService {

    @Value("${whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.access-token}")
    private String accessToken;

    @Value("${whatsapp.api-version}")
    private String apiVersion;

    @Value("${whatsapp.api-base-url}")
    private String apiBaseUrl;

    private RestClient restClient;

    /**
     * Inicializa el RestClient después de que se inyecten los valores.
     */
    private RestClient getRestClient() {
        if (restClient == null) {
            restClient = RestClient.builder()
                    .baseUrl(apiBaseUrl + "/" + apiVersion)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
        }
        return restClient;
    }

    /**
     * Envía un mensaje de texto por WhatsApp a un número de teléfono.
     *
     * @param telefonoDestino Número de teléfono en formato internacional (ej: 51987654321)
     * @param mensaje         Contenido del mensaje a enviar
     * @return ID del mensaje enviado según la API de WhatsApp, o null si falla
     */
    public String enviarMensaje(String telefonoDestino, String mensaje) {
        try {
            // Construir el payload según la documentación de WhatsApp Business API
            Map<String, Object> payload = Map.of(
                    "messaging_product", "whatsapp",
                    "recipient_type", "individual",
                    "to", telefonoDestino,
                    "type", "text",
                    "text", Map.of("body", mensaje)
            );

            log.info("Enviando mensaje de WhatsApp a: {} | Longitud: {} caracteres", telefonoDestino, mensaje.length());

            // Realizar la petición POST a la API
            WhatsAppResponse response = getRestClient()
                    .post()
                    .uri("/{phoneNumberId}/messages", phoneNumberId)
                    .body(payload)
                    .retrieve()
                    .body(WhatsAppResponse.class);

            if (response != null && response.messages() != null && !response.messages().isEmpty()) {
                String messageId = response.messages().get(0).id();
                log.info("✅ Mensaje enviado exitosamente. ID: {}", messageId);
                return messageId;
            }

            log.warn("⚠️ No se recibió ID de mensaje en la respuesta de WhatsApp");
            return null;

        } catch (Exception e) {
            log.error("❌ Error al enviar mensaje de WhatsApp a {}: {}", telefonoDestino, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Formatea un número de teléfono peruano (9 dígitos) al formato internacional.
     *
     * @param numeroLocal Número local de 9 dígitos (ej: 987654321)
     * @return Número en formato internacional (ej: 51987654321)
     */
    public String formatearNumeroPeruano(String numeroLocal) {
        if (numeroLocal == null || numeroLocal.isBlank()) {
            return null;
        }
        
        String numero = numeroLocal.trim();
        
        // Si ya tiene código de país, retornarlo tal cual
        if (numero.startsWith("51") && numero.length() == 11) {
            return numero;
        }
        
        // Si es número local de 9 dígitos, agregar código de Perú (+51)
        if (numero.length() == 9 && numero.matches("\\d+")) {
            return "51" + numero;
        }
        
        log.warn("⚠️ Formato de teléfono no reconocido: {}", numero);
        return numero;
    }

    /**
     * DTO para la respuesta de la API de WhatsApp.
     */
    private record WhatsAppResponse(
            java.util.List<MessageInfo> messages,
            String messaging_product,
            ContactInfo[] contacts
    ) {}

    private record MessageInfo(String id) {}

    private record ContactInfo(String wa_id, String input) {}
}
