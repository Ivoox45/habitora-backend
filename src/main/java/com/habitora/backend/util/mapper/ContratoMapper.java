package com.habitora.backend.util.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.habitora.backend.persistence.entity.Contrato;
import com.habitora.backend.presentation.dto.contrato.response.ContratoDetailResponseDto;
import com.habitora.backend.presentation.dto.contrato.response.ContratoListResponseDto;

@Mapper(componentModel = "spring")
public interface ContratoMapper {

    @Mapping(target = "estado", expression = "java(c.getEstado().name())")
    @Mapping(target = "inquilinoId", source = "inquilino.id")
    @Mapping(target = "inquilinoNombre", source = "inquilino.nombreCompleto")
    @Mapping(target = "inquilinoDni", source = "inquilino.numeroDni")
    @Mapping(target = "habitacionId", source = "habitacion.id")
    @Mapping(target = "habitacionCodigo", source = "habitacion.codigo")
    ContratoListResponseDto toListDto(Contrato c);

    @Mapping(target = "propiedadId", source = "propiedad.id")
    @Mapping(target = "estado", expression = "java(c.getEstado().name())")
    @Mapping(target = "inquilinoId", source = "inquilino.id")
    @Mapping(target = "inquilinoNombre", source = "inquilino.nombreCompleto")
    @Mapping(target = "inquilinoDni", source = "inquilino.numeroDni")
    @Mapping(target = "inquilinoEmail", source = "inquilino.email")
    @Mapping(target = "inquilinoTelefono", source = "inquilino.telefonoWhatsapp")
    @Mapping(target = "habitacionId", source = "habitacion.id")
    @Mapping(target = "habitacionCodigo", source = "habitacion.codigo")
    @Mapping(target = "habitacionEstado", expression = "java(c.getHabitacion().getEstado().name())")
    @Mapping(target = "habitacionPrecioRenta", expression = "java(c.getHabitacion().getPrecioRenta().toPlainString())")
    @Mapping(target = "tieneFirma", expression = "java(c.getFirmaInquilino() != null)")
    ContratoDetailResponseDto toDetailDto(Contrato c);
}
