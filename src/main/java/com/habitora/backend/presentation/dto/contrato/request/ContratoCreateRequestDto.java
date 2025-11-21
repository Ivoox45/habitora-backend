package com.habitora.backend.presentation.dto.contrato.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class ContratoCreateRequestDto {

    @NotNull
    private Long inquilinoId;

    @NotNull
    private Long habitacionId;

    @NotNull
    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    @NotNull
    @PositiveOrZero
    private BigDecimal montoDeposito;
}
