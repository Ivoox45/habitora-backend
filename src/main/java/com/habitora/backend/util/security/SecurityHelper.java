package com.habitora.backend.util.security;

import com.habitora.backend.exception.UnauthorizedException;
import com.habitora.backend.persistence.entity.Propiedad;
import com.habitora.backend.persistence.entity.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SecurityHelper {

    public Usuario getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Usuario usuario)) {
            throw new UnauthorizedException("No se encontró un usuario autenticado en el contexto.");
        }
        return usuario;
    }

    public void validateOwner(Propiedad propiedad, Usuario usuario) {
        if (!Objects.equals(propiedad.getUsuario().getId(), usuario.getId())) {
            throw new UnauthorizedException("No tienes acceso a esta propiedad.");
        }
    }

    public void validateOwner(Propiedad propiedad) {
        validateOwner(propiedad, getCurrentUser());
    }
}
