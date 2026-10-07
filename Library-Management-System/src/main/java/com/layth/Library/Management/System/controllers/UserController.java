package com.layth.Library.Management.System.controllers;

import com.layth.Library.Management.System.entities.UserRoles;
import com.layth.Library.Management.System.requestsAndResponses.users.UpdateUserRolesRequest;
import com.layth.Library.Management.System.requestsAndResponses.users.UserResponse;
import com.layth.Library.Management.System.services.UserService;
import com.layth.Library.Management.System.utils.annotations.RequireRole;
import com.layth.Library.Management.System.utils.exceptions.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Replaces a user's permission set. Only a full administrator (roles = -1) may call it;
     * any narrower guard would let that permission holder grant itself everything.
     * The change applies to tokens issued after it, so the user must log in again.
     */
    @PutMapping("/{id}/roles")
    @RequireRole(role = UserRoles.AllRoles)
    public ResponseEntity<UserResponse> updateRoles(@PathVariable(name = "id") Integer id,
                                                    @Valid @RequestBody UpdateUserRolesRequest request) throws ResourceNotFoundException {
        return ResponseEntity.ok(UserResponse.from(userService.updateRoles(id, request.roles())));
    }
}
