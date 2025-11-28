package com.habitora.backend.presentation.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador personalizado que verifica que un String contenga solo:
 * - Letras (A-Z, a-z, incluyendo tildes y ñ)
 * - Espacios
 * - Guiones (- y ')
 */
public class OnlyLettersValidator implements ConstraintValidator<OnlyLetters, String> {

    private static final String LETTERS_PATTERN = "^[a-záéíóúüñA-ZÁÉÍÓÚÜÑ\\s'-]+$";

    @Override
    public void initialize(OnlyLetters constraintAnnotation) {
        // Inicialización si es necesaria
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Null y strings vacíos no se validan aquí (usar @NotBlank para eso)
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        
        return value.matches(LETTERS_PATTERN);
    }
}
