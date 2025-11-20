package com.habitora.backend.presentation.dto.habitacion.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HabitacionListResponseDto {

    private Long id;
    private Long pisoId;
    private String codigo;
    private String estado;
}
