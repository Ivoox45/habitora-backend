package com.habitora.backend.service.implementation;

import com.habitora.backend.exception.ResourceNotFoundException;
import com.habitora.backend.persistence.entity.*;
import com.habitora.backend.persistence.repository.*;
import com.habitora.backend.presentation.dto.dashboard.DashboardResponseDto;
import com.habitora.backend.presentation.dto.dashboard.DashboardResponseDto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DashboardServiceImpl implements com.habitora.backend.service.interfaces.IDashboardService {

    private final PropiedadRepository propiedadRepository;
    private final HabitacionRepository habitacionRepository;
    private final ContratoRepository contratoRepository;
    private final FacturaRepository facturaRepository;
    private final PagoRepository pagoRepository;
    private final PisoRepository pisoRepository;

    @Override
    public DashboardResponseDto getDashboardStats(Long propiedadId) {
        log.info("Generando estadísticas del dashboard para propiedad ID: {}", propiedadId);

        // Validar propiedad y permisos
        Propiedad propiedad = propiedadRepository.findById(propiedadId)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada."));
        validateOwner(propiedad);

        // Calcular todas las estadísticas
        return DashboardResponseDto.builder()
                .resumenGeneral(calcularResumenGeneral(propiedadId))
                .estadisticasHabitaciones(calcularEstadisticasHabitaciones(propiedadId))
                .ingresos(calcularIngresos(propiedadId))
                .estadisticasFacturas(calcularEstadisticasFacturas(propiedadId))
                .estadisticasContratos(calcularEstadisticasContratos(propiedadId))
                .ingresosPorMes(calcularIngresosPorMes(propiedadId))
                .ocupacionPorPiso(calcularOcupacionPorPiso(propiedadId))
                .build();
    }

    private ResumenGeneral calcularResumenGeneral(Long propiedadId) {
        List<Habitacion> habitaciones = habitacionRepository.findByPropiedadId(propiedadId);
        long ocupadas = habitaciones.stream()
                .filter(h -> h.getEstado() == Habitacion.EstadoHabitacion.OCUPADA)
                .count();
        long disponibles = habitaciones.stream()
                .filter(h -> h.getEstado() == Habitacion.EstadoHabitacion.DISPONIBLE)
                .count();

        List<Contrato> contratosActivos = contratoRepository.findByPropiedadIdAndEstado(
                propiedadId, Contrato.EstadoContrato.ACTIVO);

        // Inquilinos únicos en contratos activos
        Set<Long> inquilinosUnicos = contratosActivos.stream()
                .map(c -> c.getInquilino().getId())
                .collect(Collectors.toSet());

        // Ingresos del mes actual
        YearMonth mesActual = YearMonth.now();
        LocalDate inicioMes = mesActual.atDay(1);
        LocalDate finMes = mesActual.atEndOfMonth();
        
        BigDecimal ingresosMes = calcularIngresosPeriodo(propiedadId, inicioMes, finMes);

        BigDecimal tasaOcupacion = habitaciones.isEmpty() ? BigDecimal.ZERO
                : BigDecimal.valueOf(ocupadas)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(habitaciones.size()), 2, RoundingMode.HALF_UP);

        return ResumenGeneral.builder()
                .totalHabitaciones(habitaciones.size())
                .habitacionesOcupadas((int) ocupadas)
                .habitacionesDisponibles((int) disponibles)
                .totalInquilinos(inquilinosUnicos.size())
                .contratosActivos(contratosActivos.size())
                .ingresosMesActual(ingresosMes)
                .tasaOcupacion(tasaOcupacion)
                .build();
    }

    private EstadisticasHabitaciones calcularEstadisticasHabitaciones(Long propiedadId) {
        List<Habitacion> habitaciones = habitacionRepository.findByPropiedadId(propiedadId);
        
        long ocupadas = habitaciones.stream()
                .filter(h -> h.getEstado() == Habitacion.EstadoHabitacion.OCUPADA)
                .count();

        BigDecimal porcentajeOcupacion = habitaciones.isEmpty() ? BigDecimal.ZERO
                : BigDecimal.valueOf(ocupadas)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(habitaciones.size()), 2, RoundingMode.HALF_UP);

        BigDecimal precioPromedio = habitaciones.isEmpty() ? BigDecimal.ZERO
                : habitaciones.stream()
                        .map(Habitacion::getPrecioRenta)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(habitaciones.size()), 2, RoundingMode.HALF_UP);

        return EstadisticasHabitaciones.builder()
                .total(habitaciones.size())
                .ocupadas((int) ocupadas)
                .disponibles(habitaciones.size() - (int) ocupadas)
                .porcentajeOcupacion(porcentajeOcupacion)
                .precioPromedioRenta(precioPromedio)
                .build();
    }

    private IngresosDto calcularIngresos(Long propiedadId) {
        YearMonth mesActual = YearMonth.now();
        YearMonth mesAnterior = mesActual.minusMonths(1);

        LocalDate inicioMesActual = mesActual.atDay(1);
        LocalDate finMesActual = mesActual.atEndOfMonth();
        LocalDate inicioMesAnterior = mesAnterior.atDay(1);
        LocalDate finMesAnterior = mesAnterior.atEndOfMonth();

        BigDecimal ingresosMesActual = calcularIngresosPeriodo(propiedadId, inicioMesActual, finMesActual);
        BigDecimal ingresosMesAnterior = calcularIngresosPeriodo(propiedadId, inicioMesAnterior, finMesAnterior);

        // Total del año
        LocalDate inicioAno = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        LocalDate finAno = LocalDate.of(LocalDate.now().getYear(), 12, 31);
        BigDecimal totalAno = calcularIngresosPeriodo(propiedadId, inicioAno, finAno);

        // Variación mensual
        BigDecimal variacion = BigDecimal.ZERO;
        if (ingresosMesAnterior.compareTo(BigDecimal.ZERO) > 0) {
            variacion = ingresosMesActual.subtract(ingresosMesAnterior)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(ingresosMesAnterior, 2, RoundingMode.HALF_UP);
        }

        // Pendientes por cobrar (facturas abiertas + vencidas)
        List<Factura> facturasAbiertas = facturaRepository.findByContratoPropertyIdAndEstadoIn(
                propiedadId, Arrays.asList(Factura.EstadoFactura.ABIERTA, Factura.EstadoFactura.VENCIDA));
        
        BigDecimal pendientes = facturasAbiertas.stream()
                .map(Factura::getMontoRenta)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return IngresosDto.builder()
                .mesActual(ingresosMesActual)
                .mesAnterior(ingresosMesAnterior)
                .totalAno(totalAno)
                .variacionMensual(variacion)
                .pendientesPorCobrar(pendientes)
                .build();
    }

    private EstadisticasFacturas calcularEstadisticasFacturas(Long propiedadId) {
        List<Factura> todasFacturas = facturaRepository.findByContratoPropertyId(propiedadId);

        long abiertas = todasFacturas.stream()
                .filter(f -> f.getEstado() == Factura.EstadoFactura.ABIERTA)
                .count();
        
        long pagadas = todasFacturas.stream()
                .filter(f -> f.getEstado() == Factura.EstadoFactura.PAGADA)
                .count();
        
        long vencidas = todasFacturas.stream()
                .filter(f -> f.getEstado() == Factura.EstadoFactura.VENCIDA)
                .count();

        BigDecimal montoPendiente = todasFacturas.stream()
                .filter(f -> f.getEstado() == Factura.EstadoFactura.ABIERTA)
                .map(Factura::getMontoRenta)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal montoVencido = todasFacturas.stream()
                .filter(f -> f.getEstado() == Factura.EstadoFactura.VENCIDA)
                .map(Factura::getMontoRenta)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return EstadisticasFacturas.builder()
                .totalAbiertas((int) abiertas)
                .totalPagadas((int) pagadas)
                .totalVencidas((int) vencidas)
                .montoPendiente(montoPendiente)
                .montoVencido(montoVencido)
                .build();
    }

    private EstadisticasContratos calcularEstadisticasContratos(Long propiedadId) {
        List<Contrato> todosContratos = contratoRepository.findByPropiedadId(propiedadId);

        long activos = todosContratos.stream()
                .filter(c -> c.getEstado() == Contrato.EstadoContrato.ACTIVO)
                .count();

        long cancelados = todosContratos.stream()
                .filter(c -> c.getEstado() == Contrato.EstadoContrato.CANCELADO)
                .count();

        long sinFirmar = todosContratos.stream()
                .filter(c -> c.getEstado() == Contrato.EstadoContrato.ACTIVO && c.getFirmaPath() == null)
                .count();

        // Contratos que vencen en los próximos 30 días
        LocalDate hoy = LocalDate.now();
        LocalDate en30Dias = hoy.plusDays(30);
        
        long proximosAVencer = todosContratos.stream()
                .filter(c -> c.getEstado() == Contrato.EstadoContrato.ACTIVO)
                .filter(c -> c.getFechaFin() != null)
                .filter(c -> !c.getFechaFin().isBefore(hoy) && !c.getFechaFin().isAfter(en30Dias))
                .count();

        return EstadisticasContratos.builder()
                .activos((int) activos)
                .cancelados((int) cancelados)
                .sinFirmar((int) sinFirmar)
                .proximosAVencer((int) proximosAVencer)
                .build();
    }

    private List<IngresoMensual> calcularIngresosPorMes(Long propiedadId) {
        List<IngresoMensual> ingresos = new ArrayList<>();
        YearMonth mesActual = YearMonth.now();

        // Últimos 6 meses
        for (int i = 5; i >= 0; i--) {
            YearMonth mes = mesActual.minusMonths(i);
            LocalDate inicio = mes.atDay(1);
            LocalDate fin = mes.atEndOfMonth();

            List<Contrato> contratos = contratoRepository.findByPropiedadId(propiedadId);
            List<Long> contratoIds = contratos.stream()
                    .map(Contrato::getId)
                    .collect(Collectors.toList());

            List<Pago> pagos = pagoRepository.findByContratoIdInAndFechaPagoBetweenAndEstado(
                    contratoIds, inicio, fin, Pago.EstadoPago.COMPLETADO);

            BigDecimal total = pagos.stream()
                    .map(Pago::getMonto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String mesNombre = mes.getMonth().getDisplayName(TextStyle.FULL, java.util.Locale.of("es", "ES"));
            mesNombre = mesNombre.substring(0, 1).toUpperCase() + mesNombre.substring(1);

            ingresos.add(IngresoMensual.builder()
                    .mes(mes.toString())
                    .mesNombre(mesNombre + " " + mes.getYear())
                    .monto(total)
                    .cantidadPagos(pagos.size())
                    .build());
        }

        return ingresos;
    }

    private List<OcupacionPiso> calcularOcupacionPorPiso(Long propiedadId) {
        List<Piso> pisos = pisoRepository.findByPropiedadId(propiedadId);
        List<OcupacionPiso> resultado = new ArrayList<>();

        for (Piso piso : pisos) {
            List<Habitacion> habitaciones = habitacionRepository.findByPisoId(piso.getId());
            
            long ocupadas = habitaciones.stream()
                    .filter(h -> h.getEstado() == Habitacion.EstadoHabitacion.OCUPADA)
                    .count();

            BigDecimal porcentaje = habitaciones.isEmpty() ? BigDecimal.ZERO
                    : BigDecimal.valueOf(ocupadas)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(habitaciones.size()), 2, RoundingMode.HALF_UP);

            resultado.add(OcupacionPiso.builder()
                    .pisoCodigo(piso.getCodigo())
                    .totalHabitaciones(habitaciones.size())
                    .ocupadas((int) ocupadas)
                    .disponibles(habitaciones.size() - (int) ocupadas)
                    .porcentajeOcupacion(porcentaje)
                    .build());
        }

        return resultado;
    }

    private BigDecimal calcularIngresosPeriodo(Long propiedadId, LocalDate inicio, LocalDate fin) {
        List<Contrato> contratos = contratoRepository.findByPropiedadId(propiedadId);
        List<Long> contratoIds = contratos.stream()
                .map(Contrato::getId)
                .collect(Collectors.toList());

        if (contratoIds.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<Pago> pagos = pagoRepository.findByContratoIdInAndFechaPagoBetweenAndEstado(
                contratoIds, inicio, fin, Pago.EstadoPago.COMPLETADO);

        return pagos.stream()
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validateOwner(Propiedad propiedad) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Usuario usuario)) {
            throw new IllegalStateException("No hay usuario autenticado.");
        }
        
        if (!usuario.getId().equals(propiedad.getUsuario().getId())) {
            throw new IllegalArgumentException("No tienes permiso para acceder a esta propiedad.");
        }
    }
}
