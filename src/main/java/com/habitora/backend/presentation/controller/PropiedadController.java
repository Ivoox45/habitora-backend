package com.habitora.backend.presentation.controller;

import com.habitora.backend.presentation.dto.propiedad.request.PropiedadCreateRequestDto;
import com.habitora.backend.presentation.dto.propiedad.request.PropiedadUpdateRequestDto;
import com.habitora.backend.presentation.dto.propiedad.response.PropiedadListResponseDto;
import com.habitora.backend.presentation.dto.propiedad.response.PropiedadResponseDto;
import com.habitora.backend.service.interfaces.IPropiedadService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/propiedades")
@Tag(name = "Propiedades", description = "APIs para gestionar propiedades del usuario autenticado")
@RequiredArgsConstructor
public class PropiedadController {

    private final IPropiedadService propiedadService;

    // ==========================
    //   Crear propiedad
    // ==========================
    @Operation(
            summary = "Crear propiedad",
            description = "Crea una nueva propiedad asociada al usuario autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Propiedad creada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PropiedadResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    })
    @PostMapping
    public ResponseEntity<PropiedadResponseDto> create(
            @Valid @RequestBody PropiedadCreateRequestDto request
    ) {
        PropiedadResponseDto response = propiedadService.createForCurrentUser(request);
        return ResponseEntity
                .created(URI.create("/api/propiedades/" + response.getId()))
                .body(response);
    }

    // ==========================
    //   Listar propiedades
    // ==========================
    @Operation(
            summary = "Listar propiedades",
            description = "Obtiene todas las propiedades registradas por el usuario autenticado."
    )
    @ApiResponse(responseCode = "200", description = "Lista de propiedades",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = PropiedadListResponseDto.class))))
    @GetMapping
    public ResponseEntity<List<PropiedadListResponseDto>> findAll() {
        List<PropiedadListResponseDto> list = propiedadService.findAllForCurrentUser();
        return ResponseEntity.ok(list);
    }

    // ==========================
    //   Obtener propiedad por ID
    // ==========================
    @Operation(
            summary = "Obtener propiedad por id",
            description = "Obtiene una propiedad específica del usuario autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Propiedad encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PropiedadResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Propiedad no encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<PropiedadResponseDto> findById(@PathVariable Long id) {
        return propiedadService.findByIdForCurrentUser(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ==========================
    //   Actualizar propiedad
    // ==========================
    @Operation(
            summary = "Actualizar propiedad",
            description = "Actualiza los datos de una propiedad del usuario autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Propiedad actualizada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PropiedadResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Propiedad no encontrada", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<PropiedadResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody PropiedadUpdateRequestDto request
    ) {
        PropiedadResponseDto updated = propiedadService.updateForCurrentUser(id, request);
        return ResponseEntity.ok(updated);
    }

    // ==========================
    //   Eliminar propiedad
    // ==========================
    @Operation(
            summary = "Eliminar propiedad",
            description = "Elimina una propiedad del usuario autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Propiedad eliminada", content = @Content),
            @ApiResponse(responseCode = "404", description = "Propiedad no encontrada", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        propiedadService.deleteForCurrentUser(id);
        return ResponseEntity.noContent().build();
    }
}
