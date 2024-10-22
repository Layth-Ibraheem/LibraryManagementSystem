package com.layth.Library.Management.System.controllers;

import com.layth.Library.Management.System.entities.Borrowing;
import com.layth.Library.Management.System.services.BorrowingService;
import com.layth.Library.Management.System.utils.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BorrowingController {
    @Autowired
    private BorrowingService _borrowingService;

    @PostMapping("/borrow/{bookId}/patron/{patronId}")
    public ResponseEntity<?> borrowBook(@PathVariable Integer bookId, @PathVariable Integer patronId) throws ResourceNotFoundException {
        Borrowing borrowing = _borrowingService.borrowBook(patronId, bookId);
        if (borrowing == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("this patron has already borrowed this book");
        }
        return ResponseEntity.status(HttpStatus.OK).body("book borrowed successfully");
    }

    @PutMapping("/return/{bookId}/patron/{patronId}")
    public ResponseEntity<?> returnBook(@PathVariable Integer bookId, @PathVariable Integer patronId) {
        boolean isReturned = _borrowingService.returnBook(bookId, patronId);
        return isReturned ? ResponseEntity.status(HttpStatus.OK).body("book returned successfully") : ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body("Internal Server Error");
    }
}
