package com.layth.Library.Management.System.requestsAndResponses;

import com.layth.Library.Management.System.requestsAndResponses.auth.LoginRequest;
import com.layth.Library.Management.System.requestsAndResponses.auth.RegisterRequest;
import com.layth.Library.Management.System.requestsAndResponses.books.AddNewBookRequest;
import com.layth.Library.Management.System.requestsAndResponses.books.UpdateBookRequest;
import com.layth.Library.Management.System.requestsAndResponses.librarians.AddNewLibrarianRequest;
import com.layth.Library.Management.System.requestsAndResponses.patrons.AddNewPatronRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Year;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Plain Bean Validation checks of the request DTOs: no Spring context needed.
 */
class RequestValidationTest {
    private static final String DUNE_ISBN = "978-0-441-17271-9";
    private static final int NEXT_YEAR = Year.now().getValue() + 1;

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    @Test
    void shortRealTitlesAreAccepted() {
        assertThat(invalidFields(new AddNewBookRequest("Dune", "Frank Herbert", 1965, DUNE_ISBN))).isEmpty();
        assertThat(invalidFields(new AddNewBookRequest("It", "Stephen King", 1986, DUNE_ISBN))).isEmpty();
    }

    @Test
    void titlesLongerThanTheColumnAreRejected() {
        String title201 = "x".repeat(201);

        assertThat(invalidFields(new AddNewBookRequest(title201, "Frank Herbert", 1965, DUNE_ISBN))).containsExactly("title");
        assertThat(invalidFields(new UpdateBookRequest(title201, "Frank Herbert", 1965))).containsExactly("title");
    }

    @Test
    void aMissingPublicationYearIsAValidationErrorNotACrash() {
        assertThat(invalidFields(new AddNewBookRequest("Dune", "Frank Herbert", null, DUNE_ISBN))).containsExactly("publicationYear");
        assertThat(invalidFields(new UpdateBookRequest("Dune", "Frank Herbert", null))).containsExactly("publicationYear");
    }

    @Test
    void futureYearsAreRejectedOnCreateAndOnUpdate() {
        assertThat(invalidFields(new AddNewBookRequest("Dune", "Frank Herbert", NEXT_YEAR, DUNE_ISBN))).containsExactly("publicationYear");
        assertThat(invalidFields(new UpdateBookRequest("Dune", "Frank Herbert", NEXT_YEAR))).containsExactly("publicationYear");
        assertThat(invalidFields(new UpdateBookRequest("Dune", "Frank Herbert", Year.now().getValue()))).isEmpty();
    }

    @Test
    void shortNamesAreAccepted() {
        assertThat(invalidFields(new AddNewLibrarianRequest("Ali", "Lee"))).isEmpty();
        assertThat(invalidFields(new AddNewPatronRequest("Ali", "ali@example.com", "0991234567"))).isEmpty();
        assertThat(invalidFields(new AddNewLibrarianRequest("A", "Lee"))).containsExactly("firstName");
    }

    @Test
    void patronsNeedAValidEmailAndAPhoneNumber() {
        assertThat(invalidFields(new AddNewPatronRequest("Ali", "not-an-email", "0991234567"))).containsExactly("email");
        assertThat(invalidFields(new AddNewPatronRequest("Ali", "ali@example.com", null))).containsExactly("phoneNumber");
        assertThat(invalidFields(new AddNewPatronRequest("Ali", "ali@example.com", "call me"))).containsExactly("phoneNumber");
        assertThat(invalidFields(new AddNewPatronRequest("Ali", "ali@example.com", "+963 991-234-567"))).isEmpty();
    }

    @Test
    void passwordsAreLimitedTo72BytesNot72Characters() {
        String e = "é"; // 2 bytes in UTF-8

        assertThat(invalidFields(new RegisterRequest("ines", "x".repeat(72)))).isEmpty();
        assertThat(invalidFields(new RegisterRequest("ines", "x".repeat(73)))).containsExactly("password");
        assertThat(invalidFields(new RegisterRequest("ines", e.repeat(36)))).isEmpty();
        assertThat(invalidFields(new RegisterRequest("ines", e.repeat(37)))).containsExactly("password");
        assertThat(invalidFields(new RegisterRequest("ines", "x".repeat(7)))).containsExactly("password");

        assertThat(invalidFields(new LoginRequest("ines", e.repeat(36)))).isEmpty();
        assertThat(invalidFields(new LoginRequest("ines", e.repeat(36) + "x"))).containsExactly("password");
    }

    private static Set<String> invalidFields(Object request) {
        Set<ConstraintViolation<Object>> violations = validator.validate(request);
        return violations.stream().map(violation -> violation.getPropertyPath().toString()).collect(Collectors.toSet());
    }
}
