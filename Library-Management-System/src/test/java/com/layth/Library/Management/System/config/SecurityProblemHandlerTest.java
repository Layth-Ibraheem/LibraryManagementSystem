package com.layth.Library.Management.System.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The 403 path of the security chain. No URL rule denies an authenticated user today
 * (permissions are checked per endpoint by RoleCheckAspect), so it is tested directly.
 */
class SecurityProblemHandlerTest {
    // Built like Spring Boot's ObjectMapper, which flattens ProblemDetail properties.
    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();
    private final SecurityProblemHandler handler = new SecurityProblemHandler(objectMapper);

    @Test
    void aUrlLevelDenialIsAForbiddenProblem() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(new MockHttpServletRequest("DELETE", "/api/books/7"), response, new AccessDeniedException("Access Denied"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertThat(body.get("status").asInt()).isEqualTo(403);
        assertThat(body.get("title").asText()).isEqualTo("Forbidden");
        assertThat(body.get("detail").asText()).isEqualTo("You do not have permission to perform this action");
        assertThat(body.get("instance").asText()).isEqualTo("/api/books/7");
    }
}
