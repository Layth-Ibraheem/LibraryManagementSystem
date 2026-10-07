package com.layth.Library.Management.System.services;

import com.layth.Library.Management.System.entities.Book;
import com.layth.Library.Management.System.entities.User;
import com.layth.Library.Management.System.repositories.BookRepository;
import com.layth.Library.Management.System.repositories.BorrowingRepository;
import com.layth.Library.Management.System.repositories.UserRepository;
import com.layth.Library.Management.System.requestsAndResponses.books.AddNewBookRequest;
import com.layth.Library.Management.System.requestsAndResponses.books.BookResponse;
import com.layth.Library.Management.System.requestsAndResponses.books.UpdateBookRequest;
import com.layth.Library.Management.System.utils.exceptions.ConflictException;
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
    private final BorrowingRepository borrowingRepository;
    private final UserRepository userRepository;

    public BookService(BookRepository bookRepository, BorrowingRepository borrowingRepository, UserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.borrowingRepository = borrowingRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream().map(BookResponse::from).toList();
    }

    /**
     * Adds a book with the ISBN the client sent, stored without hyphens.
     *
     * @throws ConflictException if a book with the same ISBN already exists
     */
    @Transactional
    public BookResponse addNewBook(AddNewBookRequest request, Integer addedByUserId) {
        String isbn = normalizeIsbn(request.getIsbn());
        if (bookRepository.existsByIsbn(isbn)) {
            throw new ConflictException("A book with ISBN " + isbn + " already exists");
        }
        User addedBy = userRepository.getReferenceById(addedByUserId);
        Book book = new Book(null, request.getTitle(), request.getAuthor(), request.getPublicationYear(), isbn,
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

    /**
     * Deletes a book that has never been lent. A book with loans is kept, because deleting it would
     * erase the loan history (409). The row lock stops a borrow from slipping in between the check and the delete.
     */
    @CacheEvict(value = BOOKS_CACHE, key = "#bookId")
    @Transactional
    public void deleteBook(Integer bookId) {
        Book book = bookRepository.findByIdForUpdate(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("There is no book with id " + bookId));
        if (borrowingRepository.existsByBookId(bookId)) {
            throw new ConflictException("Book " + bookId + " has loan history and cannot be deleted");
        }
        bookRepository.delete(book);
    }

    @Cacheable(value = BOOKS_CACHE, key = "#bookId")
    @Transactional(readOnly = true)
    public BookResponse getBookById(Integer bookId) {
        return BookResponse.from(findBook(bookId));
    }

    /** 978-0-441-17271-9 and 9780441172719 are the same ISBN. */
    static String normalizeIsbn(String isbn) {
        return isbn.replace("-", "");
    }

    private Book findBook(Integer bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("There is no book with id " + bookId));
    }
}
