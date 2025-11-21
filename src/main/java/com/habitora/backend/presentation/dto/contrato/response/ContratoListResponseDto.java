package com.habitora.backend.presentation.dto.contrato.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class ContratoListResponseDto {

    private Long id;

    private String estado;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private BigDecimal montoDeposito;

    private Long inquilinoId;
    private String inquilinoNombre;
    private String inquilinoDni;

    private Long habitacionId;
    private String habitacionCodigo;
}
