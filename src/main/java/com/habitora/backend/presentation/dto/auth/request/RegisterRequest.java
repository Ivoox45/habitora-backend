package com.habitora.backend.presentation.dto.auth.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String nombreCompleto;
    private String email;
    private String telefonoWhatsapp;
    private String password;
}