package com.layth.Library.Management.System.controllers;

import com.layth.Library.Management.System.entities.UserRoles;
import com.layth.Library.Management.System.requestsAndResponses.borrowing.LoanResponse;
import com.layth.Library.Management.System.services.BorrowingService;
import com.layth.Library.Management.System.utils.annotations.RequireRole;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BorrowingController {
    private final BorrowingService borrowingService;

    public BorrowingController(BorrowingService borrowingService) {
        this.borrowingService = borrowingService;
    }

    /** 200 with the new loan; 404 for an unknown book or patron; 409 if the book is already on loan. */
    @PostMapping("/borrow/{bookId}/patron/{patronId}")
    @RequireRole(role = UserRoles.ManagePatrons)
    public ResponseEntity<LoanResponse> borrowBook(@PathVariable(name = "bookId") Integer bookId, @PathVariable(name = "patronId") Integer patronId) {
        return ResponseEntity.ok(LoanResponse.from(borrowingService.borrowBook(bookId, patronId)));
    }

    /** 200 with the closed loan; 404 for an unknown book or patron; 409 if the patron has no open loan of the book. */
    @PutMapping("/return/{bookId}/patron/{patronId}")
    @RequireRole(role = UserRoles.ManagePatrons)
    public ResponseEntity<LoanResponse> returnBook(@PathVariable(name = "bookId") Integer bookId, @PathVariable(name = "patronId") Integer patronId) {
        return ResponseEntity.ok(LoanResponse.from(borrowingService.returnBook(bookId, patronId)));
    }
}
