package com.habitora.backend.presentation.controller;

import com.habitora.backend.persistence.entity.Piso;
import com.habitora.backend.persistence.entity.Propiedad;
import com.habitora.backend.persistence.repository.PisoRepository;
import com.habitora.backend.persistence.repository.PropiedadRepository;
import com.habitora.backend.presentation.dto.piso.response.PisoListResponseDto;
import com.habitora.backend.util.mapper.PisoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/propiedades/{propiedadId}/pisos")
@Tag(name = "Pisos", description = "APIs para gestionar los pisos de una propiedad")
@RequiredArgsConstructor
public class PisoController {

    private final PropiedadRepository propiedadRepository;
    private final PisoRepository pisoRepository;
    private final PisoMapper pisoMapper;

    @Operation(
            summary = "Listar pisos de una propiedad",
            description = "Obtiene todos los pisos asociados a una propiedad del usuario autenticado"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lista de pisos",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = PisoListResponseDto.class))
            )
    )
    @GetMapping
    public ResponseEntity<List<PisoListResponseDto>> listarPisosDePropiedad(
            @PathVariable Long propiedadId,
            @AuthenticationPrincipal com.habitora.backend.persistence.entity.Usuario usuarioActual
    ) {
        // Verificar que la propiedad pertenezca al usuario logueado
        Propiedad propiedad = propiedadRepository.findById(propiedadId)
                .orElseThrow(() -> new IllegalArgumentException("Propiedad no encontrada"));

        if (!propiedad.getUsuario().getId().equals(usuarioActual.getId())) {
            return ResponseEntity.status(403).build(); // Forbiden
        }

        List<Piso> pisos = pisoRepository.findByPropiedadOrderByNumeroPisoAsc(propiedad);

        List<PisoListResponseDto> response = pisos.stream()
                .map(pisoMapper::toListResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
