package com.habitora.backend.presentation.dto.usuario.request;

import com.habitora.backend.presentation.validation.OnlyLetters;
import com.habitora.backend.presentation.validation.PeruvianPhone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioCreateRequestDto {

    @NotBlank(message = "El nombre completo es obligatorio.")
    @OnlyLetters(message = "El nombre solo puede contener letras, espacios y tildes.")
    @Size(max = 120)
    private String nombreCompleto;

    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email
    private String email;

    @PeruvianPhone(message = "El teléfono debe tener exactamente 9 dígitos (sin código de país).")
    @Size(max = 9)
    private String telefonoWhatsapp;

    @NotBlank(message = "La contraseña es obligatoria.")
    @Size(min = 8, max = 255)
    private String contrasena;

}
