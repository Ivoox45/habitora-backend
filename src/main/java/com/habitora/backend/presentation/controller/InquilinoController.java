package com.habitora.backend.presentation.controller;

import com.habitora.backend.presentation.dto.inquilino.request.InquilinoCreateRequestDto;
import com.habitora.backend.presentation.dto.inquilino.request.InquilinoUpdateRequestDto;
import com.habitora.backend.presentation.dto.inquilino.response.InquilinoListResponseDto;
import com.habitora.backend.presentation.dto.inquilino.response.InquilinoResponseDto;
import com.habitora.backend.service.interfaces.IInquilinoService;
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
@RequestMapping("/api/inquilinos")
@Tag(name = "Inquilinos", description = "APIs para gestionar inquilinos")
@RequiredArgsConstructor
public class InquilinoController {

    private final IInquilinoService inquilinoService;

    @Operation(summary = "Crear inquilino")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Inquilino creado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InquilinoResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    @PostMapping
    public ResponseEntity<InquilinoResponseDto> create(
            @Valid @RequestBody InquilinoCreateRequestDto request) {
        InquilinoResponseDto response = inquilinoService.create(request);
        return ResponseEntity
                .created(URI.create("/api/inquilinos/" + response.getId()))
                .body(response);
    }

    @Operation(summary = "Listar inquilinos")
    @ApiResponse(responseCode = "200", description = "Lista de inquilinos", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = InquilinoListResponseDto.class))))
    @GetMapping
    public ResponseEntity<List<InquilinoListResponseDto>> findAll() {
        return ResponseEntity.ok(inquilinoService.findAll());
    }

    @Operation(summary = "Obtener inquilino por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inquilino encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InquilinoResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Inquilino no encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<InquilinoResponseDto> findById(@PathVariable Long id) {
        return inquilinoService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Actualizar inquilino")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inquilino actualizado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InquilinoResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Inquilino no encontrado", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<InquilinoResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody InquilinoUpdateRequestDto request) {
        InquilinoResponseDto updated = inquilinoService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Eliminar inquilino")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Inquilino eliminado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Inquilino no encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        inquilinoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar inquilinos por nombre o DNI (búsqueda parcial)")
    public ResponseEntity<List<InquilinoListResponseDto>> search(
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(inquilinoService.search(query));
    }

}
