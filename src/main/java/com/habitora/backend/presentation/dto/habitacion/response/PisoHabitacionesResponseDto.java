package com.habitora.backend.presentation.dto.habitacion.response;

import lombok.Data;

import java.util.List;

@Data
public class PisoHabitacionesResponseDto {

    private Long pisoId;
    private Integer numeroPiso;
    private List<HabitacionResponseDto> habitaciones;
}
