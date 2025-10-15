package com.habitora.backend.presentation.dto.usuario.response;

import lombok.Data;

@Data
public class UsuarioResponseDto {

    private Long id;
    private String nombreCompleto;
    private String email;
    private String telefonoWhatsapp;

}
