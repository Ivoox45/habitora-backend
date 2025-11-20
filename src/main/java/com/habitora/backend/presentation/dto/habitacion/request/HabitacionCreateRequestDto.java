package com.habitora.backend.presentation.dto.habitacion.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HabitacionCreateRequestDto {

    @NotNull(message = "El ID del piso es obligatorio.")
    private Long pisoId;

    @Min(value = 1, message = "Debe crear al menos 1 habitación.")
    @Max(value = 8, message = "No puede crear más de 8 habitaciones por piso.")
    private Integer cantidadHabitaciones;
}
