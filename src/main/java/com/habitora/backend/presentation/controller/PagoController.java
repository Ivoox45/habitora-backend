package com.habitora.backend.presentation.controller;

import com.habitora.backend.presentation.dto.pago.request.PagoCreateRequestDto;
import com.habitora.backend.presentation.dto.pago.response.PagoResponseDto;
import com.habitora.backend.service.interfaces.IPagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/propiedades/{propiedadId}/facturas/{facturaId}/pagos")
@Tag(name = "Pagos", description = "Registro de pagos de facturas de alquiler")
@RequiredArgsConstructor
public class PagoController {

    private final IPagoService pagoService;

    // ======================================================
    // REGISTRAR PAGO DE UNA FACTURA
    // ======================================================
    @PostMapping
    @Operation(
            summary = "Registrar pago de una factura",
            description = """
                    Registra un pago COMPLETO de una factura específica.
                    El monto debe ser exactamente igual al total de la factura.
                    Después de registrar el pago, la factura se marca como PAGADA.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Pago registrado",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PagoResponseDto.class)
            )
    )
    public ResponseEntity<PagoResponseDto> registrarPago(
            @PathVariable Long propiedadId,
            @PathVariable Long facturaId,
            @Valid @RequestBody PagoCreateRequestDto request
    ) {
        return ResponseEntity.ok(pagoService.registrarPago(propiedadId, facturaId, request));
    }

    // ======================================================
    // LISTAR PAGOS DE UNA FACTURA (opcional, para detalle)
    // ======================================================
    @GetMapping
    @Operation(
            summary = "Listar pagos de una factura",
            description = "Devuelve todos los pagos registrados para una factura (normalmente 1, ya que son pagos completos)."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lista de pagos",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = PagoResponseDto.class))
            )
    )
    public ResponseEntity<List<PagoResponseDto>> listarPagos(
            @PathVariable Long propiedadId,
            @PathVariable Long facturaId
    ) {
        return ResponseEntity.ok(pagoService.listarPagosFactura(propiedadId, facturaId));
    }
}
