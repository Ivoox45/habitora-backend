package com.habitora.backend.presentation.dto.pago.request;

import com.habitora.backend.persistence.entity.Pago;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PagoCreateRequestDto {

    @NotNull(message = "La fecha de pago es obligatoria.")
    private LocalDate fechaPago;

    @NotNull(message = "El monto es obligatorio.")
    @Positive(message = "El monto debe ser mayor a cero.")
    private BigDecimal monto;

    @NotNull(message = "El método de pago es obligatorio.")
    private Pago.MetodoPago metodo;
}
