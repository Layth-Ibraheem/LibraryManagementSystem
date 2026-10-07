package com.layth.Library.Management.System.requestsAndResponses.auth;

import com.layth.Library.Management.System.utils.validation.MaxUtf8Bytes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {
    @NotBlank(message = "User name is required")
    @Size(max = 50)
    private String userName;

    // Same byte limit as on register: no stored password is longer, and BCrypt would compare
    // only the first 72 bytes of a longer one.
    @NotBlank(message = "Password is required")
    @MaxUtf8Bytes(72)
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String userName, String password) {
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
