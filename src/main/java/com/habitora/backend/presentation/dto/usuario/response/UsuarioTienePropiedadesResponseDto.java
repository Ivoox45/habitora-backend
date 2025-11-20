package com.habitora.backend.presentation.dto.usuario.response;

public record UsuarioTienePropiedadesResponseDto(
        Long usuarioId,
        boolean tienePropiedades
) {}
