package com.layth.Library.Management.System.repositories;

import com.layth.Library.Management.System.entities.Borrowing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * A loan is open while its returnedDate is null.
 */
public interface BorrowingRepository extends JpaRepository<Borrowing,Integer> {
    boolean existsByBookIdAndReturnedDateIsNull(Integer bookId);

    boolean existsByBookId(Integer bookId);

    boolean existsByPatronId(Integer patronId);

    Optional<Borrowing> findByBookIdAndPatronIdAndReturnedDateIsNull(Integer bookId, Integer patronId);
}
