package com.habitora.backend.service.interfaces;

import com.habitora.backend.presentation.dto.auth.request.LoginRequest;
import com.habitora.backend.presentation.dto.auth.request.RegisterRequest;

import jakarta.servlet.http.HttpServletResponse;

public interface IAuthService {

    void register(RegisterRequest request, HttpServletResponse response);

    void login(LoginRequest request, HttpServletResponse response);

    void logout(HttpServletResponse response);

}
