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
     * Lista facturas con filtros opcionales.
     */
    List<FacturaResponseDto> listar(Long propiedadId, Long contratoId, Factura.EstadoFactura estado);
}
