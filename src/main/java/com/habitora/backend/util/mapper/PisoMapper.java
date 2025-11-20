package com.habitora.backend.util.mapper;

import com.habitora.backend.persistence.entity.Piso;
import com.habitora.backend.presentation.dto.piso.response.PisoListResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PisoMapper {

    PisoListResponseDto toListResponse(Piso piso);
}
