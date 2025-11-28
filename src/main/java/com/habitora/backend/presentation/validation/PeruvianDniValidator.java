package com.habitora.backend.presentation.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador personalizado que verifica que un String sea un DNI
 * peruano válido (exactamente 8 dígitos numéricos).
 */
public class PeruvianDniValidator implements ConstraintValidator<PeruvianDni, String> {

    private static final String DNI_PATTERN = "^\\d{8}$";

    @Override
    public void initialize(PeruvianDni constraintAnnotation) {
        // Inicialización si es necesaria
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Null y strings vacíos no se validan aquí (usar @NotBlank para eso)
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        
        return value.trim().matches(DNI_PATTERN);
    }
}
