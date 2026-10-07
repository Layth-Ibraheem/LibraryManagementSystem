package com.layth.Library.Management.System.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.layth.Library.Management.System.utils.jwt.JwtRequestFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletContextInitializerBeans;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Runs the real security setup end to end on the h2 profile, with the users from DevUserSeeder.
 * It uses its own in-memory database so it cannot disturb other test contexts.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:auth-flow;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class AuthAndPermissionsFlowTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationContext context;

    @Value("${app.seed.admin-password}")
    private String adminPassword;

    @Value("${app.seed.clerk-password}")
    private String clerkPassword;

    @Test
    void registrationIgnoresClientSuppliedRolesAndGrantsNothing() throws Exception {
        JsonNode registered = body(register("eve", "eve-password-1", ", \"roles\": -1")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roles").value(0)));

        mockMvc.perform(get("/api/books").header("Authorization", bearer(registered)))
                .andExpect(status().isForbidden());
    }

    @Test
    void seededAdminLogsInWithPostAndCanUseTheApi() throws Exception {
        String adminToken = bearer(body(login("admin", adminPassword)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").value(-1))));

        mockMvc.perform(get("/api/books").header("Authorization", adminToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/librarians").header("Authorization", adminToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/patrons").header("Authorization", adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void adminGrantsPermissionsAndTheNextLoginCarriesThem() throws Exception {
        JsonNode bob = body(register("bob", "bob-password-1", "").andExpect(status().isCreated()));
        String adminToken = bearer(body(login("admin", adminPassword).andExpect(status().isOk())));

        mockMvc.perform(put("/api/users/{id}/roles", bob.get("id").asInt())
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\": 2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bob.get("id").asInt()))
                .andExpect(jsonPath("$.userName").value("bob"))
                .andExpect(jsonPath("$.roles").value(2))
                .andExpect(jsonPath("$.password").doesNotExist());

        String bobToken = bearer(body(login("bob", "bob-password-1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").value(2))));
        mockMvc.perform(get("/api/books").header("Authorization", bobToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/patrons").header("Authorization", bobToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void grantingRejectsUndefinedPermissionFlags() throws Exception {
        String adminToken = bearer(body(login("admin", adminPassword).andExpect(status().isOk())));

        mockMvc.perform(put("/api/users/{id}/roles", 1)
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\": 8}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.roles").exists());
    }

    @Test
    void onlyAFullAdminCanGrantPermissions() throws Exception {
        String clerkToken = bearer(body(login("clerk", clerkPassword).andExpect(status().isOk())));

        mockMvc.perform(put("/api/users/{id}/roles", 1)
                        .header("Authorization", clerkToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\": -1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void borrowingAndReturningRequireManagePatrons() throws Exception {
        String adminToken = bearer(body(login("admin", adminPassword).andExpect(status().isOk())));
        String clerkToken = bearer(body(login("clerk", clerkPassword).andExpect(status().isOk())));
        String daveToken = bearer(body(register("dave", "dave-password-1", "").andExpect(status().isCreated())));

        int bookId = body(mockMvc.perform(post("/api/books")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Domain-Driven Design\", \"author\": \"Eric Evans\", \"publicationYear\": 2003, \"isbn\": \"978-0-321-12521-7\"}"))
                .andExpect(status().isCreated())).get("id").asInt();
        int patronId = body(mockMvc.perform(post("/api/patrons")
                        .header("Authorization", clerkToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Patron One\", \"email\": \"patron.one@example.com\", \"phoneNumber\": \"0991234567\"}"))
                .andExpect(status().isCreated())).get("id").asInt();

        mockMvc.perform(post("/api/borrow/{bookId}/patron/{patronId}", bookId, patronId)
                        .header("Authorization", daveToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/borrow/{bookId}/patron/{patronId}", bookId, patronId)
                        .header("Authorization", clerkToken))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/return/{bookId}/patron/{patronId}", bookId, patronId)
                        .header("Authorization", daveToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/return/{bookId}/patron/{patronId}", bookId, patronId)
                        .header("Authorization", clerkToken))
                .andExpect(status().isOk());
    }

    @Test
    void rejectedRequestsDoNotCreateAnHttpSession() throws Exception {
        MvcResult rejected = mockMvc.perform(get("/api/books"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        assertNull(rejected.getRequest().getSession(false), "a stateless token API must not create HTTP sessions");
    }

    @Test
    void noDefaultUserStoreOrUnusedAuthenticationManagerIsRegistered() {
        assertEquals(0, context.getBeanNamesForType(UserDetailsService.class).length);
        assertEquals(0, context.getBeanNamesForType(AuthenticationManager.class).length);
    }

    @Test
    void theJwtFilterRunsOnlyInsideTheSecurityChainNotAsASecondServletFilter() {
        // The filters Spring Boot would register with the servlet container, as it computes them at startup.
        var containerFilters = new ServletContextInitializerBeans(context).stream()
                .filter(FilterRegistrationBean.class::isInstance)
                .map(FilterRegistrationBean.class::cast)
                .filter(FilterRegistrationBean::isEnabled)
                .map(FilterRegistrationBean::getFilter)
                .toList();

        assertThat(containerFilters).isNotEmpty().noneMatch(JwtRequestFilter.class::isInstance);
    }

    @Test
    void loginIgnoresABrokenTokenLeftInTheAuthorizationHeader() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .header("Authorization", "Bearer not-a-valid-jwt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\": \"admin\", \"password\": \"%s\"}".formatted(adminPassword)))
                .andExpect(status().isOk());
    }

    @Test
    void loginRejectsWrongPassword() throws Exception {
        login("admin", "not-the-password").andExpect(status().isUnauthorized());
    }

    @Test
    void registrationRejectsInvalidAndDuplicateRequests() throws Exception {
        register("x", "short", "")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.userName").exists())
                .andExpect(jsonPath("$.errors.password").exists());

        register("carol", "carol-password-1", "").andExpect(status().isCreated());
        register("carol", "carol-password-2", "").andExpect(status().isConflict());
    }

    @Test
    void passwordsLongerThan72BytesAreRejectedInsteadOfTruncatedByBCrypt() throws Exception {
        String seventyTwoBytes = "é".repeat(36); // 36 characters, 2 bytes each in UTF-8

        register("utf8-ines", "é".repeat(72), "")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").value("must be at most 72 bytes in UTF-8"));

        register("utf8-ines", seventyTwoBytes, "").andExpect(status().isCreated());
        login("utf8-ines", seventyTwoBytes).andExpect(status().isOk());
        // Same first 72 bytes, different password: BCrypt alone would accept it.
        login("utf8-ines", seventyTwoBytes + "x".repeat(10)).andExpect(status().isBadRequest());
    }

    private ResultActions register(String userName, String password, String extraJson) throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userName\": \"%s\", \"password\": \"%s\"%s}".formatted(userName, password, extraJson)));
    }

    private ResultActions login(String userName, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userName\": \"%s\", \"password\": \"%s\"}".formatted(userName, password)));
    }

    private JsonNode body(ResultActions result) throws Exception {
        return objectMapper.readTree(result.andReturn().getResponse().getContentAsString());
    }

    private static String bearer(JsonNode authResponse) {
        return "Bearer " + authResponse.get("token").asText();
    }
}
