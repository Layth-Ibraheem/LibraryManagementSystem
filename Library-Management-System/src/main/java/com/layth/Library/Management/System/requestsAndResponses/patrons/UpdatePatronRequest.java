package com.layth.Library.Management.System.requestsAndResponses.patrons;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Same rules as {@link AddNewPatronRequest}.
 */
public class UpdatePatronRequest {
    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    @NotBlank
    @Size(min = 7, max = 20)
    @Pattern(regexp = "\\+?[0-9][0-9 -]*", message = "must contain only digits, spaces or hyphens, optionally starting with +")
    private String phoneNumber;

    public UpdatePatronRequest(String name, String email, String phoneNumber) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
