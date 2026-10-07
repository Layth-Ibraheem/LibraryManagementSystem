package com.layth.Library.Management.System.repositories;

import com.layth.Library.Management.System.entities.Book;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book,Integer> {
    /**
     * Loads the book and write-locks its row until the surrounding transaction ends
     * (SELECT ... FOR UPDATE on H2, WITH (UPDLOCK, HOLDLOCK, ROWLOCK) on SQL Server).
     * Another transaction that asks for the same lock waits until this one commits.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Book b WHERE b.id = :id")
    Optional<Book> findByIdForUpdate(@Param("id") Integer id);

    boolean existsByIsbn(String isbn);
}
