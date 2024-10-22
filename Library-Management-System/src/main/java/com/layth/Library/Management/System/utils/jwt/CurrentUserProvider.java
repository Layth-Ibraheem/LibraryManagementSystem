package com.layth.Library.Management.System.utils.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class CurrentUserProvider {
    @Value("SECRET_KEY")
    private static String secretKey;

    public CurrentUser getCurrentUser(String token){
        Claims claims = extractAllClaims(token);
        Integer id = claims.get("id",Integer.class);
        Integer roles = claims.get("roles",Integer.class);
        String userName = claims.get("username",String.class);

        return new CurrentUser(id,userName,roles);
    }

    private static Claims extractAllClaims(String token) {
        String secretKey = "secret-key-secret-key-secret-key-secret-key-secret-key-secret-key-secret-key-secret-key-secret-key-secret-key";
        return Jwts.parser()
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token)
                .getBody();
    }
}
