package com.habitora.backend.service.implementation;

import com.habitora.backend.persistence.entity.*;
import com.habitora.backend.persistence.repository.FacturaRepository;
import com.habitora.backend.persistence.repository.PropiedadRepository;
import com.habitora.backend.presentation.dto.factura.response.FacturaResponseDto;
import com.habitora.backend.service.interfaces.IFacturaService;
import com.habitora.backend.util.mapper.FacturaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class FacturaServiceImpl implements IFacturaService {

    private final FacturaRepository facturaRepository;
    private final PropiedadRepository propiedadRepository;
    private final FacturaMapper facturaMapper;

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

    /* ===================== generación de facturas ===================== */

    @Override
    public void generarFacturasParaContrato(Contrato contrato) {

        // Si no hay fecha fin, por ahora no generamos nada automáticamente
        if (contrato.getFechaFin() == null) {
            return;
        }

        BigDecimal montoMensual = contrato.getHabitacion().getPrecioRenta();

        LocalDate inicio = contrato.getFechaInicio();
        LocalDate fin = contrato.getFechaFin();

        YearMonth mesActual = YearMonth.from(inicio);
        YearMonth mesFin = YearMonth.from(fin);

        while (!mesActual.isAfter(mesFin)) {

            LocalDate periodoInicio = mesActual.atDay(1);
            LocalDate periodoFin = mesActual.atEndOfMonth();

            // Ajustes si el contrato inicia/termina a mitad de mes
            if (periodoInicio.isBefore(inicio)) {
                periodoInicio = inicio;
            }
            if (periodoFin.isAfter(fin)) {
                periodoFin = fin;
            }

            // Vencimiento: día 5 del mes correspondiente
            LocalDate fechaVencimiento = mesActual.atDay(5);

            Factura factura = Factura.builder()
                    .contrato(contrato)
                    .periodoInicio(periodoInicio)
                    .periodoFin(periodoFin)
                    .fechaVencimiento(fechaVencimiento)
                    .montoRenta(montoMensual)
                    .estado(Factura.EstadoFactura.ABIERTA)
                    .build();

            facturaRepository.save(factura);

            mesActual = mesActual.plusMonths(1);
        }
    }

    /* ===================== helpers de mapeo con flags ===================== */

    private FacturaResponseDto buildDtoConFlags(Factura f) {
        FacturaResponseDto dto = facturaMapper.toResponse(f);

        LocalDate hoy = LocalDate.now();

        boolean pagada = f.getEstado() == Factura.EstadoFactura.PAGADA;
        boolean vencidaPorEstado = f.getEstado() == Factura.EstadoFactura.VENCIDA;
        boolean vencimientoPasado = f.getFechaVencimiento().isBefore(hoy);

        boolean esVencida = vencidaPorEstado || (!pagada && vencimientoPasado);

        dto.setEsPagada(pagada);
        dto.setEsVencida(esVencida);

        if (esVencida && !pagada) {
            long dias = ChronoUnit.DAYS.between(f.getFechaVencimiento(), hoy);
            dto.setDiasRetraso((int) dias);
        } else {
            dto.setDiasRetraso(0);
        }

        return dto;
    }

    /* ===================== listar facturas ===================== */

    @Override
    @Transactional(readOnly = true)
    public List<FacturaResponseDto> listar(Long propiedadId, Long contratoId, Factura.EstadoFactura estado) {

        validarPropiedadDelUsuario(propiedadId);

        List<Factura> facturas;

        if (contratoId != null) {
            // Filtrar por contrato
            facturas = facturaRepository.findByContratoId(contratoId);
            // Asegurar que el contrato pertenece a la propiedad
            facturas = facturas.stream()
                    .filter(f -> f.getContrato().getPropiedad().getId().equals(propiedadId))
                    .toList();
        } else if (estado != null) {
            // Filtrar por estado
            facturas = facturaRepository.findByContratoPropiedadIdAndEstado(propiedadId, estado);
        } else {
            // Todas las de la propiedad
            facturas = facturaRepository.findByContratoPropiedadId(propiedadId);
        }

        // Si se pide filtrar por estado Y contrato a la vez (aunque el repo de arriba
        // solo filtraba por contrato)
        if (contratoId != null && estado != null) {
            facturas = facturas.stream()
                    .filter(f -> f.getEstado() == estado)
                    .toList();
        }

        return facturas.stream()
                .map(this::buildDtoConFlags)
                .toList();
    }
}
