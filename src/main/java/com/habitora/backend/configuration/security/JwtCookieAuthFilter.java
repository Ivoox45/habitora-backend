package com.habitora.backend.configuration.security;

import com.habitora.backend.persistence.entity.Usuario;
import com.habitora.backend.service.interfaces.IJwtService;
import com.habitora.backend.service.interfaces.IUsuarioService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtCookieAuthFilter extends OncePerRequestFilter {

    private final IJwtService jwtService;
    private final IUsuarioService usuarioService;
    private final CookieUtil cookieUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Ignore preflight OPTIONS requests
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Leer token de la cookie
        String token = cookieUtil.getCookieValue(request, "access_token");

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Si ya existe autenticación → no reprocesar
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraer email
        String email;
        try {
            email = jwtService.extractUserEmail(token);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        if (email == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 4. Buscar usuario
        Optional<Usuario> userOpt = usuarioService.findByEmail(email);
        if (userOpt.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        Usuario usuario = userOpt.get();

        // 5. Validar token
        if (!jwtService.isTokenValid(token, usuario)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 6. Autenticar
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        usuario,
                        null,
                        Collections.emptyList()
                );

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}
