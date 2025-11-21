package com.habitora.backend.presentation.dto.inquilino.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InquilinoCreateRequestDto {

    @NotBlank(message = "El nombre completo es obligatorio.")
    @Size(max = 140, message = "El nombre completo no puede exceder los 140 caracteres.")
    private String nombreCompleto;

    @NotBlank(message = "El DNI es obligatorio.")
    @Size(max = 20, message = "El DNI no puede exceder los 20 caracteres.")
    private String numeroDni;

    @Email(message = "El correo electrónico no tiene un formato válido.")
    @Size(max = 160, message = "El correo electrónico no puede exceder los 160 caracteres.")
    private String email;

    @Size(max = 40, message = "El número de WhatsApp no puede exceder los 40 caracteres.")
    private String telefonoWhatsapp;
}
