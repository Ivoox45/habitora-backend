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
        String token = null;

        // Prefer Authorization header Bearer token
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        // Fallback to cookie (compatibility)
        if (token == null) {
            token = cookieUtil.getCookieValue(request, "access_token");
        }

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
            // Token malformed/invalid signature
            // If token came from Authorization header, return 401; if from cookie, remove cookie then 401.
            cookieUtil.deleteCookie(response, "access_token");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token");
            return;
        }

        if (email == null) {
            cookieUtil.deleteCookie(response, "access_token");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token");
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
            cookieUtil.deleteCookie(response, "access_token");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Expired or invalid token");
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
