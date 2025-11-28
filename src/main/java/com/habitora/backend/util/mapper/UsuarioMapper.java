package com.habitora.backend.util.mapper;

import org.mapstruct.Mapping;

import com.habitora.backend.persistence.entity.Usuario;
import com.habitora.backend.presentation.dto.usuario.request.UsuarioCreateRequestDto;
import com.habitora.backend.presentation.dto.usuario.request.UsuarioUpdateRequestDto;
import com.habitora.backend.presentation.dto.usuario.response.UsuarioResponseDto;
import com.habitora.backend.presentation.dto.usuario.response.UsuarioListResponseDto;

@SuppressWarnings("all")
@org.mapstruct.Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface UsuarioMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contrasena", source = "contrasena")
    Usuario toEntity(UsuarioCreateRequestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contrasena", source = "contrasena")
    Usuario toEntity(UsuarioUpdateRequestDto dto);

    UsuarioResponseDto toResponse(Usuario usuario);

    UsuarioListResponseDto toListResponse(Usuario usuario);

}
