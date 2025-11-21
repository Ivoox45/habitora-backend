package com.habitora.backend.presentation.dto.inquilino.response;

import lombok.Data;

@Data
public class InquilinoResponseDto {

    private Long id;
    private String nombreCompleto;
    private String numeroDni;
    private String email;
    private String telefonoWhatsapp;
    private int cantidadContratos;
}
