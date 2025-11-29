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

        // Si fin <= inicio, una sola factura (caso borde)
        if (!fin.isAfter(inicio)) {
            Factura unica = Factura.builder()
                    .contrato(contrato)
                    .periodoInicio(inicio)
                    .periodoFin(fin)
                    .fechaVencimiento(inicio.plusDays(5))
                    .montoRenta(montoMensual)
                    .estado(Factura.EstadoFactura.PAGADA)
                    .build();
            facturaRepository.save(unica);
            return;
        }

        // Calcular número de meses (sin off-by-one). Ej: 29 Nov -> 29 May = 6 meses.
        int meses = (fin.getYear() - inicio.getYear()) * 12 + (fin.getMonthValue() - inicio.getMonthValue());
        if (meses <= 0) {
            // Duración menor a un mes: una sola factura.
            Factura unica = Factura.builder()
                    .contrato(contrato)
                    .periodoInicio(inicio)
                    .periodoFin(fin)
                    .fechaVencimiento(inicio.plusDays(5))
                    .montoRenta(montoMensual)
                    .estado(Factura.EstadoFactura.PAGADA)
                    .build();
            facturaRepository.save(unica);
            return;
        }

        for (int i = 0; i < meses; i++) {
            LocalDate periodoInicio = inicio.plusMonths(i);
            LocalDate siguienteInicio = inicio.plusMonths(i + 1);
            boolean esUltima = i == meses - 1; // última iteración
            LocalDate periodoFin = esUltima ? fin : siguienteInicio.minusDays(1);
            if (periodoFin.isBefore(periodoInicio)) {
                periodoFin = periodoInicio; // seguridad
            }
            LocalDate fechaVencimiento = periodoInicio.plusDays(5);
            Factura.EstadoFactura estadoInicial = (i == 0) ? Factura.EstadoFactura.PAGADA : Factura.EstadoFactura.ABIERTA;
            Factura factura = Factura.builder()
                    .contrato(contrato)
                    .periodoInicio(periodoInicio)
                    .periodoFin(periodoFin)
                    .fechaVencimiento(fechaVencimiento)
                    .montoRenta(montoMensual)
                    .estado(estadoInicial)
                    .build();
            facturaRepository.save(factura);
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
