package com.example.demo;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a String field contains a comma-separated list of valid
 * email addresses. Null and blank values are considered valid (use @NotBlank
 * separately if the field is required).
 */
@Documented
@Constraint(validatedBy = EmailListValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEmailList {
    String message() default "must contain valid comma-separated email addresses";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
