package com.habitora.backend.presentation.controller;

import com.habitora.backend.persistence.entity.Habitacion;
import com.habitora.backend.presentation.dto.habitacion.request.HabitacionCreateRequestDto;
import com.habitora.backend.presentation.dto.habitacion.request.HabitacionManualCreateRequestDto;
import com.habitora.backend.presentation.dto.habitacion.request.HabitacionUpdateRequestDto;
import com.habitora.backend.presentation.dto.habitacion.response.HabitacionResponseDto;
import com.habitora.backend.presentation.dto.habitacion.response.PisoHabitacionesResponseDto;
import com.habitora.backend.service.interfaces.IHabitacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Habitaciones", description = "Gestión de habitaciones dentro de una propiedad")
public class HabitacionController {

        private final IHabitacionService habitacionService;

        // =====================================================
        // ENDPOINTS "LEGACY" / GENERALES (por piso)
        // =====================================================

        @PostMapping("/habitaciones/crear-automatico")
        @Operation(summary = "Crear habitaciones automáticas", description = "Genera códigos como 101, 102, 201, 202 según el piso.")
        public List<HabitacionResponseDto> crearAutomatico(
                        @Valid @RequestBody HabitacionCreateRequestDto dto) {
                return habitacionService.createAutomatic(dto);
        }

        @GetMapping("/habitaciones/piso/{pisoId}")
        @Operation(summary = "Listar habitaciones por piso")
        public List<HabitacionResponseDto> listarPorPiso(@PathVariable Long pisoId) {
                return habitacionService.getByPiso(pisoId);
        }

        // =====================================================
        // ENDPOINTS PRINCIPALES POR PROPIEDAD
        // =====================================================

        @GetMapping("/propiedades/{propiedadId}/habitaciones")
        @Operation(summary = "Listar habitaciones agrupadas por pisos", description = """
                        Devuelve todas las habitaciones de una propiedad,
                        agrupadas por piso.

                        Filtros opcionales:
                        - estado: DISPONIBLE u OCUPADA
                        - search: código de habitación (empieza con ...)
                        - requierePrecio: true=solo con precio, false=solo sin precio
                        """)
        public ResponseEntity<List<PisoHabitacionesResponseDto>> listarHabitaciones(
                        @PathVariable Long propiedadId,
                        @RequestParam(required = false) Habitacion.EstadoHabitacion estado,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) Boolean requierePrecio) {

                List<PisoHabitacionesResponseDto> response = habitacionService.getByPropiedadGroupedByPiso(
                                propiedadId,
                                estado,
                                search,
                                requierePrecio);

                return ResponseEntity.ok(response);
        }

        @PostMapping("/propiedades/{propiedadId}/habitaciones/manual")
        @Operation(summary = "Crear una habitación manualmente")
        public ResponseEntity<HabitacionResponseDto> crearManual(
                        @PathVariable Long propiedadId,
                        @Valid @RequestBody HabitacionManualCreateRequestDto dto) {

                HabitacionResponseDto response = habitacionService.createManual(propiedadId, dto);

                return ResponseEntity.ok(response);
        }

        @PutMapping("/propiedades/{propiedadId}/habitaciones/{habitacionId}")
        @Operation(summary = "Actualizar habitación")
        public ResponseEntity<HabitacionResponseDto> actualizar(
                        @PathVariable Long propiedadId,
                        @PathVariable Long habitacionId,
                        @Valid @RequestBody HabitacionUpdateRequestDto dto) {

                HabitacionResponseDto updated = habitacionService.update(propiedadId, habitacionId, dto);

                return ResponseEntity.ok(updated);
        }

        @DeleteMapping("/propiedades/{propiedadId}/habitaciones/{habitacionId}")
        @Operation(summary = "Eliminar habitación")
        public ResponseEntity<Void> eliminar(
                        @PathVariable Long propiedadId,
                        @PathVariable Long habitacionId) {

                habitacionService.delete(propiedadId, habitacionId);
                return ResponseEntity.noContent().build();
        }
}
