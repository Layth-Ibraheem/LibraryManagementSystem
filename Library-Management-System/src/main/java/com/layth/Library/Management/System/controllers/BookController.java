package com.layth.Library.Management.System.controllers;

import com.layth.Library.Management.System.entities.UserRoles;
import com.layth.Library.Management.System.requestsAndResponses.books.AddNewBookRequest;
import com.layth.Library.Management.System.requestsAndResponses.books.BookResponse;
import com.layth.Library.Management.System.requestsAndResponses.books.UpdateBookRequest;
import com.layth.Library.Management.System.services.BookService;
import com.layth.Library.Management.System.utils.annotations.RequireRole;
import com.layth.Library.Management.System.utils.jwt.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    @RequireRole(role = UserRoles.ManageBooks)
    public ResponseEntity<BookResponse> addNewBook(@Valid @RequestBody AddNewBookRequest request,
                                                   @AuthenticationPrincipal CurrentUser currentUser) {
        BookResponse addedBook = bookService.addNewBook(request, currentUser.getId());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(addedBook.id()).toUri();
        return ResponseEntity.created(location).body(addedBook);
    }

    @PutMapping("/{id}")
    @RequireRole(role = UserRoles.ManageBooks)
    public ResponseEntity<BookResponse> updateBook(@PathVariable(name = "id") Integer id, @Valid @RequestBody UpdateBookRequest request) {
        return ResponseEntity.ok(bookService.updateBook(id, request));
    }

    @GetMapping("/{id}")
    @RequireRole(role = UserRoles.ManageBooks)
    public ResponseEntity<BookResponse> getBookById(@PathVariable(name = "id") Integer id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @GetMapping
    @RequireRole(role = UserRoles.ManageBooks)
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @DeleteMapping("/{id}")
    @RequireRole(role = UserRoles.ManageBooks)
    public ResponseEntity<Void> deleteBook(@PathVariable(name = "id") Integer id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
