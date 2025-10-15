package com.habitora.backend.presentation.dto.usuario.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioUpdateRequestDto {

    @Size(max = 120)
    private String nombreCompleto;

    @Email
    private String email;

    @Size(max = 40)
    private String telefonoWhatsapp;

    @Size(min = 8, max = 255)
    private String contraseña;

}
