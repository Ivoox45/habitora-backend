package com.habitora.backend.presentation.dto.pago.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PagoResponseDto {

    private Long id;

    private Long facturaId;
    private Long contratoId;

    private Long inquilinoId;
    private String inquilinoNombre;

    private Long habitacionId;
    private String habitacionCodigo;

    private String mes; // <-- Ej: "Junio 2025"

    private LocalDate fechaPago;
    private BigDecimal monto;
    private String metodo;

    private String estado; // Siempre "Completado" por ahora
}
