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
    private BigDecimal totalAPagar;

    private String estado; // ABIERTA, PAGADA, VENCIDA, CANCELADA
}
