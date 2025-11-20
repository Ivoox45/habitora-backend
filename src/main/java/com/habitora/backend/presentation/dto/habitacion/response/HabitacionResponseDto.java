package com.habitora.backend.presentation.dto.habitacion.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HabitacionResponseDto {

    private Long id;
    private Long propiedadId;
    private Long pisoId;
    private String codigo;
    private String estado;
    private String precioRenta;
}
