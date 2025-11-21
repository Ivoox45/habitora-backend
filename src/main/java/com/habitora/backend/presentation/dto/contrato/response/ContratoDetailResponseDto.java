package com.habitora.backend.presentation.dto.contrato.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class ContratoDetailResponseDto {

    private Long id;
    private Long propiedadId;

    private String estado;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private BigDecimal montoDeposito;

    private Long inquilinoId;
    private String inquilinoNombre;
    private String inquilinoDni;
    private String inquilinoEmail;
    private String inquilinoTelefono;

    private Long habitacionId;
    private String habitacionCodigo;
    private String habitacionEstado;
    private String habitacionPrecioRenta;

    private boolean tieneFirma;
}
