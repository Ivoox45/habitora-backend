package com.habitora.backend.presentation.dto.factura.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FacturaResponseDto {

    private Long id;

    private Long contratoId;
    private Long inquilinoId;
    private String inquilinoNombre;

    private Long habitacionId;
    private String habitacionCodigo;

    private LocalDate periodoInicio;
    private LocalDate periodoFin;
    private LocalDate fechaVencimiento;

    private BigDecimal montoRenta;

    private String estado; // ABIERTA, PAGADA, VENCIDA, CANCELADA

    // 🚀 Nuevos campos para el frontend (flags)
    private boolean esPagada;
    private boolean esVencida;
    private int diasRetraso; // 0 si no aplica
}
