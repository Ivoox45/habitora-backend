package com.habitora.backend.presentation.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Anotación de validación personalizada para verificar que un campo
 * contenga un DNI peruano válido (8 dígitos).
 */
@Documented
@Constraint(validatedBy = PeruvianDniValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PeruvianDni {
    String message() default "El DNI debe tener exactamente 8 dígitos numéricos";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
