package com.layth.Library.Management.System.requestsAndResponses.books;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.Year;

public class AddNewBookRequest {
    @Size(min = 10, max = 255)
    @NotBlank(message = "Title is required")
    private String title;

    @Size(min = 4, max = 50)
    @NotBlank(message = "Author is required")
    private String author;

    @Min(1450)
    private Integer publicationYear;

    public AddNewBookRequest(String title, String author, Integer publicationYear) {
        this.title = title;
        this.author = author;
        this.publicationYear = publicationYear;
    }

    public AddNewBookRequest() {
    }
    @AssertTrue(message = "Publication year can`t be in the future")
    public boolean isValidPublicationYear(){
        return this.publicationYear <= Year.now().getValue();
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }
}
