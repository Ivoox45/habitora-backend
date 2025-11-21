package com.habitora.backend.util.mapper;

import com.habitora.backend.persistence.entity.Pago;
import com.habitora.backend.presentation.dto.pago.response.PagoResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PagoMapper {

    @Mapping(target = "facturaId", source = "factura.id")
    @Mapping(target = "contratoId", source = "contrato.id")
    @Mapping(target = "metodo", source = "metodo")
    PagoResponseDto toResponse(Pago pago);
}
