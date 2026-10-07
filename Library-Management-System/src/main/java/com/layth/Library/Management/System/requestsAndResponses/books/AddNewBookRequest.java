package com.layth.Library.Management.System.requestsAndResponses.books;

import com.layth.Library.Management.System.utils.validation.NotFutureYear;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.ISBN;

/**
 * Limits match the books table: title and author are 200 characters at most.
 */
public class AddNewBookRequest {
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

    /**
     * ISBN-13, either as 13 digits or with hyphens between groups (978-0-441-17271-9).
     * @ISBN checks the length and the check digit but ignores any other character, so
     * @Pattern limits the input to digits and single hyphens.
     */
    @NotBlank(message = "ISBN is required")
    @Pattern(regexp = "\\d+(-\\d+)*", message = "must contain only digits, optionally separated by single hyphens")
    @ISBN(type = ISBN.Type.ISBN_13)
    private String isbn;

    public AddNewBookRequest(String title, String author, Integer publicationYear, String isbn) {
        this.title = title;
        this.author = author;
        this.publicationYear = publicationYear;
        this.isbn = isbn;
    }

    public AddNewBookRequest() {
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

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
}
