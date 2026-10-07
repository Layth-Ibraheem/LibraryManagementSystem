package com.layth.Library.Management.System.controllers;

import com.layth.Library.Management.System.aspects.RoleCheckAspect;
import com.layth.Library.Management.System.config.JwtProperties;
import com.layth.Library.Management.System.config.SecurityConfig;
import com.layth.Library.Management.System.config.SecurityProblemHandler;
import com.layth.Library.Management.System.entities.User;
import com.layth.Library.Management.System.entities.UserRoles;
import com.layth.Library.Management.System.requestsAndResponses.books.AddNewBookRequest;
import com.layth.Library.Management.System.requestsAndResponses.books.BookResponse;
import com.layth.Library.Management.System.services.BookService;
import com.layth.Library.Management.System.utils.jwt.CurrentUser;
import com.layth.Library.Management.System.utils.jwt.CurrentUserProvider;
import com.layth.Library.Management.System.utils.jwt.JwtTokenUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web slice for BookController: MVC, the exception advice and the real security setup
 * (SecurityConfig, JwtRequestFilter and RoleCheckAspect), with BookService mocked.
 * No database and no service beans are started.
 * <p>
 * Most requests are authenticated with spring-security-test's {@code authentication(..)}
 * post-processor, whose principal is a {@link CurrentUser} exactly like the one JwtRequestFilter
 * builds. One test sends a real signed token to show the filter accepts it.
 */
@WebMvcTest(controllers = BookController.class,
        // Same exclusion as the application class: no default in-memory user in the slice either.
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@Import({SecurityConfig.class, SecurityProblemHandler.class, CurrentUserProvider.class, JwtTokenUtils.class,
        RoleCheckAspect.class})
class BookControllerTest {
    private static final String VALID_BOOK = """
            {"title": "Domain-Driven Design", "author": "Eric Evans",
             "publicationYear": 2003, "isbn": "978-0-321-12521-7"}
            """;

    /** The slice does not load @Aspect beans, AOP proxying or configuration properties on its own. */
    @TestConfiguration
    @ImportAutoConfiguration(AopAutoConfiguration.class)
    @EnableConfigurationProperties(JwtProperties.class)
    static class SliceConfiguration {
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenUtils jwtTokenUtils;

    @MockBean
    private BookService bookService;

    @Test
    void createReturns201WithLocationAndTheNewBook() throws Exception {
        when(bookService.addNewBook(any(AddNewBookRequest.class), eq(7))).thenReturn(new BookResponse(42,
                "Domain-Driven Design", "Eric Evans", 2003, "9780321125217", LocalDate.of(2026, 10, 7), false, 7));

        mockMvc.perform(post("/api/books")
                        .with(userWith(7, UserRoles.ManageBooks))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BOOK))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/api/books/42"))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.isbn").value("9780321125217"))
                .andExpect(jsonPath("$.borrowed").value(false))
                .andExpect(jsonPath("$.addedByUserId").value(7));

        // The id of the user who adds the book comes from the token, not from the request body.
        verify(bookService).addNewBook(argThat(request -> request.getIsbn().equals("978-0-321-12521-7")), eq(7));
    }

    @Test
    void invalidBodyReturns400ProblemDetailWithOneMessagePerField() throws Exception {
        mockMvc.perform(post("/api/books")
                        .with(userWith(7, UserRoles.ManageBooks))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "", "author": "Eric Evans", "publicationYear": 1200, "isbn": "978-0-321-12521-0"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("The request has invalid fields"))
                .andExpect(jsonPath("$.instance").value("/api/books"))
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.publicationYear").exists())
                .andExpect(jsonPath("$.errors.isbn").exists())
                .andExpect(jsonPath("$.errors.author").doesNotExist());

        verifyNoInteractions(bookService);
    }

    @Test
    void userWithoutManageBooksGets403ProblemDetail() throws Exception {
        mockMvc.perform(post("/api/books")
                        .with(userWith(8, UserRoles.ManagePatrons))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BOOK))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(403));

        verifyNoInteractions(bookService);
    }

    @Test
    void requestWithoutTokenGets401FromTheSecurityChain() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BOOK))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));

        verifyNoInteractions(bookService);
    }

    @Test
    void realBearerTokenIsAcceptedByTheJwtFilter() throws Exception {
        String token = jwtTokenUtils.generateToken(new User(9, "librarian", "unused", UserRoles.ManageBooks.getRole()));
        when(bookService.addNewBook(any(AddNewBookRequest.class), eq(9))).thenReturn(new BookResponse(43,
                "Domain-Driven Design", "Eric Evans", 2003, "9780321125217", LocalDate.of(2026, 10, 7), false, 9));

        mockMvc.perform(post("/api/books")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BOOK))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.addedByUserId").value(9));
    }

    private static RequestPostProcessor userWith(int id, UserRoles permission) {
        CurrentUser principal = new CurrentUser(id, "user-" + id, permission.getRole());
        return authentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }
}
