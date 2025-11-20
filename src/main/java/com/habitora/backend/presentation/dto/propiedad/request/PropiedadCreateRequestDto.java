package com.habitora.backend.presentation.dto.propiedad.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropiedadCreateRequestDto {

    @NotBlank(message = "El nombre de la propiedad es obligatorio.")
    @Size(max = 120, message = "El nombre no puede exceder los 120 caracteres.")
    private String nombre;

    @Size(max = 200, message = "La dirección no puede exceder los 200 caracteres.")
    private String direccion;

    @NotNull(message = "La cantidad de pisos es obligatoria.")
    @Min(value = 1, message = "La propiedad debe tener al menos 1 piso.")
    @Max(value = 10, message = "Por ahora la propiedad no puede tener más de 10 pisos.")
    private Integer cantidadPisos;

    @Min(value = 0, message = "El piso de residencia debe ser 0 o positivo.")
    private Integer pisoResidenciaDueno;
}
