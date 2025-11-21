package com.habitora.backend.service.interfaces;

import com.habitora.backend.persistence.entity.Contrato;
import com.habitora.backend.persistence.entity.Factura;
import com.habitora.backend.presentation.dto.factura.response.FacturaResponseDto;

import java.util.List;

public interface IFacturaService {

    // Se llama cuando se crea un contrato
    void generarFacturasParaContrato(Contrato contrato);

    List<FacturaResponseDto> listarPorPropiedad(Long propiedadId, Factura.EstadoFactura estado);

    List<FacturaResponseDto> listarPorContrato(Long propiedadId, Long contratoId);
}
