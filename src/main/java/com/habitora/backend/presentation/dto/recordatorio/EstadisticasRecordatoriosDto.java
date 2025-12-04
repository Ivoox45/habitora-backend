package com.habitora.backend.presentation.dto.recordatorio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasRecordatoriosDto {
    
    private Long totalProgramados;
    private Long totalEnviados;
    private Long totalFallidos;
    private Long totalCancelados;
    private Double tasaExito; // porcentaje
    private Long proximosEnvios; // recordatorios programados para hoy
}
