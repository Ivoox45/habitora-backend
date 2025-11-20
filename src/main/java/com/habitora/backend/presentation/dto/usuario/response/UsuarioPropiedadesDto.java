package com.habitora.backend.presentation.dto.usuario.response;

import java.util.List;

public record UsuarioPropiedadesDto(
        Long usuarioId,
        String nombreCompleto,
        String email,
        List<PropiedadSimpleDto> propiedades
) {}
