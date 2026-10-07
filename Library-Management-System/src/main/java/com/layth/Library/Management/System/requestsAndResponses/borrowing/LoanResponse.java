package com.layth.Library.Management.System.requestsAndResponses.borrowing;

import com.layth.Library.Management.System.entities.Borrowing;

import java.time.LocalDate;

/**
 * One loan of a book to a patron. {@code returnedDate} is null while the book is still out.
 */
public record LoanResponse(Integer id, Integer bookId, Integer patronId, LocalDate borrowingDate, LocalDate returnedDate) {
    /** Reads only the ids of the book and the patron, which lazy proxies hold without a query. */
    public static LoanResponse from(Borrowing borrowing) {
        return new LoanResponse(borrowing.getId(), borrowing.getBook().getId(), borrowing.getPatron().getId(),
                borrowing.getBorrowingDate(), borrowing.getReturnedDate());
    }
}
