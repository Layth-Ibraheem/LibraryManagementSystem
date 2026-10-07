package com.layth.Library.Management.System.services;

import com.layth.Library.Management.System.utils.exceptions.ResourceNotFoundException;
import com.layth.Library.Management.System.entities.Book;
import com.layth.Library.Management.System.entities.User;
import com.layth.Library.Management.System.repositories.BookRepository;
import com.layth.Library.Management.System.requestsAndResponses.books.AddNewBookRequest;
import com.layth.Library.Management.System.requestsAndResponses.books.UpdateBookRequest;
import com.layth.Library.Management.System.utils.ISBNGenerator;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book addNewBook(AddNewBookRequest request, User addedBy) {
        Book book = new Book(null, request.getTitle(), request.getAuthor(), request.getPublicationYear(), ISBNGenerator.GenerateISBN(),
                LocalDate.now(), addedBy, false);
        return bookRepository.save(book);
    }

    @CachePut(value = "books", key = "#bookId")
    public Book updateBook(Integer bookId, UpdateBookRequest request) {
        Optional<Book> optionalBook = bookRepository.findById(bookId);
        if (optionalBook.isPresent()) {
            optionalBook.get().setTitle(request.getTitle());
            optionalBook.get().setAuthor(request.getAuthor());
            optionalBook.get().setPublicationYear(request.getPublicationYear());
            return bookRepository.save(optionalBook.get());
        } else {
            throw new ResourceNotFoundException("There is no book with id " + bookId);
        }
    }
    @CachePut(value = "books", key = "#bookId")
    public Boolean deleteBook(Integer bookId) {
        Optional<Book> optionalBook = bookRepository.findById(bookId);
        if (optionalBook.isPresent()) {
            bookRepository.delete(optionalBook.get());
            return true;
        } else {
            return false;
        }
    }
    @Cacheable(value = "books",key = "#bookId")
    public Book getBookById(Integer bookId) {
        Optional<Book> book = bookRepository.findById(bookId);
        return book.orElse(null);
    }
}
