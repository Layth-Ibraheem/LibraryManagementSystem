package com.layth.Library.Management.System.services;

import com.layth.Library.Management.System.entities.Book;
import com.layth.Library.Management.System.entities.Patron;
import com.layth.Library.Management.System.entities.User;
import com.layth.Library.Management.System.repositories.BookRepository;
import com.layth.Library.Management.System.repositories.BorrowingRepository;
import com.layth.Library.Management.System.repositories.PatronsRepository;
import com.layth.Library.Management.System.repositories.UserRepository;
import com.layth.Library.Management.System.utils.exceptions.ConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Two requests borrow the same book at the same time. Not @Transactional: each side needs its own
 * real, committed transaction for the row lock to matter.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:borrow-race;DB_CLOSE_DELAY=-1")
class BorrowingConcurrencyTest {
    @Autowired
    private BorrowingService borrowingService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private PatronsRepository patronsRepository;

    @Autowired
    private BorrowingRepository borrowingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void aSecondBorrowWaitsForTheFirstToCommitAndThenGetsAConflict() throws Exception {
        User admin = userRepository.findByUserName("admin").orElseThrow();
        Book book = bookRepository.save(new Book(null, "Java Concurrency in Practice", "Brian Goetz", 2006,
                "isbn-race-1", LocalDate.now(), admin, false));
        Patron first = patronsRepository.save(new Patron(null, "First Patron", "first@example.com", "0991111111"));
        Patron second = patronsRepository.save(new Patron(null, "Second Patron", "second@example.com", "0992222222"));

        // The first request borrows the book inside a transaction that stays open for a while,
        // as if the request were slow. Its row lock is held until that transaction commits.
        CountDownLatch firstHasBorrowed = new CountDownLatch(1);
        CompletableFuture<Void> firstRequest = CompletableFuture.runAsync(() ->
                new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                    borrowingService.borrowBook(book.getId(), first.getId());
                    firstHasBorrowed.countDown();
                    sleep(300);
                }));
        assertThat(firstHasBorrowed.await(10, TimeUnit.SECONDS)).isTrue();

        // Without the lock, this call would not see the first loan (not committed yet) and would succeed too.
        assertThatThrownBy(() -> borrowingService.borrowBook(book.getId(), second.getId()))
                .isInstanceOf(ConflictException.class);

        firstRequest.get(10, TimeUnit.SECONDS);
        assertThat(borrowingRepository.findAll())
                .filteredOn(loan -> loan.getReturnedDate() == null)
                .hasSize(1);
        assertThat(bookRepository.findById(book.getId()).orElseThrow().isBorrowed()).isTrue();
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
