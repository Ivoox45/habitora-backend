package com.habitora.backend.presentation.controller;

import com.habitora.backend.integration.DniLookupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dni")
@RequiredArgsConstructor
@Tag(name = "DNI Lookup", description = "Consulta de datos por DNI desde RENIEC")
public class DniLookupController {

    private final DniLookupService dniLookupService;

    @GetMapping("/lookup")
    @Operation(
        summary = "Buscar nombre por DNI",
        description = "Consulta el nombre completo de una persona desde RENIEC usando su DNI"
    )
    public ResponseEntity<Map<String, String>> lookupByDni(@RequestParam String dni) {
        String nombreCompleto = dniLookupService.getNombrePorDni(dni);
        
        return ResponseEntity.ok(Map.of(
            "dni", dni,
            "nombreCompleto", nombreCompleto != null ? nombreCompleto : ""
        ));
    }
}
