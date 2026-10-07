package com.layth.Library.Management.System.utils.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotated Integer is a year that is not after the current year. Null is left to @NotNull.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotFutureYearValidator.class)
public @interface NotFutureYear {
    String message() default "must not be in the future";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
