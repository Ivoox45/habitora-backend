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
@Tag(name = "Inquilinos", description = "Gestión de inquilinos por propiedad (multi-propiedad por usuario)")
@RequiredArgsConstructor
public class InquilinoController {

        private final IInquilinoService service;

        @Operation(summary = "Crear un inquilino en la propiedad", description = "Registra un nuevo inquilino dentro de la propiedad indicada en la ruta.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Inquilino creado correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InquilinoResponseDto.class))),
                        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
        })
        @PostMapping
        public ResponseEntity<InquilinoResponseDto> create(
                        @PathVariable Long propiedadId,
                        @Valid @RequestBody InquilinoCreateRequestDto request) {

                InquilinoResponseDto response = service.create(propiedadId, request);

                return ResponseEntity
                                .created(URI.create(
                                                "/api/propiedades/" + propiedadId + "/inquilinos/" + response.getId()))
                                .body(response);
        }

        @Operation(summary = "Listar inquilinos de la propiedad", description = "Devuelve todos los inquilinos registrados dentro de la propiedad seleccionada.")
        @ApiResponse(responseCode = "200", description = "Lista de inquilinos obtenida", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = InquilinoListResponseDto.class))))
        @GetMapping
        public ResponseEntity<List<InquilinoListResponseDto>> list(
                        @PathVariable Long propiedadId) {

                return ResponseEntity.ok(service.findAll(propiedadId));
        }

        @Operation(summary = "Obtener un inquilino por ID", description = "Retorna el inquilino, siempre que pertenezca a esta propiedad.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Inquilino encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InquilinoResponseDto.class))),
                        @ApiResponse(responseCode = "404", description = "Inquilino no pertenece a la propiedad o no existe", content = @Content)
        })
        @GetMapping("/{id}")
        public ResponseEntity<InquilinoResponseDto> findById(
                        @PathVariable Long propiedadId,
                        @PathVariable Long id) {

                return service.findById(propiedadId, id)
                                .map(ResponseEntity::ok)
                                .orElseGet(() -> ResponseEntity.notFound().build());
        }

        @Operation(summary = "Actualizar datos de un inquilino", description = "Permite editar la información de un inquilino si pertenece a esta propiedad.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Inquilino actualizado correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InquilinoResponseDto.class))),
                        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Inquilino no pertenece a la propiedad", content = @Content)
        })
        @PutMapping("/{id}")
        public ResponseEntity<InquilinoResponseDto> update(
                        @PathVariable Long propiedadId,
                        @PathVariable Long id,
                        @Valid @RequestBody InquilinoUpdateRequestDto request) {

                return ResponseEntity.ok(service.update(propiedadId, id, request));
        }

        @Operation(summary = "Eliminar inquilino", description = "Elimina un inquilino, siempre que pertenezca a esta propiedad.")
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "Inquilino eliminado correctamente", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Inquilino no pertenece a la propiedad", content = @Content)
        })
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> delete(
                        @PathVariable Long propiedadId,
                        @PathVariable Long id) {

                service.deleteById(propiedadId, id);
                return ResponseEntity.noContent().build();
        }

        @Operation(summary = "Buscar inquilinos por nombre o DNI", description = "Busca dentro de los inquilinos pertenecientes a la propiedad usando coincidencia parcial.")
        @ApiResponse(responseCode = "200", description = "Lista filtrada de inquilinos", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = InquilinoListResponseDto.class))))
        @GetMapping("/search")
        public ResponseEntity<List<InquilinoListResponseDto>> search(
                        @PathVariable Long propiedadId,
                        @RequestParam(required = false) String query) {

                return ResponseEntity.ok(service.search(propiedadId, query));
        }
}
