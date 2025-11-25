package com.habitora.backend.presentation.controller;

import com.habitora.backend.persistence.entity.Usuario;
import com.habitora.backend.presentation.dto.usuario.response.UsuarioTienePropiedadesResponseDto;
import com.habitora.backend.presentation.dto.usuario.response.UsuarioPropiedadesDto;
import com.habitora.backend.service.interfaces.IUsuarioService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "APIs para gestionar usuarios")
@RequiredArgsConstructor
public class UsuarioController {

        private final IUsuarioService usuarioService;

        @GetMapping("/tiene-propiedades")
        @Operation(summary = "Verifica si el usuario autenticado tiene propiedades")
        public ResponseEntity<UsuarioTienePropiedadesResponseDto> usuarioTienePropiedades() {

                Authentication auth = SecurityContextHolder.getContext().getAuthentication();

                if (auth == null || !(auth.getPrincipal() instanceof Usuario usuario)) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }

                boolean tiene = usuarioService.userHasProperties(usuario.getId());

                return ResponseEntity.ok(
                                new UsuarioTienePropiedadesResponseDto(
                                                usuario.getId(),
                                                tiene));
        }

        @GetMapping("/{usuarioId}/propiedades-simple")
        @Operation(summary = "Obtiene solo los datos básicos del usuario y sus propiedades")
        public ResponseEntity<UsuarioPropiedadesDto> getSimpleProperties(@PathVariable Long usuarioId) {
                return ResponseEntity.ok(usuarioService.getUserSimpleProperties(usuarioId));
        }

}
