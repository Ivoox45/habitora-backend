package com.habitora.backend.util.mapper;

import com.habitora.backend.persistence.entity.Pago;
import com.habitora.backend.presentation.dto.pago.response.PagoResponseDto;
import org.springframework.stereotype.Component;

@Component
public class PagoMapper {

    public PagoResponseDto toResponse(Pago pago) {

        PagoResponseDto dto = new PagoResponseDto();

        dto.setId(pago.getId());
        dto.setFacturaId(pago.getFactura().getId());
        dto.setContratoId(pago.getContrato().getId());

        dto.setFechaPago(pago.getFechaPago());
        dto.setMonto(pago.getMonto());

        // método de pago como String
        dto.setMetodo(pago.getMetodo().name());

        return dto;
    }
}
