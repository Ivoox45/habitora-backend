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
    private final com.habitora.backend.service.interfaces.IRefreshTokenService refreshTokenService;

    // Segundos
    private static final int REFRESH_TOKEN_EXP = 2_592_000; // 30 días

    @Override
    public String register(RegisterRequest request, HttpServletResponse response) {
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

        return performLogin(usuario, response);
    }

    @Override
    public String login(LoginRequest request, HttpServletResponse response) {
        log.info("Intento de login para: {}", request.getEmail());

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Email o contraseña incorrectos."));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getContrasena())) {
            throw new BusinessException("Email o contraseña incorrectos.");
        }

        String token = performLogin(usuario, response);
        log.info("Login exitoso para: {}", usuario.getEmail());
        return token;
    }

    private String performLogin(Usuario usuario, HttpServletResponse response) {

        String accessToken = jwtService.generateAccessToken(usuario);
        // Create opaque refresh token and persist hash
        String refreshToken = refreshTokenService.createRefreshToken(usuario, REFRESH_TOKEN_EXP);

        // Store refresh token in HttpOnly cookie. Access token is returned in body.
        cookieUtil.addCookie(response, "refresh_token", refreshToken, REFRESH_TOKEN_EXP);

        return accessToken;
    }

    @Override
    public void logout(jakarta.servlet.http.HttpServletRequest request, HttpServletResponse response) {

        // Try to remove only the refresh token corresponding to this session
        String refreshToken = cookieUtil.getCookieValue(request, "refresh_token");
        if (refreshToken != null) {
            try {
                refreshTokenService.deleteByRawToken(refreshToken);
            } catch (Exception e) {
                log.warn("Error deleting refresh token on logout: {}", e.getMessage());
            }
        }

        // Remove cookies client-side
        cookieUtil.deleteCookie(response, "access_token");
        cookieUtil.deleteCookie(response, "refresh_token");
    }

    @Override
    public String refresh(jakarta.servlet.http.HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = cookieUtil.getCookieValue(request, "refresh_token");

        if (refreshToken == null) {
            throw new com.habitora.backend.exception.UnauthorizedException("Refresh token not present");
        }

        // Validate and rotate the specific refresh token used (individual session rotation)
        String newRefreshToken = refreshTokenService.validateAndRotate(refreshToken, REFRESH_TOKEN_EXP);
        if (newRefreshToken == null) {
            throw new com.habitora.backend.exception.UnauthorizedException("Refresh token invalid or expired");
        }

        // update cookie with the new refresh token
        Usuario usuario = refreshTokenService.validateRefreshToken(newRefreshToken);
        cookieUtil.addCookie(response, "refresh_token", newRefreshToken, REFRESH_TOKEN_EXP);

        String newAccessToken = jwtService.generateAccessToken(usuario);
        return newAccessToken;
    }
}
