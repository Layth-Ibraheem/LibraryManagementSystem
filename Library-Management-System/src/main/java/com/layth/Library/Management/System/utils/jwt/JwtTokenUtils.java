package com.layth.Library.Management.System.utils.jwt;

import com.layth.Library.Management.System.config.JwtProperties;
import com.layth.Library.Management.System.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtTokenUtils {
    private final JwtProperties properties;
    private final SecretKey signingKey;
    private final JwtParser parser;

    public JwtTokenUtils(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.parser = Jwts.parserBuilder().setSigningKey(signingKey).build();
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId()); // Add user ID to claims
        claims.put("username", user.getUserName()); // Add username to claims
        claims.put("roles", user.getRoles()); // Add roles to claims

        Date issuedAt = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUserName())
                .setIssuedAt(issuedAt)
                .setExpiration(new Date(issuedAt.getTime() + properties.expiration().toMillis()))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Verifies the token's signature and expiry and returns its claims.
     *
     * @throws JwtException if the token is malformed, tampered with or expired
     */
    public Claims parseClaims(String token) {
        return parser.parseClaimsJws(token).getBody();
    }
}
