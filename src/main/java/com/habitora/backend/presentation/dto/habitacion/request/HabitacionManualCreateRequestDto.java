package com.habitora.backend.presentation.dto.habitacion.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HabitacionManualCreateRequestDto {

    @NotNull(message = "El piso es obligatorio.")
    private Long pisoId;

    @NotBlank(message = "El código de la habitación es obligatorio.")
    private String codigo;

    @NotNull(message = "El precio de renta es obligatorio.")
    @PositiveOrZero(message = "El precio de renta no puede ser negativo.")
    private BigDecimal precioRenta;
}
