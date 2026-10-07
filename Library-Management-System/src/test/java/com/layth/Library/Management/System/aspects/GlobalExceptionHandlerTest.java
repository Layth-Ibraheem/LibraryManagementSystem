package com.layth.Library.Management.System.aspects;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

/** The parts of the error contract that no endpoint can trigger on demand. */
class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void anUnexpectedExceptionIsA500WithoutItsMessage() {
        ProblemDetail problem = handler.handleUnexpected(
                new IllegalStateException("SELECT * FROM users failed"), new MockHttpServletRequest("GET", "/api/books"));

        assertThat(problem.getStatus()).isEqualTo(500);
        assertThat(problem.getDetail()).isEqualTo("An unexpected error occurred");
    }

    @Test
    void aConstraintViolationIsA409WithoutTheDatabaseMessage() {
        ProblemDetail problem = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("Unique index or primary key violation: UK_BOOKS_ISBN ON PUBLIC.BOOKS(ISBN)"),
                new MockHttpServletRequest("POST", "/api/books"));

        assertThat(problem.getStatus()).isEqualTo(409);
        assertThat(problem.getDetail()).isEqualTo("The request conflicts with data that already exists");
    }
}
