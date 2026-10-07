package com.layth.Library.Management.System.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Every error the API returns is an RFC 9457 problem detail with the right status code.
 * It shares the cached context (and the auth-flow database) of AuthAndPermissionsFlowTest.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:auth-flow;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class ErrorResponsesTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.seed.admin-password}")
    private String adminPassword;

    @Value("${app.seed.clerk-password}")
    private String clerkPassword;

    private String adminToken;

    @BeforeEach
    void logInAsAdmin() throws Exception {
        adminToken = token("admin", adminPassword);
    }

    @Test
    void unknownBookIsANotFoundProblem() throws Exception {
        problem(mockMvc.perform(get("/api/books/{id}", 999_999).header(HttpHeaders.AUTHORIZATION, adminToken)), 404)
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("There is no book with id 999999"))
                .andExpect(jsonPath("$.instance").value("/api/books/999999"));
    }

    @Test
    void unknownUrlIsANotFoundProblem() throws Exception {
        problem(mockMvc.perform(get("/api/no-such-endpoint").header(HttpHeaders.AUTHORIZATION, adminToken)), 404)
                .andExpect(jsonPath("$.detail").value("No endpoint GET /api/no-such-endpoint"));
    }

    @Test
    void invalidFieldsAreABadRequestProblemWithOneMessagePerField() throws Exception {
        problem(postBook("{\"title\": \"\", \"author\": \"A\", \"publicationYear\": 3000, \"isbn\": \"123\"}"), 400)
                .andExpect(jsonPath("$.detail").value("The request has invalid fields"))
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.author").exists())
                .andExpect(jsonPath("$.errors.publicationYear").exists())
                .andExpect(jsonPath("$.errors.isbn").exists());
    }

    @Test
    void malformedJsonIsABadRequestProblemWithoutParserDetails() throws Exception {
        problem(postBook("{\"title\": "), 400)
                .andExpect(content().string(not(containsString("JSON parse error"))))
                .andExpect(content().string(not(containsString("com.fasterxml"))));
    }

    @Test
    void aPathVariableOfTheWrongTypeIsABadRequestProblem() throws Exception {
        problem(mockMvc.perform(get("/api/books/abc").header(HttpHeaders.AUTHORIZATION, adminToken)), 400)
                .andExpect(content().string(not(containsString("java.lang"))));
    }

    @Test
    void aWrongMethodIsAMethodNotAllowedProblem() throws Exception {
        problem(mockMvc.perform(get("/api/auth/login")), 405)
                .andExpect(header().string(HttpHeaders.ALLOW, containsString("POST")));
    }

    @Test
    void aWrongContentTypeIsAnUnsupportedMediaTypeProblem() throws Exception {
        problem(mockMvc.perform(post("/api/books")
                .header(HttpHeaders.AUTHORIZATION, adminToken)
                .contentType(MediaType.TEXT_PLAIN)
                .content("Dune")), 415);
    }

    @Test
    void aMissingPermissionIsAForbiddenProblem() throws Exception {
        String clerkToken = token("clerk", clerkPassword);

        problem(mockMvc.perform(get("/api/books").header(HttpHeaders.AUTHORIZATION, clerkToken)), 403)
                .andExpect(jsonPath("$.detail").value("You do not have permission to perform this action"));
    }

    @Test
    void aTakenUserNameIsAConflictProblem() throws Exception {
        register("errors-frank").andExpect(status().isCreated());

        problem(register("errors-frank"), 409)
                .andExpect(jsonPath("$.detail").value("The user name 'errors-frank' is already taken"));
    }

    private ResultActions problem(ResultActions result, int status) throws Exception {
        return result
                .andExpect(status().is(status))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(status))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.detail").exists());
    }

    private ResultActions postBook(String json) throws Exception {
        return mockMvc.perform(post("/api/books")
                .header(HttpHeaders.AUTHORIZATION, adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private ResultActions register(String userName) throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userName\": \"%s\", \"password\": \"long-enough-password\"}".formatted(userName)));
    }

    private String token(String userName, String password) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\": \"%s\", \"password\": \"%s\"}".formatted(userName, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + objectMapper.readTree(body).get("token").asText();
    }
}
