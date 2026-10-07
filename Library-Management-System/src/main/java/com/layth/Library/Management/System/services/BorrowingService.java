package com.layth.Library.Management.System.services;

import com.layth.Library.Management.System.entities.Book;
import com.layth.Library.Management.System.entities.Borrowing;
import com.layth.Library.Management.System.entities.Patron;
import com.layth.Library.Management.System.repositories.BookRepository;
import com.layth.Library.Management.System.repositories.BorrowingRepository;
import com.layth.Library.Management.System.repositories.PatronsRepository;
import com.layth.Library.Management.System.utils.exceptions.ConflictException;
import com.layth.Library.Management.System.utils.exceptions.ResourceNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Lending and returning books. The open loans (rows with no returned date) are the source of truth;
 * Book.isBorrowed is kept in step with them for the book listings.
 * <p>
 * Both operations first lock the book row (SELECT ... FOR UPDATE) until the transaction ends, so two
 * requests for the same book run one after the other: the second one sees the first one's loan
 * instead of both passing the "is it on loan?" check at the same time.
 */
@Service
public class BorrowingService {
    private final BorrowingRepository borrowingRepository;
    private final BookRepository bookRepository;
    private final PatronsRepository patronsRepository;

    public BorrowingService(BorrowingRepository borrowingRepository, BookRepository bookRepository,
                            PatronsRepository patronsRepository) {
        this.borrowingRepository = borrowingRepository;
        this.bookRepository = bookRepository;
        this.patronsRepository = patronsRepository;
    }

    /**
     * Lends the book to the patron.
     *
     * @throws ResourceNotFoundException if the book or the patron does not exist
     * @throws ConflictException         if the book is already on loan to anyone
     */
    @CacheEvict(value = BookService.BOOKS_CACHE, key = "#bookId")
    @Transactional
    public Borrowing borrowBook(Integer bookId, Integer patronId) {
        Book book = lockBook(bookId);
        Patron patron = patronsRepository.findById(patronId)
                .orElseThrow(() -> new ResourceNotFoundException("There is no patron with id " + patronId));
        if (borrowingRepository.existsByBookIdAndReturnedDateIsNull(bookId)) {
            throw new ConflictException("Book " + bookId + " is already on loan");
        }

        book.setBorrowed(true); // no save(book): the locked book is managed, so dirty checking writes it
        return borrowingRepository.save(new Borrowing(null, patron, book, LocalDate.now()));
    }

    /**
     * Closes the patron's open loan of the book.
     *
     * @throws ResourceNotFoundException if the book or the patron does not exist
     * @throws ConflictException         if this patron has no open loan of this book
     */
    @CacheEvict(value = BookService.BOOKS_CACHE, key = "#bookId")
    @Transactional
    public Borrowing returnBook(Integer bookId, Integer patronId) {
        Book book = lockBook(bookId);
        if (!patronsRepository.existsById(patronId)) {
            throw new ResourceNotFoundException("There is no patron with id " + patronId);
        }
        Borrowing loan = borrowingRepository.findByBookIdAndPatronIdAndReturnedDateIsNull(bookId, patronId)
                .orElseThrow(() -> new ConflictException("Patron " + patronId + " has no open loan of book " + bookId));

        loan.setReturnedDate(LocalDate.now());
        book.setBorrowed(false);
        return loan;
    }

    private Book lockBook(Integer bookId) {
        return bookRepository.findByIdForUpdate(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("There is no book with id " + bookId));
    }
}
