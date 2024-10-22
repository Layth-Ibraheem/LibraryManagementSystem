package com.layth.Library.Management.System.requestsAndResponses.librarians;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateLibrarianRequest {
    @Size(min = 4,max = 25)
    @NotBlank(message = "First name is required")
    private String firstName;
    @Size(min = 4,max = 25)
    @NotBlank(message = "Last name is required")
    private String lastName;


    public UpdateLibrarianRequest() {
    }

    public UpdateLibrarianRequest(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

}
