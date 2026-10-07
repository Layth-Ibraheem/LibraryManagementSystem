package com.layth.Library.Management.System.entities;

import java.util.Arrays;

/**
 * Permission flags. A user's {@code roles} column is a bit set of these values,
 * or {@link #AllRoles} (-1) for an administrator who has every permission.
 */
public enum UserRoles {
    AllRoles(-1),
    ManageLibrarians(1),
    ManageBooks(2),
    ManagePatrons(4);

    /** Permission set given to every self-registered user: none. An administrator grants more. */
    public static final int NO_PERMISSIONS = 0;

    private static final int DEFINED_FLAGS = Arrays.stream(values())
            .mapToInt(UserRoles::getRole)
            .filter(role -> role > 0)
            .reduce(0, (all, role) -> all | role);

    private Integer role;

    UserRoles(Integer role){
        this.role = role;
    }

    public Integer getRole() {
        return role;
    }

    /**
     * True for -1 (all permissions) or any combination of the defined flags, including 0 (none).
     */
    public static boolean isValidPermissionSet(int roles) {
        return roles == AllRoles.getRole() || (roles >= 0 && (roles & ~DEFINED_FLAGS) == 0);
    }
}
