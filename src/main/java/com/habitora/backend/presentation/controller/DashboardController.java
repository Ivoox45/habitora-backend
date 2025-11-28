package com.habitora.backend.presentation.controller;

import com.habitora.backend.presentation.dto.dashboard.DashboardResponseDto;
import com.habitora.backend.service.interfaces.IDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/propiedades/{propiedadId}/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Estadísticas y métricas del dashboard")
public class DashboardController {

    private final IDashboardService dashboardService;

    @GetMapping
    @Operation(
            summary = "Obtener estadísticas del dashboard",
            description = """
                    Retorna todas las estadísticas y métricas para el dashboard de una propiedad:
                    - Resumen general (habitaciones, inquilinos, ocupación)
                    - Ingresos (mes actual, anterior, anual)
                    - Estadísticas de facturas y contratos
                    - Gráfico de ingresos mensuales (últimos 6 meses)
                    - Ocupación por piso
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Estadísticas obtenidas exitosamente",
            content = @Content(schema = @Schema(implementation = DashboardResponseDto.class))
    )
    public ResponseEntity<DashboardResponseDto> getDashboardStats(
            @PathVariable Long propiedadId
    ) {
        return ResponseEntity.ok(dashboardService.getDashboardStats(propiedadId));
    }
}
