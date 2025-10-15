package com.habitora.backend.presentation.dto.usuario.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioCreateRequestDto {

    @NotBlank(message = "El nombre completo es obligatorio.")
    @Size(max = 120)
    private String nombreCompleto;

    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email
    private String email;

    @Size(max = 40)
    private String telefonoWhatsapp;

    @NotBlank(message = "La contraseña es obligatoria.")
    @Size(min = 8, max = 255)
    private String contraseña;

}
