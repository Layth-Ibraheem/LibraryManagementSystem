package com.layth.Library.Management.System.requestsAndResponses.books;

import com.layth.Library.Management.System.utils.validation.NotFutureYear;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Same rules as {@link AddNewBookRequest}. The ISBN identifies the book and cannot be changed.
 */
public class UpdateBookRequest {
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200)
    private String title;

    @NotBlank(message = "Author is required")
    @Size(min = 2, max = 200)
    private String author;

    @NotNull(message = "Publication year is required")
    @Min(1450)
    @NotFutureYear
    private Integer publicationYear;

    public UpdateBookRequest() {
    }

    public UpdateBookRequest(String title, String author, Integer publicationYear) {
        this.title = title;
        this.author = author;
        this.publicationYear = publicationYear;
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
