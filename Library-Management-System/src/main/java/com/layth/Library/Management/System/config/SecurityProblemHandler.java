package com.layth.Library.Management.System.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.layth.Library.Management.System.utils.jwt.JwtRequestFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;

/**
 * Writes the security filter chain's own rejections as RFC 9457 problem details, in the same
 * shape GlobalExceptionHandler uses for controller errors. That advice cannot help here:
 * these requests are rejected before they reach the DispatcherServlet.
 * <ul>
 *   <li>401 (entry point): no token, a non-Bearer header, or a token JwtRequestFilter rejected.
 *       The WWW-Authenticate header tells the client which case it is (RFC 6750).</li>
 *   <li>403 (access denied handler): an authenticated caller denied by a URL rule.
 *       A missing permission on an endpoint is reported by RoleCheckAspect through the advice.</li>
 * </ul>
 */
@Component
public class SecurityProblemHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    public SecurityProblemHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        boolean tokenRejected = request.getAttribute(JwtRequestFilter.REJECTED_TOKEN_ATTRIBUTE) != null;
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, tokenRejected ? "Bearer error=\"invalid_token\"" : "Bearer");
        write(request, response, HttpStatus.UNAUTHORIZED, tokenRejected
                ? "The access token is invalid or has expired"
                : "Authentication is required: send 'Authorization: Bearer <token>' from POST /api/auth/login");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        write(request, response, HttpStatus.FORBIDDEN, "You do not have permission to perform this action");
    }

    private void write(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String detail)
            throws IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setInstance(URI.create(request.getRequestURI()));
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
