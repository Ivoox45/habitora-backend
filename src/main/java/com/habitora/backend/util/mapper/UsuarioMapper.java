package com.habitora.backend.util.mapper;

import org.mapstruct.Mapping;

import com.habitora.backend.persistence.entity.Usuario;
import com.habitora.backend.presentation.dto.usuario.request.UsuarioCreateRequestDto;
import com.habitora.backend.presentation.dto.usuario.request.UsuarioUpdateRequestDto;
import com.habitora.backend.presentation.dto.usuario.response.UsuarioResponseDto;
import com.habitora.backend.presentation.dto.usuario.response.UsuarioListResponseDto;

@org.mapstruct.Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contraseña", source = "contraseña")
    Usuario toEntity(UsuarioCreateRequestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contraseña", source = "contraseña")
    Usuario toEntity(UsuarioUpdateRequestDto dto);

    UsuarioResponseDto toResponse(Usuario usuario);

    UsuarioListResponseDto toListResponse(Usuario usuario);

}
