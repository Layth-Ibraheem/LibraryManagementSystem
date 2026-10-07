package com.layth.Library.Management.System.utils.validation;

import com.layth.Library.Management.System.entities.UserRoles;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidPermissionsValidator implements ConstraintValidator<ValidPermissions, Integer> {
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return value == null || UserRoles.isValidPermissionSet(value);
    }
}
