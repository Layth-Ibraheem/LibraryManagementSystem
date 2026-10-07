package com.layth.Library.Management.System.utils.jwt;

import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {
    private final JwtTokenUtils jwtTokenUtils;

    public CurrentUserProvider(JwtTokenUtils jwtTokenUtils) {
        this.jwtTokenUtils = jwtTokenUtils;
    }

    public CurrentUser getCurrentUser(String token) {
        Claims claims = jwtTokenUtils.parseClaims(token);
        Integer id = claims.get("id", Integer.class);
        Integer roles = claims.get("roles", Integer.class);
        String userName = claims.get("username", String.class);

        return new CurrentUser(id, userName, roles);
    }
}
