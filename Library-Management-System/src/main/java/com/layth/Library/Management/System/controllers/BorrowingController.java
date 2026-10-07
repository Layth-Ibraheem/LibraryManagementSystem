package com.layth.Library.Management.System.controllers;

import com.layth.Library.Management.System.entities.Borrowing;
import com.layth.Library.Management.System.entities.UserRoles;
import com.layth.Library.Management.System.services.BorrowingService;
import com.layth.Library.Management.System.utils.annotations.RequireRole;
import com.layth.Library.Management.System.utils.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BorrowingController {
    private final BorrowingService borrowingService;

    public BorrowingController(BorrowingService borrowingService) {
        this.borrowingService = borrowingService;
    }

    @PostMapping("/borrow/{bookId}/patron/{patronId}")
    @RequireRole(role = UserRoles.ManagePatrons)
    public ResponseEntity<?> borrowBook(@PathVariable(name = "bookId") Integer bookId, @PathVariable(name = "patronId") Integer patronId) throws ResourceNotFoundException {
        Borrowing borrowing = borrowingService.borrowBook(patronId, bookId);
        if (borrowing == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("this patron has already borrowed this book");
        }
        return ResponseEntity.status(HttpStatus.OK).body("book borrowed successfully");
    }

    @PutMapping("/return/{bookId}/patron/{patronId}")
    @RequireRole(role = UserRoles.ManagePatrons)
    public ResponseEntity<?> returnBook(@PathVariable(name = "bookId") Integer bookId, @PathVariable(name = "patronId") Integer patronId) {
        boolean isReturned = borrowingService.returnBook(bookId, patronId);
        return isReturned ? ResponseEntity.status(HttpStatus.OK).body("book returned successfully") : ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body("Internal Server Error");
    }
}
