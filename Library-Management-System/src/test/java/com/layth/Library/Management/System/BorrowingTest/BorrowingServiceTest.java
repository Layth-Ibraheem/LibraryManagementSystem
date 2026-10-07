package com.layth.Library.Management.System.BorrowingTest;

import com.layth.Library.Management.System.entities.Book;
import com.layth.Library.Management.System.entities.Borrowing;
import com.layth.Library.Management.System.entities.Patron;
import com.layth.Library.Management.System.repositories.BookRepository;
import com.layth.Library.Management.System.repositories.BorrowingRepository;
import com.layth.Library.Management.System.repositories.PatronsRepository;
import com.layth.Library.Management.System.services.BorrowingService;
import com.layth.Library.Management.System.utils.exceptions.ConflictException;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class BorrowingServiceTest {
    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowingRepository borrowingRepository;

    @Mock
    private PatronsRepository patronRepository;

    @InjectMocks
    private BorrowingService borrowingService;

    @Test
    public void testBorrowBook() {
        // Arrange
        Integer patronId = 1;
        Integer bookId = 1;

        // Mocking a Patron
        Patron patron = new Patron();
        patron.setId(patronId);

        // Mocking a Book
        Book book = new Book();
        book.setId(bookId);
        book.setBorrowed(false);  // Initially the book is not borrowed

        // Mocking a Borrowing
        Borrowing borrowing = new Borrowing();
        borrowing.setPatron(patron);
        borrowing.setBook(book);
        borrowing.setBorrowingDate(LocalDate.now());

        // Mock the PatronRepository call to return a valid patron
        when(patronRepository.findById(patronId)).thenReturn(Optional.of(patron));

        // Mock the BookRepository call to return a valid book (loaded with a row lock)
        when(bookRepository.findByIdForUpdate(bookId)).thenReturn(Optional.of(book));

        // Mock the BorrowingRepository save method
        when(borrowingRepository.save(any(Borrowing.class))).thenReturn(borrowing);

        // Act
        borrowingService.borrowBook(bookId, patronId);

        // Assert
        assertTrue(book.isBorrowed()); // The book's isBorrowed must be true
        verify(borrowingRepository, times(1)).save(any(Borrowing.class)); // Ensure borrowing record is saved
    }

    @Test
    public void testReturnBook() {
        // Arrange
        Integer patronId = 1;
        Integer bookId = 1;

        Book book = new Book();
        book.setId(bookId);
        book.setBorrowed(true);  // Initially the book is borrowed

        Borrowing borrowing = new Borrowing();
        borrowing.setId(1);
        borrowing.setPatron(new Patron(patronId,"","",""));
        borrowing.setBook(book);
        borrowing.setBorrowingDate(LocalDate.of(2023, 10, 1));  // Some past borrowing date

        when(bookRepository.findByIdForUpdate(bookId)).thenReturn(Optional.of(book));
        when(patronRepository.existsById(patronId)).thenReturn(true);
        when(borrowingRepository.findByBookIdAndPatronIdAndReturnedDateIsNull(bookId, patronId))
                .thenReturn(Optional.of(borrowing));

        // Act
        Borrowing result = borrowingService.returnBook(bookId, patronId);

        // Assert
        assertSame(borrowing, result);
        assertFalse(book.isBorrowed()); // The book's isBorrowed must be false
        assertEquals(LocalDate.now(), borrowing.getReturnedDate()); // The returnedDate must be now
    }

    @Test
    public void borrowingABookThatIsAlreadyOnLoanIsAConflict() {
        Book book = new Book();
        book.setId(1);
        book.setBorrowed(true);
        when(bookRepository.findByIdForUpdate(1)).thenReturn(Optional.of(book));
        when(patronRepository.findById(2)).thenReturn(Optional.of(new Patron(2, "Second Patron", "", "")));
        when(borrowingRepository.existsByBookIdAndReturnedDateIsNull(1)).thenReturn(true);

        assertThrows(ConflictException.class, () -> borrowingService.borrowBook(1, 2));
        verify(borrowingRepository, never()).save(any(Borrowing.class));
    }
}
