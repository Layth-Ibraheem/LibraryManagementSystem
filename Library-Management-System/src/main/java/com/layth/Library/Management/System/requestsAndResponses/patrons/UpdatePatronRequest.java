package com.layth.Library.Management.System.requestsAndResponses.patrons;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdatePatronRequest {
    @NotBlank
    @Size(min = 4,max = 50)
    private String name;
    @Email
    @NotBlank
    private String email;
    @Size(min = 9,max = 15)
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
