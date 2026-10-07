package com.layth.Library.Management.System.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * JWT settings bound from {@code jwt.*} (for example the JWT_SECRET environment variable).
 * Validation runs at startup, so a missing or too-short key stops the application immediately.
 *
 * @param secret     HMAC-SHA256 signing key; at least 32 characters (256 bits)
 * @param expiration how long an issued token stays valid, for example {@code 10h}
 */
@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        @NotBlank @Size(min = 32) String secret,
        @NotNull Duration expiration) {
}
