package com.habitora.backend.presentation.controller;

import com.habitora.backend.presentation.dto.habitacion.request.HabitacionCreateRequestDto;
import com.habitora.backend.presentation.dto.habitacion.response.HabitacionResponseDto;
import com.habitora.backend.service.interfaces.IHabitacionService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habitaciones")
@RequiredArgsConstructor
@Tag(name = "Habitaciones", description = "Gestión de habitaciones dentro de una propiedad")
public class HabitacionController {

    private final IHabitacionService habitacionService;

    @PostMapping("/crear-automatico")
    @Operation(summary = "Crear habitaciones automáticas",
            description = "Genera códigos como 101,102,201,202 según el piso.")
    public List<HabitacionResponseDto> crearAutomatico(
            @RequestBody HabitacionCreateRequestDto dto) {
        return habitacionService.createAutomatic(dto);
    }

    @GetMapping("/piso/{pisoId}")
    @Operation(summary = "Listar habitaciones por piso")
    public List<HabitacionResponseDto> listarPorPiso(@PathVariable Long pisoId) {
        return habitacionService.getByPiso(pisoId);
    }
}
