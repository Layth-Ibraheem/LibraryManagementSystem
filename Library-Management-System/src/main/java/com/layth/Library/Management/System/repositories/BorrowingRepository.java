package com.layth.Library.Management.System.repositories;

import com.layth.Library.Management.System.entities.Borrowing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BorrowingRepository extends JpaRepository<Borrowing,Integer> {
    @Query("SELECT b FROM Borrowing b WHERE b.patron.id = :patronId AND b.book.id = :bookId AND b.book.isBorrowed = true AND b.returnedDate IS NULL")
    Optional<Borrowing> findActiveBorrowingByPatronIdAndBookId(@Param("patronId") Integer patronId, @Param("bookId") Integer bookId);
}
