package com.habitora.backend.presentation.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador personalizado que verifica que un String sea un número
 * de teléfono peruano válido (exactamente 9 dígitos numéricos).
 */
public class PeruvianPhoneValidator implements ConstraintValidator<PeruvianPhone, String> {

    private static final String PHONE_PATTERN = "^\\d{9}$";

    @Override
    public void initialize(PeruvianPhone constraintAnnotation) {
        // Inicialización si es necesaria
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Null y strings vacíos no se validan aquí (teléfono es opcional)
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        
        return value.trim().matches(PHONE_PATTERN);
    }
}
