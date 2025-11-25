package com.habitora.backend.presentation.controller;

import com.habitora.backend.presentation.dto.inquilino.request.InquilinoCreateRequestDto;
import com.habitora.backend.presentation.dto.inquilino.request.InquilinoUpdateRequestDto;
import com.habitora.backend.presentation.dto.inquilino.response.InquilinoListResponseDto;
import com.habitora.backend.presentation.dto.inquilino.response.InquilinoResponseDto;
import com.habitora.backend.service.interfaces.IInquilinoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/propiedades/{propiedadId}/inquilinos")
@RequiredArgsConstructor
@Tag(name = "Inquilinos", description = "Gestión de inquilinos por propiedad")
public class InquilinoController {

        private final IInquilinoService service;

        // ==========================================================
        // CREATE
        // ==========================================================
        @PostMapping
        @Operation(summary = "Crear un inquilino en una propiedad", description = "Crea un nuevo inquilino dentro de la propiedad seleccionada.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Inquilino creado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InquilinoResponseDto.class))),
                        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
        })
        public ResponseEntity<InquilinoResponseDto> create(
                        @PathVariable Long propiedadId,
                        @Valid @RequestBody InquilinoCreateRequestDto request) {

                InquilinoResponseDto response = service.create(propiedadId, request);

                return ResponseEntity.created(
                                URI.create("/api/propiedades/" + propiedadId + "/inquilinos/" + response.getId()))
                                .body(response);
        }

        // ==========================================================
        // FIND ALL (con filtros)
        // ==========================================================
        @GetMapping
        @Operation(summary = "Listar inquilinos de una propiedad", description = """
                        Devuelve todos los inquilinos de la propiedad.

                        Filtros opcionales:
                        - disponibles = true → Inquilinos sin contrato ACTIVO
                        - query = texto → Buscar por nombre o DNI (startsWith)
                        """)
        @ApiResponse(responseCode = "200", description = "Lista de inquilinos", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = InquilinoListResponseDto.class))))
        public ResponseEntity<List<InquilinoListResponseDto>> list(
                        @PathVariable Long propiedadId,
                        @RequestParam(required = false) Boolean disponibles,
                        @RequestParam(required = false) String query) {

                return ResponseEntity.ok(
                                service.findAll(propiedadId, disponibles, query));
        }

        // ==========================================================
        // FIND BY ID
        // ==========================================================
        @GetMapping("/{id}")
        @Operation(summary = "Obtener un inquilino por ID", description = "Obtiene los detalles del inquilino dentro de la propiedad seleccionada.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Inquilino encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InquilinoResponseDto.class))),
                        @ApiResponse(responseCode = "404", description = "Inquilino no encontrado", content = @Content)
        })
        public ResponseEntity<InquilinoResponseDto> findById(
                        @PathVariable Long propiedadId,
                        @PathVariable Long id) {

                return service.findById(propiedadId, id)
                                .map(ResponseEntity::ok)
                                .orElseGet(() -> ResponseEntity.notFound().build());
        }

        // ==========================================================
        // UPDATE
        // ==========================================================
        @PutMapping("/{id}")
        @Operation(summary = "Actualizar un inquilino", description = "Modifica los datos del inquilino dentro de la propiedad.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Inquilino actualizado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InquilinoResponseDto.class))),
                        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Inquilino no encontrado", content = @Content)
        })
        public ResponseEntity<InquilinoResponseDto> update(
                        @PathVariable Long propiedadId,
                        @PathVariable Long id,
                        @Valid @RequestBody InquilinoUpdateRequestDto request) {

                return ResponseEntity.ok(service.update(propiedadId, id, request));
        }

        // ==========================================================
        // DELETE
        // ==========================================================
        @DeleteMapping("/{id}")
        @Operation(summary = "Eliminar un inquilino", description = "Elimina permanentemente el registro del inquilino en la propiedad.")
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "Inquilino eliminado"),
                        @ApiResponse(responseCode = "404", description = "Inquilino no encontrado")
        })
        public ResponseEntity<Void> delete(
                        @PathVariable Long propiedadId,
                        @PathVariable Long id) {

                service.deleteById(propiedadId, id);
                return ResponseEntity.noContent().build();
        }

}
