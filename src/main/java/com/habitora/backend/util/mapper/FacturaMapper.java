package com.habitora.backend.util.mapper;

import com.habitora.backend.persistence.entity.Factura;
import com.habitora.backend.presentation.dto.factura.response.FacturaResponseDto;
import org.springframework.stereotype.Component;

@Component
public class FacturaMapper {

    public FacturaResponseDto toResponse(Factura f) {
        if (f == null)
            return null;

        FacturaResponseDto dto = new FacturaResponseDto();

        dto.setId(f.getId());
        dto.setContratoId(f.getContrato().getId());
        dto.setInquilinoId(f.getContrato().getInquilino().getId());
        dto.setInquilinoNombre(f.getContrato().getInquilino().getNombreCompleto());

        dto.setHabitacionId(f.getContrato().getHabitacion().getId());
        dto.setHabitacionCodigo(f.getContrato().getHabitacion().getCodigo());

        dto.setPeriodoInicio(f.getPeriodoInicio());
        dto.setPeriodoFin(f.getPeriodoFin());
        dto.setFechaVencimiento(f.getFechaVencimiento());

        dto.setMontoRenta(f.getMontoRenta());
        dto.setEstado(f.getEstado().name());

        // Los flags se ponen en FacturaServiceImpl (buildDtoConFlags)

        return dto;
    }
}