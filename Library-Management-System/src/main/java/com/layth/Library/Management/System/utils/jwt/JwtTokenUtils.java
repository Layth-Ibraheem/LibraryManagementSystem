package com.layth.Library.Management.System.utils.jwt;

import com.layth.Library.Management.System.entities.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtTokenUtils {
    private String Secret = "secret-key-secret-key-secret-key-secret-key-secret-key-secret-key-secret-key-secret-key-secret-key-secret-key";

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId()); // Add user ID to claims
        claims.put("username", user.getUserName()); // Add username to claims
        claims.put("roles", user.getRoles()); // Add roles to claims

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUserName())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10-hour expiration
                .signWith(SignatureAlgorithm.HS256, Secret.getBytes(StandardCharsets.UTF_8))
                .compact();
    }
}
