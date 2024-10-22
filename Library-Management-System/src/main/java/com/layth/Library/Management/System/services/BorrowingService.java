package com.layth.Library.Management.System.services;

import com.layth.Library.Management.System.utils.exceptions.ResourceNotFoundException;
import com.layth.Library.Management.System.entities.Book;
import com.layth.Library.Management.System.entities.Borrowing;
import com.layth.Library.Management.System.entities.Patron;
import com.layth.Library.Management.System.repositories.BookRepository;
import com.layth.Library.Management.System.repositories.BorrowingRepository;
import com.layth.Library.Management.System.repositories.PatronsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
@Service
public class BorrowingService {
    @Autowired
    private BorrowingRepository _repo;
    @Autowired
    private BookRepository _bookRepo;

    @Autowired
    PatronsRepository _patronRepo;

    @Transactional
    public Borrowing borrowBook(Integer patronId,Integer bookId) throws ResourceNotFoundException {
        Optional<Borrowing> optionalBorrowing = _repo.findActiveBorrowingByPatronIdAndBookId(patronId,bookId);
        if(optionalBorrowing.isPresent()){
            return null;
        }

        Optional<Patron> optionalPatron = _patronRepo.findById(patronId);
        if(optionalPatron.isEmpty()){
            throw new ResourceNotFoundException("There is no patron with such id");
        }
        Optional<Book> optionalBook = _bookRepo.findById(bookId);
        if(optionalBook.isEmpty()){
            throw new ResourceNotFoundException("There is no book with such id");
        }

        Patron patron = optionalPatron.get();

        Book book = optionalBook.get();

        book.setBorrowed(true);
        Borrowing borrowing = new Borrowing(null,patron,book, LocalDate.now());
        _bookRepo.save(book);
        return _repo.save(borrowing);
    }
    @Transactional
    public boolean returnBook(Integer bookId, Integer patronId){
        Optional<Borrowing> optionalBorrowing = _repo.findActiveBorrowingByPatronIdAndBookId(patronId,bookId);
        if(optionalBorrowing.isPresent()){
            Borrowing borrowing = optionalBorrowing.get();
            borrowing.getBook().setBorrowed(false);
            borrowing.setReturnedDate(LocalDate.now());
            _repo.save(borrowing);
            return true;
        }
        return false;
    }
}
