package com.layth.Library.Management.System.requestsAndResponses.users;

import com.layth.Library.Management.System.utils.validation.ValidPermissions;
import jakarta.validation.constraints.NotNull;

/**
 * Body of PUT /api/users/{id}/roles: the user's complete new permission set.
 */
public record UpdateUserRolesRequest(@NotNull @ValidPermissions Integer roles) {
}
