package com.layth.Library.Management.System.services;

import com.layth.Library.Management.System.entities.Book;
import com.layth.Library.Management.System.entities.User;
import com.layth.Library.Management.System.repositories.BookRepository;
import com.layth.Library.Management.System.repositories.UserRepository;
import com.layth.Library.Management.System.requestsAndResponses.books.AddNewBookRequest;
import com.layth.Library.Management.System.requestsAndResponses.books.BookResponse;
import com.layth.Library.Management.System.requestsAndResponses.books.UpdateBookRequest;
import com.layth.Library.Management.System.utils.ISBNGenerator;
import com.layth.Library.Management.System.utils.exceptions.ResourceNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Books, returned as {@link BookResponse} DTOs. The "books" cache holds one BookResponse per book id:
 * reads fill it, updates replace the entry, and anything else that changes a book evicts it
 * (delete here, borrow and return in BorrowingService). A missing book throws, and exceptions are
 * never cached, so a 404 cannot stick to an id that is created later.
 */
@Service
public class BookService {
    public static final String BOOKS_CACHE = "books";

    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public BookService(BookRepository bookRepository, UserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream().map(BookResponse::from).toList();
    }

    @Transactional
    public BookResponse addNewBook(AddNewBookRequest request, Integer addedByUserId) {
        User addedBy = userRepository.getReferenceById(addedByUserId);
        Book book = new Book(null, request.getTitle(), request.getAuthor(), request.getPublicationYear(), ISBNGenerator.GenerateISBN(),
                LocalDate.now(), addedBy, false);
        return BookResponse.from(bookRepository.save(book));
    }

    @CachePut(value = BOOKS_CACHE, key = "#bookId")
    @Transactional
    public BookResponse updateBook(Integer bookId, UpdateBookRequest request) {
        Book book = findBook(bookId);
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setPublicationYear(request.getPublicationYear());
        // No save(): the book is managed, so dirty checking writes the changes at commit.
        return BookResponse.from(book);
    }

    @CacheEvict(value = BOOKS_CACHE, key = "#bookId")
    @Transactional
    public void deleteBook(Integer bookId) {
        bookRepository.delete(findBook(bookId));
    }

    @Cacheable(value = BOOKS_CACHE, key = "#bookId")
    @Transactional(readOnly = true)
    public BookResponse getBookById(Integer bookId) {
        return BookResponse.from(findBook(bookId));
    }

    private Book findBook(Integer bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("There is no book with id " + bookId));
    }
}
