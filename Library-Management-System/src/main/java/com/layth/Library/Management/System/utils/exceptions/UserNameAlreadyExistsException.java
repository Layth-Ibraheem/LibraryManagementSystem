package com.layth.Library.Management.System.utils.exceptions;

/**
 * Thrown when someone registers with a user name that is already taken. Mapped to 409 Conflict.
 */
public class UserNameAlreadyExistsException extends ConflictException {
    public UserNameAlreadyExistsException(String userName) {
        super("The user name '" + userName + "' is already taken");
    }
}
