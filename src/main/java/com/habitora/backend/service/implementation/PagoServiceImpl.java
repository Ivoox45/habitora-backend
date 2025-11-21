package com.habitora.backend.service.implementation;

import com.habitora.backend.persistence.entity.*;
import com.habitora.backend.persistence.repository.FacturaRepository;
import com.habitora.backend.persistence.repository.PagoRepository;
import com.habitora.backend.persistence.repository.PropiedadRepository;
import com.habitora.backend.presentation.dto.pago.request.PagoCreateRequestDto;
import com.habitora.backend.presentation.dto.pago.response.PagoResponseDto;
import com.habitora.backend.service.interfaces.IPagoService;
import com.habitora.backend.util.mapper.PagoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class PagoServiceImpl implements IPagoService {

    private final PagoRepository pagoRepository;
    private final FacturaRepository facturaRepository;
    private final PropiedadRepository propiedadRepository;
    private final PagoMapper pagoMapper;

    /* ===================== helpers seguridad ===================== */

    private Usuario getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Usuario usuario)) {
            throw new IllegalStateException("No hay usuario autenticado.");
        }
        return usuario;
    }

    private Propiedad validarPropiedadDelUsuario(Long propiedadId) {
        Usuario actual = getCurrentUser();

        Propiedad propiedad = propiedadRepository.findById(propiedadId)
                .orElseThrow(() -> new IllegalArgumentException("Propiedad no encontrada."));

        if (!Objects.equals(propiedad.getUsuario().getId(), actual.getId())) {
            throw new IllegalArgumentException("No tienes acceso a esta propiedad.");
        }

        return propiedad;
    }

    private Factura obtenerFacturaValidandoPropiedad(Long propiedadId, Long facturaId) {
        validarPropiedadDelUsuario(propiedadId);

        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada."));

        if (!Objects.equals(factura.getContrato().getPropiedad().getId(), propiedadId)) {
            throw new IllegalArgumentException("La factura no pertenece a esta propiedad.");
        }

        return factura;
    }

    /* ===================== registrar pago ===================== */

    @Override
    public PagoResponseDto registrarPago(Long propiedadId, Long facturaId, PagoCreateRequestDto request) {

        Factura factura = obtenerFacturaValidandoPropiedad(propiedadId, facturaId);

        // Validar estado de factura
        if (factura.getEstado() == Factura.EstadoFactura.PAGADA) {
            throw new IllegalArgumentException("La factura ya está pagada.");
        }
        if (factura.getEstado() == Factura.EstadoFactura.CANCELADA) {
            throw new IllegalArgumentException("La factura está cancelada.");
        }

        // Solo pagos completos
        if (request.getMonto().compareTo(factura.getTotalAPagar()) != 0) {
            throw new IllegalArgumentException("El pago debe ser por el monto exacto de la factura.");
        }

        // Crear pago
        Pago pago = Pago.builder()
                .contrato(factura.getContrato())
                .factura(factura)
                .fechaPago(request.getFechaPago())
                .monto(request.getMonto())
                .metodo(request.getMetodo())
                .build();

        pagoRepository.save(pago);

        // Marcar factura como pagada
        factura.setEstado(Factura.EstadoFactura.PAGADA);
        facturaRepository.save(factura);

        return pagoMapper.toResponse(pago);
    }

    /* ===================== listar pagos ===================== */

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponseDto> listarPagosFactura(Long propiedadId, Long facturaId) {

        Factura factura = obtenerFacturaValidandoPropiedad(propiedadId, facturaId);

        return pagoRepository.findByFacturaId(factura.getId())
                .stream()
                .map(pagoMapper::toResponse)
                .toList();
    }
}
