package com.layth.Library.Management.System.entities;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRolesTest {
    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 1, 2, 3, 4, 5, 6, 7})
    void acceptsAllPermissionsOrAnyCombinationOfDefinedFlags(int roles) {
        assertTrue(UserRoles.isValidPermissionSet(roles));
    }

    @ParameterizedTest
    @ValueSource(ints = {-2, -100, 8, 9, 15, 16, Integer.MAX_VALUE, Integer.MIN_VALUE})
    void rejectsUndefinedFlagsAndOtherNegativeValues(int roles) {
        assertFalse(UserRoles.isValidPermissionSet(roles));
    }
}
