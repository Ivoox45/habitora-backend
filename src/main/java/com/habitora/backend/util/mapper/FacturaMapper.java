package com.habitora.backend.util.mapper;

import com.habitora.backend.persistence.entity.Factura;
import com.habitora.backend.presentation.dto.factura.response.FacturaResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FacturaMapper {

    @Mapping(target = "contratoId", source = "contrato.id")
    @Mapping(target = "inquilinoId", source = "contrato.inquilino.id")
    @Mapping(target = "inquilinoNombre", source = "contrato.inquilino.nombreCompleto")
    @Mapping(target = "habitacionId", source = "contrato.habitacion.id")
    @Mapping(target = "habitacionCodigo", source = "contrato.habitacion.codigo")
    @Mapping(target = "estado", source = "estado")
    FacturaResponseDto toResponse(Factura factura);
}
