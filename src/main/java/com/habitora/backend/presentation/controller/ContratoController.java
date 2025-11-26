package com.habitora.backend.presentation.controller;

import com.habitora.backend.persistence.entity.Contrato;
import com.habitora.backend.presentation.dto.contrato.request.ContratoCreateRequestDto;
import com.habitora.backend.presentation.dto.contrato.response.ContratoDetailResponseDto;
import com.habitora.backend.presentation.dto.contrato.response.ContratoListResponseDto;
import com.habitora.backend.service.interfaces.IContratoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.Objects;
import java.util.List;

@RestController
@RequestMapping("/api/propiedades/{propiedadId}/contratos")
@RequiredArgsConstructor
@Tag(name = "Contratos", description = "Gestión de contratos de arrendamiento dentro de una propiedad")
public class ContratoController {

    private final IContratoService service;

    // ======================================================
    // CREAR CONTRATO
    // ======================================================
    @PostMapping
    @Operation(
            summary = "Crear contrato",
            description = """
                    Crea un contrato de arrendamiento para un inquilino y una habitación.
                    Después de crearse, el sistema genera automáticamente las facturas
                    mensuales según fechaInicio y fechaFin del contrato.
                    """
    )
    @ApiResponse(
            responseCode = "201",
            description = "Contrato creado",
            content = @Content(schema = @Schema(implementation = ContratoDetailResponseDto.class))
    )
    public ResponseEntity<ContratoDetailResponseDto> create(
            @PathVariable Long propiedadId,
            @Valid @RequestBody ContratoCreateRequestDto request) {

        ContratoDetailResponseDto response = service.create(propiedadId, request);

        return ResponseEntity.created(
                Objects.requireNonNull(URI.create("/api/propiedades/" + propiedadId + "/contratos/" + response.getId()))
        ).body(response);
    }

    // ======================================================
    // LISTAR CONTRATOS (para listado de contratos en la UI)
    // ======================================================
    @GetMapping
    @Operation(
            summary = "Listar contratos",
            description = """
                    Lista los contratos de la propiedad.
                    Filtros opcionales:
                    - estado = ACTIVO / CANCELADO
                    - search = nombre del inquilino o DNI
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lista de contratos",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ContratoListResponseDto.class)))
    )
    public ResponseEntity<List<ContratoListResponseDto>> list(
            @PathVariable Long propiedadId,
            @RequestParam(required = false) Contrato.EstadoContrato estado,
            @RequestParam(required = false) String search) {

        return ResponseEntity.ok(service.list(propiedadId, estado, search));
    }

    // ======================================================
    // DETALLE DE CONTRATO (para el resumen antes de firmar)
    // ======================================================
    @GetMapping("/{contratoId}")
    @Operation(
            summary = "Obtener detalle de contrato",
            description = "Devuelve el detalle del contrato, incluyendo inquilino, habitación y montos."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Contrato encontrado",
            content = @Content(schema = @Schema(implementation = ContratoDetailResponseDto.class))
    )
    public ResponseEntity<ContratoDetailResponseDto> detail(
            @PathVariable Long propiedadId,
            @PathVariable Long contratoId) {

        return ResponseEntity.ok(service.getById(propiedadId, contratoId));
    }

    // ======================================================
    // FINALIZAR CONTRATO
    // ======================================================
    @PutMapping("/{contratoId}/finalizar")
    @Operation(
            summary = "Finalizar contrato",
            description = "Marca el contrato como CANCELADO y libera la habitación (la pasa a DISPONIBLE)."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Contrato finalizado",
            content = @Content(schema = @Schema(implementation = ContratoDetailResponseDto.class))
    )
    public ResponseEntity<ContratoDetailResponseDto> finalizar(
            @PathVariable Long propiedadId,
            @PathVariable Long contratoId) {

        return ResponseEntity.ok(service.finalizar(propiedadId, contratoId));
    }

    // ======================================================
    // SUBIR FIRMA DIGITAL DEL CONTRATO
    // ======================================================
    @PostMapping("/{contratoId}/firma")
    @Operation(
            summary = "Subir firma digital del contrato",
            description = """
                    Sube un archivo de imagen (PNG/JPG) con la firma del inquilino.
                    La firma se almacena como binario en el contrato.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Firma registrada",
            content = @Content(schema = @Schema(implementation = ContratoDetailResponseDto.class))
    )
    public ResponseEntity<ContratoDetailResponseDto> subirFirma(
            @PathVariable Long propiedadId,
            @PathVariable Long contratoId,
            @RequestParam("file") MultipartFile file) throws Exception {

        return ResponseEntity.ok(service.uploadFirma(propiedadId, contratoId, file));
    }

    // ======================================================
    // DESCARGAR / VER FIRMA DIGITAL
    // ======================================================
    @GetMapping(value = "/{contratoId}/firma", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(
            summary = "Obtener firma digital",
            description = "Devuelve la firma digital almacenada del contrato como imagen PNG."
    )
    public ResponseEntity<byte[]> getFirma(
            @PathVariable Long propiedadId,
            @PathVariable Long contratoId) {

        byte[] firma = service.getFirma(propiedadId, contratoId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=firma-contrato.png")
                .contentType(MediaType.IMAGE_PNG)
                .body(firma);
    }

}
