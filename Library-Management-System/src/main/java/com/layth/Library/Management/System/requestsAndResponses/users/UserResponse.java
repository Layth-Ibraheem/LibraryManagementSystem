package com.layth.Library.Management.System.requestsAndResponses.users;

import com.layth.Library.Management.System.entities.User;

/**
 * A user as the API shows it: never includes the password hash.
 */
public record UserResponse(Integer id, String userName, Integer roles) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUserName(), user.getRoles());
    }
}
