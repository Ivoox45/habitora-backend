package com.habitora.backend.util.mapper;

import com.habitora.backend.persistence.entity.Contrato;
import com.habitora.backend.persistence.entity.Habitacion;
import com.habitora.backend.persistence.entity.Inquilino;
import com.habitora.backend.presentation.dto.contrato.response.ContratoDetailResponseDto;
import com.habitora.backend.presentation.dto.contrato.response.ContratoListResponseDto;
import org.springframework.stereotype.Component;

@Component
public class ContratoMapper {

    public ContratoListResponseDto toListDto(Contrato contrato) {
        ContratoListResponseDto dto = new ContratoListResponseDto();

        dto.setId(contrato.getId());
        dto.setEstado(contrato.getEstado().name());
        dto.setFechaInicio(contrato.getFechaInicio());
        dto.setFechaFin(contrato.getFechaFin());
        dto.setMontoDeposito(contrato.getMontoDeposito());

        Inquilino inq = contrato.getInquilino();
        dto.setInquilinoId(inq.getId());
        dto.setInquilinoNombre(inq.getNombreCompleto());
        dto.setInquilinoDni(inq.getNumeroDni());

        Habitacion hab = contrato.getHabitacion();
        dto.setHabitacionId(hab.getId());
        dto.setHabitacionCodigo(hab.getCodigo());

        return dto;
    }

    public ContratoDetailResponseDto toDetailDto(Contrato contrato) {
        ContratoDetailResponseDto dto = new ContratoDetailResponseDto();

        dto.setId(contrato.getId());
        dto.setPropiedadId(contrato.getPropiedad().getId());
        dto.setEstado(contrato.getEstado().name());
        dto.setFechaInicio(contrato.getFechaInicio());
        dto.setFechaFin(contrato.getFechaFin());
        dto.setMontoDeposito(contrato.getMontoDeposito());

        Inquilino inq = contrato.getInquilino();
        dto.setInquilinoId(inq.getId());
        dto.setInquilinoNombre(inq.getNombreCompleto());
        dto.setInquilinoDni(inq.getNumeroDni());
        dto.setInquilinoEmail(inq.getEmail());
        dto.setInquilinoTelefono(inq.getTelefonoWhatsapp());

        Habitacion hab = contrato.getHabitacion();
        dto.setHabitacionId(hab.getId());
        dto.setHabitacionCodigo(hab.getCodigo());
        dto.setHabitacionEstado(hab.getEstado().name());
        dto.setHabitacionPrecioRenta(hab.getPrecioRenta().toPlainString());

        dto.setTieneFirma(contrato.getFirmaInquilino() != null);

        return dto;
    }
}
