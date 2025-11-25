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
        @Operation(summary = "Listar facturas", description = """
                        Lista las facturas de la propiedad.
                        Filtros opcionales:
                        - contratoId: para ver solo las de un contrato específico.
                        - estado: ABIERTA, PAGADA, VENCIDA, ANULADA.
                        """)
        @ApiResponse(responseCode = "200", description = "Lista de facturas", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = FacturaResponseDto.class))))
        public ResponseEntity<List<FacturaResponseDto>> listar(
                        @PathVariable Long propiedadId,
                        @RequestParam(required = false) Long contratoId,
                        @RequestParam(required = false) Factura.EstadoFactura estado) {
                return ResponseEntity.ok(facturaService.listar(propiedadId, contratoId, estado));
        }
}
