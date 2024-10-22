package com.layth.Library.Management.System.utils.annotations;

import com.layth.Library.Management.System.entities.UserRoles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequireRole {
    UserRoles role();
}
