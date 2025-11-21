package com.habitora.backend.presentation.controller;

import com.habitora.backend.persistence.entity.Factura;
import com.habitora.backend.presentation.dto.factura.response.FacturaResponseDto;
import com.habitora.backend.service.interfaces.IFacturaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/propiedades/{propiedadId}/facturas")
@Tag(name = "Facturas", description = "Gestión de facturación mensual de contratos")
@RequiredArgsConstructor
public class FacturaController {

    private final IFacturaService facturaService;

    // ======================================================
    // LISTAR FACTURAS POR PROPIEDAD
    // ======================================================
    @GetMapping
    @Operation(
            summary = "Listar facturas de una propiedad",
            description = """
                    Lista todas las facturas de la propiedad.
                    
                    Uso típico en el módulo Pagos:
                    - Pagos pendientes: estado = ABIERTA (y/o VENCIDA)
                    - Historial de pagos: estado = PAGADA
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lista de facturas",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = FacturaResponseDto.class))
            )
    )
    public ResponseEntity<List<FacturaResponseDto>> listarPorPropiedad(
            @PathVariable Long propiedadId,
            @RequestParam(required = false) Factura.EstadoFactura estado
    ) {
        return ResponseEntity.ok(facturaService.listarPorPropiedad(propiedadId, estado));
    }

    // ======================================================
    // LISTAR FACTURAS DE UN CONTRATO
    // ======================================================
    @GetMapping("/contrato/{contratoId}")
    @Operation(
            summary = "Listar facturas de un contrato",
            description = "Devuelve todas las facturas asociadas a un contrato dentro de una propiedad."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lista de facturas",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = FacturaResponseDto.class))
            )
    )
    public ResponseEntity<List<FacturaResponseDto>> listarPorContrato(
            @PathVariable Long propiedadId,
            @PathVariable Long contratoId
    ) {
        return ResponseEntity.ok(facturaService.listarPorContrato(propiedadId, contratoId));
    }
}
