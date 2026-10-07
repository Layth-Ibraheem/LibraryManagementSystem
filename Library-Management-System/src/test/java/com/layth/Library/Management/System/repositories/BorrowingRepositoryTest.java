package com.layth.Library.Management.System.repositories;

import com.layth.Library.Management.System.entities.Book;
import com.layth.Library.Management.System.entities.Borrowing;
import com.layth.Library.Management.System.entities.Patron;
import com.layth.Library.Management.System.entities.User;
import com.layth.Library.Management.System.entities.UserRoles;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JPA slice: only the repositories and an embedded H2 database (replaced by @DataJpaTest, so
 * no configured database is ever used). Each test runs in a transaction that is rolled back.
 */
@DataJpaTest
class BorrowingRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BorrowingRepository borrowingRepository;

    @Autowired
    private BookRepository bookRepository;

    private Book book;
    private Patron patron;
    private Patron otherPatron;

    @BeforeEach
    void persistABookAndTwoPatrons() {
        User librarian = entityManager.persist(new User(null, "librarian", "not-a-real-hash", UserRoles.ManageBooks.getRole()));
        book = entityManager.persist(new Book(null, "Refactoring", "Martin Fowler", 1999, "9780201485677",
                LocalDate.now(), librarian, false));
        patron = entityManager.persist(new Patron(null, "Ada Reader", "ada@example.com", "+963 11 555 0100"));
        otherPatron = entityManager.persist(new Patron(null, "Grace Reader", "grace@example.com", "+963 11 555 0101"));
    }

    @Test
    void aLoanWithoutReturnedDateIsOpen() {
        entityManager.persistAndFlush(new Borrowing(null, patron, book, LocalDate.now()));

        assertThat(borrowingRepository.existsByBookIdAndReturnedDateIsNull(book.getId())).isTrue();
        assertThat(borrowingRepository.findByBookIdAndPatronIdAndReturnedDateIsNull(book.getId(), patron.getId()))
                .isPresent();
    }

    @Test
    void aReturnedLoanIsNoLongerOpenButStillCountsAsHistory() {
        Borrowing loan = new Borrowing(null, patron, book, LocalDate.now().minusDays(3));
        loan.setReturnedDate(LocalDate.now());
        entityManager.persistAndFlush(loan);

        assertThat(borrowingRepository.existsByBookIdAndReturnedDateIsNull(book.getId())).isFalse();
        assertThat(borrowingRepository.findByBookIdAndPatronIdAndReturnedDateIsNull(book.getId(), patron.getId()))
                .isEmpty();
        assertThat(borrowingRepository.existsByBookId(book.getId())).isTrue();
        assertThat(borrowingRepository.existsByPatronId(patron.getId())).isTrue();
    }

    @Test
    void anOpenLoanIsFoundOnlyForThePatronWhoHasIt() {
        entityManager.persistAndFlush(new Borrowing(null, patron, book, LocalDate.now()));

        assertThat(borrowingRepository.findByBookIdAndPatronIdAndReturnedDateIsNull(book.getId(), otherPatron.getId()))
                .isEmpty();
        assertThat(borrowingRepository.existsByPatronId(otherPatron.getId())).isFalse();
    }

    @Test
    void findByIdForUpdateLoadsTheBookWithAPessimisticWriteLock() {
        entityManager.flush();
        entityManager.clear();

        Book locked = bookRepository.findByIdForUpdate(book.getId()).orElseThrow();

        assertThat(locked.getIsbn()).isEqualTo("9780201485677");
        assertThat(entityManager.getEntityManager().getLockMode(locked)).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
        assertThat(bookRepository.findByIdForUpdate(book.getId() + 1000)).isEmpty();
    }
}
