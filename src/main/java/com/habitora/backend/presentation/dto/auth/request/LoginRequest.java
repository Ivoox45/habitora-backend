package com.habitora.backend.presentation.dto.auth.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}