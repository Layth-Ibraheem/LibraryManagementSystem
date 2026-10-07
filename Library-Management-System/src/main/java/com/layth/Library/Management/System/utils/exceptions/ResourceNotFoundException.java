package com.layth.Library.Management.System.utils.exceptions;

/**
 * Thrown when a requested entity does not exist. Mapped to 404 Not Found.
 * <p>
 * It is unchecked on purpose: callers cannot recover from it, and a checked exception
 * would not roll back a surrounding {@code @Transactional} method by default.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
