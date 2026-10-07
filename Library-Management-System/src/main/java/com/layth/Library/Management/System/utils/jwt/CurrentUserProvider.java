package com.layth.Library.Management.System.utils.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {
    private final JwtTokenUtils jwtTokenUtils;

    public CurrentUserProvider(JwtTokenUtils jwtTokenUtils) {
        this.jwtTokenUtils = jwtTokenUtils;
    }

    /**
     * Builds the current user from a verified token.
     *
     * @throws io.jsonwebtoken.JwtException if the token is invalid or expired, or lacks the id, username or roles claim
     */
    public CurrentUser getCurrentUser(String token) {
        Claims claims = jwtTokenUtils.parseClaims(token);
        Integer id = requiredClaim(claims, "id", Integer.class);
        Integer roles = requiredClaim(claims, "roles", Integer.class);
        String userName = requiredClaim(claims, "username", String.class);

        return new CurrentUser(id, userName, roles);
    }

    // A correctly signed token without these claims was not issued by this application. Reject it
    // like any other bad token (401) instead of failing later with a NullPointerException (500).
    private static <T> T requiredClaim(Claims claims, String name, Class<T> type) {
        T value = claims.get(name, type);
        if (value == null) {
            throw new MalformedJwtException("Token has no '" + name + "' claim");
        }
        return value;
    }
}
