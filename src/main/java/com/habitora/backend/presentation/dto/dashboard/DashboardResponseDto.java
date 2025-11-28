package com.habitora.backend.presentation.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDto {
    
    // Resumen General
    private ResumenGeneral resumenGeneral;
    
    // Estadísticas de Habitaciones
    private EstadisticasHabitaciones estadisticasHabitaciones;
    
    // Ingresos
    private IngresosDto ingresos;
    
    // Facturas
    private EstadisticasFacturas estadisticasFacturas;
    
    // Contratos
    private EstadisticasContratos estadisticasContratos;
    
    // Ingresos por Mes (últimos 6 meses)
    private List<IngresoMensual> ingresosPorMes;
    
    // Ocupación por Piso
    private List<OcupacionPiso> ocupacionPorPiso;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumenGeneral {
        private Integer totalHabitaciones;
        private Integer habitacionesOcupadas;
        private Integer habitacionesDisponibles;
        private Integer totalInquilinos;
        private Integer contratosActivos;
        private BigDecimal ingresosMesActual;
        private BigDecimal tasaOcupacion; // porcentaje
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstadisticasHabitaciones {
        private Integer total;
        private Integer ocupadas;
        private Integer disponibles;
        private BigDecimal porcentajeOcupacion;
        private BigDecimal precioPromedioRenta;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngresosDto {
        private BigDecimal mesActual;
        private BigDecimal mesAnterior;
        private BigDecimal totalAno;
        private BigDecimal variacionMensual; // porcentaje
        private BigDecimal pendientesPorCobrar;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstadisticasFacturas {
        private Integer totalAbiertas;
        private Integer totalPagadas;
        private Integer totalVencidas;
        private BigDecimal montoPendiente;
        private BigDecimal montoVencido;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstadisticasContratos {
        private Integer activos;
        private Integer cancelados;
        private Integer sinFirmar;
        private Integer proximosAVencer; // próximos 30 días
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngresoMensual {
        private String mes; // formato: "2024-11"
        private String mesNombre; // formato: "Noviembre 2024"
        private BigDecimal monto;
        private Integer cantidadPagos;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OcupacionPiso {
        private String pisoCodigo;
        private Integer totalHabitaciones;
        private Integer ocupadas;
        private Integer disponibles;
        private BigDecimal porcentajeOcupacion;
    }
}
