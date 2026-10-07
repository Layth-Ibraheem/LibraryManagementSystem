package com.layth.Library.Management.System.requestsAndResponses.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank(message = "User name is required")
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "User name may contain only letters, digits, '.', '_' and '-'")
    private String userName;

    // BCrypt ignores everything after 72 bytes, so longer passwords are rejected instead of silently truncated.
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 72)
    private String password;
    private Integer roles;

    public RegisterRequest() {
    }

    public RegisterRequest(String userName, String password, Integer roles) {
        this.userName = userName;
        this.password = password;
        this.roles = roles;
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

    public Integer getRoles() {
        return roles;
    }

    public void setRoles(Integer roles) {
        this.roles = roles;
    }
}
