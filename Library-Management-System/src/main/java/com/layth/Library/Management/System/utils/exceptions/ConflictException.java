package com.layth.Library.Management.System.utils.exceptions;

/**
 * Thrown when a request is valid but breaks a business rule given the current state,
 * for example lending a book that is already on loan. Mapped to 409 Conflict.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
