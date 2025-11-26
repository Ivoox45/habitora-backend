package com.habitora.backend.util.mapper;

import com.habitora.backend.persistence.entity.Inquilino;
import com.habitora.backend.presentation.dto.inquilino.request.InquilinoCreateRequestDto;
import com.habitora.backend.presentation.dto.inquilino.request.InquilinoUpdateRequestDto;
import com.habitora.backend.presentation.dto.inquilino.response.InquilinoListResponseDto;
import com.habitora.backend.presentation.dto.inquilino.response.InquilinoResponseDto;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InquilinoMapper {

    // ============================
    // CREATE → Entity
    // ============================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contratos", ignore = true) // no viene del DTO
    @Mapping(target = "propiedad", ignore = true)
    Inquilino toEntity(InquilinoCreateRequestDto dto);


    // ============================
    // UPDATE → Entity
    // ============================

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contratos", ignore = true) // nunca se actualiza desde DTO
    @Mapping(target = "propiedad", ignore = true)
    void updateEntity(@MappingTarget Inquilino entity, InquilinoUpdateRequestDto dto);


    // ============================
    // Entity → Single Response
    // ============================

    @Mapping(target = "cantidadContratos",
             expression = "java( entity.getContratos() != null ? entity.getContratos().size() : 0 )")
    InquilinoResponseDto toResponse(Inquilino entity);


    // ============================
    // Entity → List Response
    // ============================

    @Mapping(target = "cantidadContratos",
             expression = "java( entity.getContratos() != null ? entity.getContratos().size() : 0 )")
    InquilinoListResponseDto toListResponse(Inquilino entity);


    // ============================
    // List
    // ============================

    List<InquilinoListResponseDto> toListResponse(List<Inquilino> list);
}
