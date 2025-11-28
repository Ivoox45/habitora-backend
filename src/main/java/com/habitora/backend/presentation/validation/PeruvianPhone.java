package com.habitora.backend.presentation.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Anotación de validación personalizada para verificar que un campo
 * contenga un número de teléfono peruano válido (9 dígitos).
 */
@Documented
@Constraint(validatedBy = PeruvianPhoneValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PeruvianPhone {
    String message() default "El número de teléfono debe tener exactamente 9 dígitos";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
