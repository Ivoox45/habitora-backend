package com.habitora.backend.service.implementation;

import com.habitora.backend.configuration.security.CookieUtil;
import com.habitora.backend.persistence.entity.Usuario;
import com.habitora.backend.persistence.repository.UsuarioRepository;
import com.habitora.backend.presentation.dto.auth.request.LoginRequest;
import com.habitora.backend.presentation.dto.auth.request.RegisterRequest;
import com.habitora.backend.exception.BusinessException;
import com.habitora.backend.service.interfaces.IAuthService;
import com.habitora.backend.service.interfaces.IJwtService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    // Leer desde application.properties (en milisegundos, convertir a segundos)
    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    private int getRefreshTokenExpSeconds() {
        return (int) (refreshTokenExpirationMs / 1000);
    }

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
        int expSeconds = getRefreshTokenExpSeconds();
        String refreshToken = refreshTokenService.createRefreshToken(usuario, expSeconds);

        // Store refresh token in HttpOnly cookie. Access token is returned in body.
        log.info("Creando cookie refresh_token para usuario: {} con maxAge: {} segundos", usuario.getEmail(), expSeconds);
        cookieUtil.addCookie(response, "refresh_token", refreshToken, expSeconds);

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

        // Log all cookies received
        Cookie[] cookies = request.getCookies();
        log.info("=== REFRESH ENDPOINT ===");
        log.info("Cookies recibidas: {}", cookies != null ? cookies.length : 0);
        if (cookies != null) {
            for (Cookie c : cookies) {
                log.info("Cookie: {} = {}", c.getName(), c.getValue().substring(0, Math.min(20, c.getValue().length())) + "...");
            }
        }

        String refreshToken = cookieUtil.getCookieValue(request, "refresh_token");
        log.info("Refresh token extraído: {}", refreshToken != null ? "SI" : "NO");

        if (refreshToken == null) {
            log.warn("Refresh token no encontrado en cookies");
            throw new com.habitora.backend.exception.UnauthorizedException("Refresh token not present");
        }

        // Primero validar el token actual y obtener el usuario
        Usuario usuario = refreshTokenService.validateRefreshToken(refreshToken);
        if (usuario == null) {
            log.warn("Refresh token inválido o expirado");
            throw new com.habitora.backend.exception.UnauthorizedException("Refresh token invalid or expired");
        }

        log.info("Usuario validado: {}", usuario.getId());

        // Rotar el token: eliminar el viejo y crear uno nuevo
        int expSeconds = getRefreshTokenExpSeconds();
        String newRefreshToken = refreshTokenService.validateAndRotate(refreshToken, expSeconds);
        if (newRefreshToken == null) {
            log.warn("Error al rotar el refresh token");
            throw new com.habitora.backend.exception.UnauthorizedException("Failed to rotate refresh token");
        }

        // Actualizar cookie con el nuevo refresh token
        cookieUtil.addCookie(response, "refresh_token", newRefreshToken, expSeconds);

        // Generar nuevo access token
        String newAccessToken = jwtService.generateAccessToken(usuario);
        log.info("Refresh exitoso para usuario: {}", usuario.getId());
        return newAccessToken;
    }
}
