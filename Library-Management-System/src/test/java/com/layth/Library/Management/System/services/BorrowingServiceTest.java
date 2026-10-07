package com.layth.Library.Management.System.services;

import com.layth.Library.Management.System.entities.Book;
import com.layth.Library.Management.System.entities.Borrowing;
import com.layth.Library.Management.System.entities.Patron;
import com.layth.Library.Management.System.repositories.BookRepository;
import com.layth.Library.Management.System.repositories.BorrowingRepository;
import com.layth.Library.Management.System.repositories.PatronsRepository;
import com.layth.Library.Management.System.utils.exceptions.ConflictException;
import com.layth.Library.Management.System.utils.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Plain Mockito unit test: no Spring context, so it runs in milliseconds. The book and the patron
 * have different ids, and Mockito's strict stubs fail on a stub called with other arguments, so
 * swapping bookId and patronId anywhere in the service or in a call makes these tests fail.
 */
@ExtendWith(MockitoExtension.class)
class BorrowingServiceTest {
    private static final int BOOK_ID = 10;
    private static final int PATRON_ID = 20;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowingRepository borrowingRepository;

    @Mock
    private PatronsRepository patronsRepository;

    @InjectMocks
    private BorrowingService borrowingService;

    @Test
    void borrowBookSavesAnOpenLoanAndMarksTheBookAsBorrowed() {
        Book book = book(false);
        Patron patron = patron();
        when(bookRepository.findByIdForUpdate(BOOK_ID)).thenReturn(Optional.of(book));
        when(patronsRepository.findById(PATRON_ID)).thenReturn(Optional.of(patron));
        when(borrowingRepository.existsByBookIdAndReturnedDateIsNull(BOOK_ID)).thenReturn(false);
        when(borrowingRepository.save(any(Borrowing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Borrowing loan = borrowingService.borrowBook(BOOK_ID, PATRON_ID);

        ArgumentCaptor<Borrowing> saved = ArgumentCaptor.forClass(Borrowing.class);
        verify(borrowingRepository).save(saved.capture());
        assertThat(saved.getValue()).isSameAs(loan);
        assertThat(loan.getBook()).isSameAs(book);
        assertThat(loan.getPatron()).isSameAs(patron);
        assertThat(loan.getBorrowingDate()).isEqualTo(LocalDate.now());
        assertThat(loan.getReturnedDate()).isNull();
        assertThat(book.isBorrowed()).isTrue();
    }

    @Test
    void borrowingABookThatIsAlreadyOnLoanIsAConflictAndSavesNothing() {
        Book book = book(true);
        when(bookRepository.findByIdForUpdate(BOOK_ID)).thenReturn(Optional.of(book));
        when(patronsRepository.findById(PATRON_ID)).thenReturn(Optional.of(patron()));
        when(borrowingRepository.existsByBookIdAndReturnedDateIsNull(BOOK_ID)).thenReturn(true);

        assertThatThrownBy(() -> borrowingService.borrowBook(BOOK_ID, PATRON_ID))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Book 10 is already on loan");
        verify(borrowingRepository, never()).save(any(Borrowing.class));
    }

    @Test
    void borrowingAnUnknownBookIsNotFound() {
        when(bookRepository.findByIdForUpdate(BOOK_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowingService.borrowBook(BOOK_ID, PATRON_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("There is no book with id 10");
        verifyNoInteractions(patronsRepository, borrowingRepository);
    }

    @Test
    void borrowingForAnUnknownPatronIsNotFoundAndLeavesTheBookAvailable() {
        Book book = book(false);
        when(bookRepository.findByIdForUpdate(BOOK_ID)).thenReturn(Optional.of(book));
        when(patronsRepository.findById(PATRON_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowingService.borrowBook(BOOK_ID, PATRON_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("There is no patron with id 20");
        verifyNoInteractions(borrowingRepository);
        assertThat(book.isBorrowed()).isFalse();
    }

    @Test
    void returnBookClosesTheOpenLoanAndMarksTheBookAsAvailable() {
        Book book = book(true);
        Borrowing openLoan = new Borrowing(1, patron(), book, LocalDate.now().minusDays(7));
        when(bookRepository.findByIdForUpdate(BOOK_ID)).thenReturn(Optional.of(book));
        when(patronsRepository.existsById(PATRON_ID)).thenReturn(true);
        when(borrowingRepository.findByBookIdAndPatronIdAndReturnedDateIsNull(BOOK_ID, PATRON_ID))
                .thenReturn(Optional.of(openLoan));

        Borrowing result = borrowingService.returnBook(BOOK_ID, PATRON_ID);

        assertThat(result).isSameAs(openLoan);
        assertThat(result.getReturnedDate()).isEqualTo(LocalDate.now());
        assertThat(book.isBorrowed()).isFalse();
    }

    @Test
    void returningWithoutAnOpenLoanIsAConflictAndChangesNothing() {
        Book book = book(true); // on loan, but to somebody else
        when(bookRepository.findByIdForUpdate(BOOK_ID)).thenReturn(Optional.of(book));
        when(patronsRepository.existsById(PATRON_ID)).thenReturn(true);
        when(borrowingRepository.findByBookIdAndPatronIdAndReturnedDateIsNull(BOOK_ID, PATRON_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowingService.returnBook(BOOK_ID, PATRON_ID))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Patron 20 has no open loan of book 10");
        assertThat(book.isBorrowed()).isTrue();
    }

    @Test
    void returningAnUnknownBookIsNotFound() {
        when(bookRepository.findByIdForUpdate(BOOK_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowingService.returnBook(BOOK_ID, PATRON_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("There is no book with id 10");
        verifyNoInteractions(patronsRepository, borrowingRepository);
    }

    @Test
    void returningForAnUnknownPatronIsNotFound() {
        when(bookRepository.findByIdForUpdate(BOOK_ID)).thenReturn(Optional.of(book(true)));
        when(patronsRepository.existsById(PATRON_ID)).thenReturn(false);

        assertThatThrownBy(() -> borrowingService.returnBook(BOOK_ID, PATRON_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("There is no patron with id 20");
        verifyNoInteractions(borrowingRepository);
    }

    private static Book book(boolean borrowed) {
        Book book = new Book();
        book.setId(BOOK_ID);
        book.setBorrowed(borrowed);
        return book;
    }

    private static Patron patron() {
        return new Patron(PATRON_ID, "Ada Reader", "ada@example.com", "+963 11 555 0100");
    }
}
