package com.habitora.backend.presentation.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Anotación de validación personalizada para verificar que un campo
 * contenga solo letras, espacios, tildes, ñ y guiones.
 * Útil para validar nombres completos.
 */
@Documented
@Constraint(validatedBy = OnlyLettersValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface OnlyLetters {
    String message() default "El campo solo puede contener letras, espacios y tildes";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
