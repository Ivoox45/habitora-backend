package com.habitora.backend.util.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.habitora.backend.persistence.entity.Habitacion;
import com.habitora.backend.presentation.dto.habitacion.response.HabitacionResponseDto;
import com.habitora.backend.presentation.dto.habitacion.response.HabitacionListResponseDto;

@Mapper(componentModel = "spring")
public interface HabitacionMapper {

    @Mapping(target = "propiedadId", source = "propiedad.id")
    @Mapping(target = "pisoId", source = "piso.id")
    HabitacionResponseDto toResponse(Habitacion habitacion);

    @Mapping(target = "pisoId", source = "piso.id")
    @Mapping(target = "estado", source = "estado")
    HabitacionListResponseDto toListResponse(Habitacion habitacion);
}
