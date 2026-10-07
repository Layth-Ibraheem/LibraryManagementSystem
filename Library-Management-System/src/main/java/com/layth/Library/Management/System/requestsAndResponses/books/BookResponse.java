package com.layth.Library.Management.System.requestsAndResponses.books;

import com.layth.Library.Management.System.entities.Book;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * A book as the API shows it. Immutable, so one cached instance can be shared by every request.
 */
public record BookResponse(Integer id, String title, String author, Integer publicationYear, String isbn,
                           LocalDate creationDate, boolean borrowed, Integer addedByUserId) implements Serializable {

    /** Reads only the creator's id, which a lazy proxy holds without a query. */
    public static BookResponse from(Book book) {
        return new BookResponse(book.getId(), book.getTitle(), book.getAuthor(), book.getPublicationYear(),
                book.getIsbn(), book.getCreationDate(), book.isBorrowed(), book.getAddedByUser().getId());
    }
}
