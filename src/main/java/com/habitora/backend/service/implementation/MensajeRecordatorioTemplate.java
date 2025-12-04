package com.habitora.backend.service.implementation;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * Plantillas de mensajes para recordatorios de pago según días antes/después del vencimiento.
 */
public class MensajeRecordatorioTemplate {

    /**
     * Genera el mensaje apropiado según los días restantes hasta el vencimiento.
     *
     * @param diasRestantes Días hasta la fecha de vencimiento (negativo = antes, positivo = después)
     * @param nombreInquilino Nombre completo del inquilino
     * @param montoRenta Monto de la renta
     * @param habitacionCodigo Código de la habitación
     * @return Mensaje personalizado para WhatsApp
     */
    public static String generarMensaje(
            int diasRestantes,
            String nombreInquilino,
            BigDecimal montoRenta,
            String habitacionCodigo
    ) {
        String nombre = obtenerPrimerNombre(nombreInquilino);
        String monto = formatearMonto(montoRenta);

        return switch (diasRestantes) {
            case -3 -> mensajeTresDiasAntes(nombre, monto, habitacionCodigo);
            case -2 -> mensajeDosDiasAntes(nombre, monto, habitacionCodigo);
            case -1 -> mensajeUnDiaAntes(nombre, monto, habitacionCodigo);
            case 0 -> mensajeDiaVencimiento(nombre, monto, habitacionCodigo);
            case 1 -> mensajeUnDiaDespues(nombre, monto, habitacionCodigo);
            case 2 -> mensajeDosDiasDespues(nombre, monto, habitacionCodigo);
            default -> null; // No enviar mensaje para otros días
        };
    }

    // ========================================
    // MENSAJES ANTES DEL VENCIMIENTO
    // ========================================

    private static String mensajeTresDiasAntes(String nombre, String monto, String habitacion) {
        return String.format(
                "Hola %s, te recordamos que en *3 días* vence tu pago de renta 🏠\n\n" +
                        "📍 Habitación: %s\n" +
                        "💰 Monto: %s\n\n" +
                        "Por favor, realiza tu pago a tiempo para evitar inconvenientes.\n\n" +
                        "¡Gracias por tu puntualidad! 😊",
                nombre, habitacion, monto
        );
    }

    private static String mensajeDosDiasAntes(String nombre, String monto, String habitacion) {
        return String.format(
                "Hola %s, te recordamos que en *2 días* vence tu pago de renta 🏠\n\n" +
                        "📍 Habitación: %s\n" +
                        "💰 Monto: %s\n\n" +
                        "Te pedimos estar al día con tu pago para evitar inconvenientes.\n\n" +
                        "Cualquier consulta, estamos a tu disposición 📞",
                nombre, habitacion, monto
        );
    }

    private static String mensajeUnDiaAntes(String nombre, String monto, String habitacion) {
        return String.format(
                "Hola %s, te recordamos que *mañana* vence tu pago de renta 🏠\n\n" +
                        "📍 Habitación: %s\n" +
                        "💰 Monto: %s\n\n" +
                        "Por favor, realiza tu pago a tiempo para evitar cargos adicionales.\n\n" +
                        "¡Muchas gracias! 🙏",
                nombre, habitacion, monto
        );
    }

    // ========================================
    // MENSAJE DÍA DEL VENCIMIENTO
    // ========================================

    private static String mensajeDiaVencimiento(String nombre, String monto, String habitacion) {
        return String.format(
                "Hola %s, *hoy* es el último día para realizar tu pago de renta ⏰\n\n" +
                        "📍 Habitación: %s\n" +
                        "💰 Monto: %s\n\n" +
                        "Por favor, regulariza cuanto antes para mantener tu contrato al día.\n\n" +
                        "Agradecemos tu pronta respuesta 🙏",
                nombre, habitacion, monto
        );
    }

    // ========================================
    // MENSAJES DESPUÉS DEL VENCIMIENTO
    // ========================================

    private static String mensajeUnDiaDespues(String nombre, String monto, String habitacion) {
        return String.format(
                "Hola %s, tu pago de renta está *vencido* ⚠️\n\n" +
                        "📍 Habitación: %s\n" +
                        "💰 Monto: %s\n" +
                        "📅 Retraso: 1 día\n\n" +
                        "Por favor, regulariza lo antes posible para evitar cargos adicionales o medidas según el contrato.\n\n" +
                        "Esperamos tu pronta respuesta 📞",
                nombre, habitacion, monto
        );
    }

    private static String mensajeDosDiasDespues(String nombre, String monto, String habitacion) {
        return String.format(
                "Hola %s, tu pago de renta lleva *2 días de retraso* 🚨\n\n" +
                        "📍 Habitación: %s\n" +
                        "💰 Monto: %s\n" +
                        "📅 Retraso: 2 días\n\n" +
                        "Te solicitamos urgentemente ponerte al día. De no regularizar tu situación, " +
                        "podrías estar sujeto a desalojo según lo establecido en tu contrato.\n\n" +
                        "Por favor, comunícate con nosotros a la brevedad 📞⚠️",
                nombre, habitacion, monto
        );
    }

    // ========================================
    // MÉTODOS AUXILIARES
    // ========================================

    /**
     * Extrae el primer nombre del nombre completo.
     */
    private static String obtenerPrimerNombre(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            return "Inquilino";
        }
        String[] partes = nombreCompleto.trim().split("\\s+");
        return partes[0];
    }

    /**
     * Formatea el monto con símbolo de soles peruanos.
     */
    private static String formatearMonto(BigDecimal monto) {
        if (monto == null) {
            return "S/ 0.00";
        }
        return String.format("S/ %.2f", monto);
    }

    /**
     * Enum para identificar el tipo de recordatorio según días restantes.
     */
    @Getter
    public enum TipoRecordatorio {
        TRES_DIAS_ANTES(-3, "3 días antes del vencimiento"),
        DOS_DIAS_ANTES(-2, "2 días antes del vencimiento"),
        UN_DIA_ANTES(-1, "1 día antes del vencimiento"),
        DIA_VENCIMIENTO(0, "Día del vencimiento"),
        UN_DIA_DESPUES(1, "1 día después del vencimiento"),
        DOS_DIAS_DESPUES(2, "2 días después del vencimiento");

        private final int diasRestantes;
        private final String descripcion;

        TipoRecordatorio(int diasRestantes, String descripcion) {
            this.diasRestantes = diasRestantes;
            this.descripcion = descripcion;
        }
    }
}
