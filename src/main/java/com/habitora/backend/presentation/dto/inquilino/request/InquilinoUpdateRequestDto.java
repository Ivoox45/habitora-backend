package com.habitora.backend.presentation.dto.inquilino.request;

import com.habitora.backend.presentation.validation.OnlyLetters;
import com.habitora.backend.presentation.validation.PeruvianDni;
import com.habitora.backend.presentation.validation.PeruvianPhone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InquilinoUpdateRequestDto {

    @NotBlank(message = "El nombre completo es obligatorio.")
    @OnlyLetters(message = "El nombre solo puede contener letras, espacios y tildes.")
    @Size(max = 140, message = "El nombre completo no puede exceder los 140 caracteres.")
    private String nombreCompleto;

    @NotBlank(message = "El DNI es obligatorio.")
    @PeruvianDni(message = "El DNI debe tener exactamente 8 dígitos numéricos.")
    @Size(max = 8, message = "El DNI debe tener exactamente 8 dígitos.")
    private String numeroDni;

    @Email(message = "El correo electrónico no tiene un formato válido.")
    @Size(max = 160, message = "El correo electrónico no puede exceder los 160 caracteres.")
    private String email;

    @PeruvianPhone(message = "El teléfono debe tener exactamente 9 dígitos (sin código de país).")
    @Size(max = 9, message = "El teléfono debe tener máximo 9 dígitos.")
    private String telefonoWhatsapp;
}
