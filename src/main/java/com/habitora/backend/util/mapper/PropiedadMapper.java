package com.habitora.backend.util.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.habitora.backend.persistence.entity.Propiedad;
import com.habitora.backend.presentation.dto.propiedad.request.PropiedadCreateRequestDto;
import com.habitora.backend.presentation.dto.propiedad.response.PropiedadListResponseDto;
import com.habitora.backend.presentation.dto.propiedad.response.PropiedadResponseDto;

@Mapper(componentModel = "spring")
public interface PropiedadMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "pisos", ignore = true)
    @Mapping(target = "habitaciones", ignore = true)
    @Mapping(target = "contratos", ignore = true)
    @Mapping(target = "configRecordatorios", ignore = true)
    Propiedad toEntity(PropiedadCreateRequestDto dto);

    PropiedadResponseDto toResponse(Propiedad entidad);

    PropiedadListResponseDto toListResponse(Propiedad entidad);
}
