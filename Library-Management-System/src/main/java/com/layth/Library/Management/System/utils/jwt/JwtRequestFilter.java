package com.layth.Library.Management.System.utils.jwt;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Turns a valid {@code Authorization: Bearer <jwt>} header into the request's Authentication.
 * <p>
 * A token that cannot be verified (malformed, tampered with, signed with another key or expired)
 * is not an error of this filter: the request simply continues unauthenticated, and the
 * authentication entry point answers 401 if the endpoint needs a user. The request is marked
 * with {@link #REJECTED_TOKEN_ATTRIBUTE} so that the 401 can say the token was the problem.
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    /** Request attribute set when a Bearer token was sent but rejected. */
    public static final String REJECTED_TOKEN_ATTRIBUTE = JwtRequestFilter.class.getName() + ".REJECTED_TOKEN";

    private static final Logger log = LoggerFactory.getLogger(JwtRequestFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    // Login and register never read the token, so a stale or broken one in the header must not block them.
    private static final RequestMatcher AUTH_ENDPOINTS = new AntPathRequestMatcher("/api/auth/**");

    private final CurrentUserProvider currentUserProvider;

    public JwtRequestFilter(CurrentUserProvider currentUserProvider) {
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return AUTH_ENDPOINTS.matches(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            try {
                CurrentUser currentUser = currentUserProvider.getCurrentUser(authorizationHeader.substring(BEARER_PREFIX.length()));

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(currentUser, null, null);
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } catch (JwtException | IllegalArgumentException ex) {
                // An expected client error, not a server fault: no stack trace, and never log the token itself.
                SecurityContextHolder.clearContext();
                request.setAttribute(REJECTED_TOKEN_ATTRIBUTE, Boolean.TRUE);
                log.debug("Rejected bearer token for {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getClass().getSimpleName());
            }
        }
        chain.doFilter(request, response);
    }
}
