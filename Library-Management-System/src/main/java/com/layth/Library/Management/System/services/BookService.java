package com.layth.Library.Management.System.services;

import com.layth.Library.Management.System.utils.exceptions.ResourceNotFoundException;
import com.layth.Library.Management.System.entities.Book;
import com.layth.Library.Management.System.entities.User;
import com.layth.Library.Management.System.repositories.BookRepository;
import com.layth.Library.Management.System.requestsAndResponses.books.AddNewBookRequest;
import com.layth.Library.Management.System.requestsAndResponses.books.UpdateBookRequest;
import com.layth.Library.Management.System.utils.ISBNGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    @Autowired
    private BookRepository _repo;

    public List<Book> getAllBooks() {
        return _repo.findAll();
    }

    public Book addNewBook(AddNewBookRequest request, User addedBy) {
        Book book = new Book(null, request.getTitle(), request.getAuthor(), request.getPublicationYear(), ISBNGenerator.GenerateISBN(),
                LocalDate.now(), addedBy, false);
        return _repo.save(book);
    }

    @CachePut(value = "books", key = "#bookId")
    public Book updateBook(Integer bookId, UpdateBookRequest request) throws ResourceNotFoundException {
        Optional<Book> optionalBook = _repo.findById(bookId);
        if (optionalBook.isPresent()) {
            optionalBook.get().setTitle(request.getTitle());
            optionalBook.get().setAuthor(request.getAuthor());
            optionalBook.get().setPublicationYear(request.getPublicationYear());
            return _repo.save(optionalBook.get());
        } else {
            throw new ResourceNotFoundException("There is no book with id " + bookId);
        }
    }
    @CachePut(value = "books", key = "#bookId")
    public Boolean deleteBook(Integer bookId) {
        Optional<Book> optionalBook = _repo.findById(bookId);
        if (optionalBook.isPresent()) {
            _repo.delete(optionalBook.get());
            return true;
        } else {
            return false;
        }
    }
    @Cacheable(value = "books",key = "#bookId")
    public Book getBookById(Integer bookId) {
        Optional<Book> book = _repo.findById(bookId);
        return book.orElse(null);
    }
}
