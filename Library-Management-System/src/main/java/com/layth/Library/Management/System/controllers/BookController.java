package com.layth.Library.Management.System.controllers;

import com.layth.Library.Management.System.entities.Book;
import com.layth.Library.Management.System.entities.User;
import com.layth.Library.Management.System.entities.UserRoles;
import com.layth.Library.Management.System.requestsAndResponses.books.AddNewBookRequest;
import com.layth.Library.Management.System.requestsAndResponses.books.BookResponse;
import com.layth.Library.Management.System.requestsAndResponses.books.UpdateBookRequest;
import com.layth.Library.Management.System.services.BookService;
import com.layth.Library.Management.System.services.LibrarianService;
import com.layth.Library.Management.System.utils.annotations.RequireRole;
import com.layth.Library.Management.System.utils.exceptions.ResourceNotFoundException;
import com.layth.Library.Management.System.utils.jwt.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {
    @Autowired
    private LibrarianService _librarianService;

    @Autowired
    private BookService _bookService;

    @PostMapping
    @RequireRole(role = UserRoles.ManageBooks)
    public ResponseEntity<?> addNewBook(@Valid @RequestBody AddNewBookRequest request) {
        CurrentUser currentUser = (CurrentUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User addedByUser = new User(currentUser.getId(), currentUser.getUserName(), "", currentUser.getRoles());

        Book addedBook = _bookService.addNewBook(request, addedByUser);
        if (addedBook.getId() == null) {
            return new ResponseEntity<>("Error happened, could not add the book", HttpStatus.FAILED_DEPENDENCY);
        } else {
            return new ResponseEntity<>(MapToBookResponse(addedBook), HttpStatus.CREATED);
        }


    }

    @PutMapping("/{id}")
    @RequireRole(role = UserRoles.ManageBooks)
    public ResponseEntity<?> updateBook(@PathVariable(name = "id") Integer id, @Valid @RequestBody UpdateBookRequest request) throws ResourceNotFoundException {
        Book updatedBook = _bookService.updateBook(id,request);
        if (updatedBook != null) {
            return new ResponseEntity<>(MapToBookResponse(updatedBook), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("there is no book with such id", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    @RequireRole(role = UserRoles.ManageBooks)
    public ResponseEntity<?> getBookById(@PathVariable(name = "id") Integer id) {
        Book book = _bookService.getBookById(id);
        if (book != null) {
            return new ResponseEntity<>(MapToBookResponse(book), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("No librarian with such id", HttpStatus.NOT_FOUND);
        }

    }

    @GetMapping
    @RequireRole(role = UserRoles.ManageBooks)
    public ResponseEntity<?> getAllBooks() {
        List<Book> books = _bookService.getAllBooks();
        List<BookResponse> booksResponse = new ArrayList<>();
        for (Book book : books) {
            booksResponse.add(MapToBookResponse(book));
        }
        return new ResponseEntity<>(booksResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @RequireRole(role = UserRoles.ManageBooks)
    public ResponseEntity<?> deleteBook(@PathVariable(name = "id") Integer id) {
        boolean isDeleted = _bookService.deleteBook(id);
        return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    private static BookResponse MapToBookResponse(Book book) {
        return new BookResponse(book.getId(), book.getTitle(), book.getAuthor(), book.getPublicationYear(), book.getIsbn(),
                book.getCreationDate(), book.isBorrowed(), book.getAddedByUser().getId());
    }
}
