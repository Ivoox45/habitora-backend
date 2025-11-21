package com.habitora.backend.service.interfaces;

import com.habitora.backend.persistence.entity.Contrato;
import com.habitora.backend.persistence.entity.Factura;
import com.habitora.backend.presentation.dto.factura.response.FacturaResponseDto;

import java.util.List;

public interface IFacturaService {

    /**
     * Genera las facturas mensuales para todo el rango del contrato,
     * usando el precio de la habitación como monto de renta.
     */
    void generarFacturasParaContrato(Contrato contrato);

    /**
     * Lista facturas de una propiedad, con filtro opcional por estado.
     */
    List<FacturaResponseDto> listarPorPropiedad(Long propiedadId, Factura.EstadoFactura estado);

    /**
     * Lista facturas de un contrato específico dentro de una propiedad.
     */
    List<FacturaResponseDto> listarPorContrato(Long propiedadId, Long contratoId);
}
