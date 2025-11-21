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

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/propiedades/{propiedadId}/contratos")
@RequiredArgsConstructor
@Tag(name = "Contratos", description = "Gestión de contratos dentro de una propiedad")
public class ContratoController {

    private final IContratoService service;

    // ======================================================
    // CREATE
    // ======================================================
    @PostMapping
    @Operation(
            summary = "Crear contrato",
            description = "Crea un contrato con inquilino, habitación y fechas."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Contrato creado",
            content = @Content(schema = @Schema(implementation = ContratoDetailResponseDto.class))
    )
    public ResponseEntity<ContratoDetailResponseDto> create(
            @PathVariable Long propiedadId,
            @RequestBody ContratoCreateRequestDto request) {

        ContratoDetailResponseDto response = service.create(propiedadId, request);

        return ResponseEntity.created(
                URI.create("/api/propiedades/" + propiedadId + "/contratos/" + response.getId())
        ).body(response);
    }

    // ======================================================
    // LISTAR + FILTROS
    // ======================================================
    @GetMapping
    @Operation(
            summary = "Listar contratos",
            description = """
                    Lista los contratos de la propiedad.
                    Filtros opcionales:
                    - estado = ACTIVO / CANCELADO
                    - search = nombre del inquilino o código de habitación
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
    // GET DETAIL
    // ======================================================
    @GetMapping("/{contratoId}")
    @Operation(
            summary = "Obtener detalle de contrato",
            description = "Devuelve detalle de contrato incluyendo habitación e inquilino."
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
    @Operation(summary = "Finalizar contrato", description = "Marca el contrato como CANCELADO y libera la habitación.")
    public ResponseEntity<ContratoDetailResponseDto> finalizar(
            @PathVariable Long propiedadId,
            @PathVariable Long contratoId) {

        return ResponseEntity.ok(service.finalizar(propiedadId, contratoId));
    }

    // ======================================================
    // SUBIR FIRMA DIGITAL
    // ======================================================
    @PostMapping("/{contratoId}/firma")
    @Operation(
            summary = "Subir firma digital del contrato",
            description = "Permite subir un archivo PNG/JPG como firma digital en binario."
    )
    public ResponseEntity<ContratoDetailResponseDto> subirFirma(
            @PathVariable Long propiedadId,
            @PathVariable Long contratoId,
            @RequestParam("file") MultipartFile file) throws Exception {

        return ResponseEntity.ok(service.uploadFirma(propiedadId, contratoId, file));
    }

    // ======================================================
    // DESCARGAR FIRMA DIGITAL
    // ======================================================
    @GetMapping(value = "/{contratoId}/firma", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Obtener firma digital", description = "Devuelve la firma digital almacenada.")
    public ResponseEntity<byte[]> getFirma(
            @PathVariable Long propiedadId,
            @PathVariable Long contratoId) {

        byte[] firma = service.getFirma(propiedadId, contratoId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=firma.png")
                .contentType(MediaType.IMAGE_PNG)
                .body(firma);
    }

}
