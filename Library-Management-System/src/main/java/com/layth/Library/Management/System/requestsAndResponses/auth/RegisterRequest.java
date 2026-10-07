package com.layth.Library.Management.System.requestsAndResponses.auth;

import com.layth.Library.Management.System.utils.validation.MaxUtf8Bytes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Self-registration. There is deliberately no roles field: new users get no permissions,
 * and only an administrator can grant them (PUT /api/users/{id}/roles).
 */
public class RegisterRequest {
    @NotBlank(message = "User name is required")
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "User name may contain only letters, digits, '.', '_' and '-'")
    private String userName;

    // BCrypt ignores everything after 72 bytes, so longer passwords are rejected instead of silently
    // truncated. The limit is in UTF-8 bytes, not characters: 'é' counts twice.
    @NotBlank(message = "Password is required")
    @Size(min = 8)
    @MaxUtf8Bytes(72)
    private String password;

    public RegisterRequest() {
    }

    public RegisterRequest(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
