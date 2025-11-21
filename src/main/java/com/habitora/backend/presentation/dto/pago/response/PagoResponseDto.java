package com.habitora.backend.presentation.dto.pago.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PagoResponseDto {

    private Long id;
    private Long facturaId;
    private Long contratoId;

    private LocalDate fechaPago;
    private BigDecimal monto;
    private String metodo;
}
