package com.layth.Library.Management.System.utils.exceptions;

/**
 * Thrown when a login does not match a user name and password. Mapped to 401 Unauthorized.
 * The message never says which of the two was wrong.
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Invalid user name or password");
    }
}
