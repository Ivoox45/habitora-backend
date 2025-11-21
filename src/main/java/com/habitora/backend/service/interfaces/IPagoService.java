package com.habitora.backend.service.interfaces;

import com.habitora.backend.presentation.dto.pago.request.PagoCreateRequestDto;
import com.habitora.backend.presentation.dto.pago.response.PagoResponseDto;

import java.util.List;

public interface IPagoService {

    PagoResponseDto registrarPago(Long propiedadId, Long facturaId, PagoCreateRequestDto request);

    List<PagoResponseDto> listarPagosFactura(Long propiedadId, Long facturaId);
}
