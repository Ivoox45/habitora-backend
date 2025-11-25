package com.habitora.backend.service.implementation;

import com.habitora.backend.configuration.security.CookieUtil;
import com.habitora.backend.persistence.entity.Usuario;
import com.habitora.backend.persistence.repository.UsuarioRepository;
import com.habitora.backend.presentation.dto.auth.request.LoginRequest;
import com.habitora.backend.presentation.dto.auth.request.RegisterRequest;
import com.habitora.backend.exception.BusinessException;
import com.habitora.backend.service.interfaces.IAuthService;
import com.habitora.backend.service.interfaces.IJwtService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements IAuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final IJwtService jwtService;
    private final CookieUtil cookieUtil;

    // Segundos
    private static final int ACCESS_TOKEN_EXP = 900; // 15 min
    private static final int REFRESH_TOKEN_EXP = 2_592_000; // 30 días

    @Override
    public void register(RegisterRequest request, HttpServletResponse response) {
        log.info("Registrando nuevo usuario: {}", request.getEmail());

        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Ya existe un usuario con ese email.");
        }

        Usuario usuario = Usuario.builder()
                .nombreCompleto(request.getNombreCompleto())
                .email(request.getEmail())
                .telefonoWhatsapp(request.getTelefonoWhatsapp())
                .contrasena(passwordEncoder.encode(request.getPassword()))
                .build();

        usuarioRepository.save(usuario);
        log.info("Usuario registrado exitosamente: {}", usuario.getEmail());

        performLogin(usuario, response);
    }

    @Override
    public void login(LoginRequest request, HttpServletResponse response) {
        log.info("Intento de login para: {}", request.getEmail());

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Email o contraseña incorrectos."));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getContrasena())) {
            throw new BusinessException("Email o contraseña incorrectos.");
        }

        performLogin(usuario, response);
        log.info("Login exitoso para: {}", usuario.getEmail());
    }

    private void performLogin(Usuario usuario, HttpServletResponse response) {

        String accessToken = jwtService.generateAccessToken(usuario);
        String refreshToken = jwtService.generateRefreshToken(usuario);

        cookieUtil.addCookie(response, "access_token", accessToken, ACCESS_TOKEN_EXP);
        cookieUtil.addCookie(response, "refresh_token", refreshToken, REFRESH_TOKEN_EXP);
    }

    @Override
    public void logout(HttpServletResponse response) {

        cookieUtil.deleteCookie(response, "access_token");
        cookieUtil.deleteCookie(response, "refresh_token");
    }
}
