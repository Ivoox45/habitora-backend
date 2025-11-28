package com.habitora.backend.service.interfaces;

import com.habitora.backend.presentation.dto.dashboard.DashboardResponseDto;

public interface IDashboardService {
    
    /**
     * Obtiene todas las estadísticas del dashboard para una propiedad específica
     */
    DashboardResponseDto getDashboardStats(Long propiedadId);
}
