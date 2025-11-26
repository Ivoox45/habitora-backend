package com.habitora.backend.service.interfaces;

import com.habitora.backend.presentation.dto.auth.request.LoginRequest;
import com.habitora.backend.presentation.dto.auth.request.RegisterRequest;

import jakarta.servlet.http.HttpServletResponse;

public interface IAuthService {

    String register(RegisterRequest request, HttpServletResponse response);

    String login(LoginRequest request, HttpServletResponse response);

    void logout(jakarta.servlet.http.HttpServletRequest request, HttpServletResponse response);

    String refresh(jakarta.servlet.http.HttpServletRequest request, HttpServletResponse response);

}
