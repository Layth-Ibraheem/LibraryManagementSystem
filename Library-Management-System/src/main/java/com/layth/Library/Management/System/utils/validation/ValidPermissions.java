package com.layth.Library.Management.System.utils.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotated Integer must be -1 (all permissions) or a combination of the
 * {@link com.layth.Library.Management.System.entities.UserRoles} flags. Null is left to @NotNull.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidPermissionsValidator.class)
public @interface ValidPermissions {
    String message() default "must be -1 (all permissions) or a combination of the permission flags 1, 2 and 4";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
