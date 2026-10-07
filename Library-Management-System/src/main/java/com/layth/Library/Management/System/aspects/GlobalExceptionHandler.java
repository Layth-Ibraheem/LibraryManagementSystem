package com.layth.Library.Management.System.aspects;

import com.layth.Library.Management.System.utils.exceptions.ConflictException;
import com.layth.Library.Management.System.utils.exceptions.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.TreeMap;

/**
 * Turns every exception raised while a controller handles a request into an RFC 9457
 * problem detail ({@code application/problem+json}).
 * <p>
 * Extending {@link ResponseEntityExceptionHandler} keeps Spring MVC's own status codes:
 * 400 for malformed JSON or a path variable of the wrong type, 404 for an unknown URL,
 * 405 for a wrong method and 415 for a wrong content type. Because the base class already
 * handles {@link MethodArgumentNotValidException}, validation errors are customised by
 * overriding {@link #handleMethodArgumentNotValid}; a second {@code @ExceptionHandler}
 * for the same exception would make the application fail at startup.
 * <p>
 * Errors raised in the security filters never reach this class; the authentication entry
 * point and the access denied handler in the security configuration answer those.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 400 with one message per invalid field under the {@code errors} property. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new TreeMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.merge(error.getField(), String.valueOf(error.getDefaultMessage()), (first, next) -> first + "; " + next);
        }
        ProblemDetail problem = ex.getBody();
        problem.setDetail("The request has invalid fields");
        problem.setProperty("errors", errors);
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    /** 404 for a URL no controller maps; the default text would talk about a "static resource". */
    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException ex, HttpHeaders headers,
                                                                    HttpStatusCode status, WebRequest request) {
        ProblemDetail problem = ex.getBody();
        problem.setDetail("No endpoint " + ex.getHttpMethod() + " /" + ex.getResourcePath());
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * A database constraint rejected the write. The services check uniqueness and loan history
     * first, so this only happens when two requests race (two books with the same ISBN, or a loan
     * created while its patron is being deleted). Request validation mirrors the NOT NULL and
     * length rules of the columns, so a bad input is answered with 400 before it gets here.
     * The database message names tables and constraints, so it is logged, not returned.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Constraint violation for {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "The request conflicts with data that already exists");
    }

    /** Thrown by RoleCheckAspect when the caller's token lacks the permission an endpoint requires. */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "You do not have permission to perform this action");
    }

    /**
     * Anything else is a bug or an infrastructure failure: log it with its stack trace and
     * answer 500 with a fixed text, so no exception message, class name or SQL reaches the client.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception for {} {}", request.getMethod(), request.getRequestURI(), ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }
}
