package com.layth.Library.Management.System.controllers;

import com.layth.Library.Management.System.config.JwtProperties;
import com.layth.Library.Management.System.entities.User;
import com.layth.Library.Management.System.entities.UserRoles;
import com.layth.Library.Management.System.utils.jwt.JwtTokenUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Duration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Requests without a usable token get 401 with a problem detail and a Bearer challenge,
 * never 403 and never an exception escaping the JWT filter.
 * It shares the cached context (and the auth-flow database) of AuthAndPermissionsFlowTest.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:auth-flow;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class AuthenticationErrorsTest {
    private static final String MISSING = "Authentication is required: send 'Authorization: Bearer <token>' from POST /api/auth/login";
    private static final String REJECTED = "The access token is invalid or has expired";

    @Autowired
    private MockMvc mockMvc;

    @Value("${jwt.secret}")
    private String secret;

    private final User admin = new User(1, "admin", "", UserRoles.AllRoles.getRole());

    @Test
    void aRequestWithoutATokenIsUnauthorized() throws Exception {
        unauthorized(mockMvc.perform(get("/api/books")), MISSING)
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"));
    }

    @Test
    void anotherAuthorizationSchemeIsTreatedLikeNoToken() throws Exception {
        unauthorized(mockMvc.perform(get("/api/books").header(HttpHeaders.AUTHORIZATION, "Basic YWRtaW46YWRtaW4=")), MISSING);
    }

    @Test
    void anUnknownUrlWithoutATokenIsUnauthorizedRatherThanNotFound() throws Exception {
        unauthorized(mockMvc.perform(get("/api/no-such-endpoint")), MISSING);
    }

    @Test
    void aMalformedTokenIsUnauthorized() throws Exception {
        rejected(mockMvc.perform(get("/api/books").header(HttpHeaders.AUTHORIZATION, "Bearer abc.def.ghi")));
    }

    @Test
    void anEmptyTokenIsUnauthorized() throws Exception {
        rejected(mockMvc.perform(get("/api/books").header(HttpHeaders.AUTHORIZATION, "Bearer ")));
    }

    @Test
    void anExpiredTokenIsUnauthorized() throws Exception {
        String expired = new JwtTokenUtils(new JwtProperties(secret, Duration.ofMinutes(-1))).generateToken(admin);

        rejected(mockMvc.perform(get("/api/books").header(HttpHeaders.AUTHORIZATION, "Bearer " + expired)));
    }

    @Test
    void aTokenSignedWithAnotherKeyIsUnauthorized() throws Exception {
        String otherKey = "not-the-server-key-0123456789-0123456789-0123456789";
        String forged = new JwtTokenUtils(new JwtProperties(otherKey, Duration.ofHours(1))).generateToken(admin);

        rejected(mockMvc.perform(get("/api/books").header(HttpHeaders.AUTHORIZATION, "Bearer " + forged)));
    }

    @Test
    void aValidTokenIsAccepted() throws Exception {
        String valid = new JwtTokenUtils(new JwtProperties(secret, Duration.ofHours(1))).generateToken(admin);

        mockMvc.perform(get("/api/books").header(HttpHeaders.AUTHORIZATION, "Bearer " + valid))
                .andExpect(status().isOk());
    }

    private void rejected(ResultActions result) throws Exception {
        unauthorized(result, REJECTED)
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer error=\"invalid_token\""));
    }

    private ResultActions unauthorized(ResultActions result, String detail) throws Exception {
        return result
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.detail").value(detail))
                .andExpect(jsonPath("$.instance").exists());
    }
}
