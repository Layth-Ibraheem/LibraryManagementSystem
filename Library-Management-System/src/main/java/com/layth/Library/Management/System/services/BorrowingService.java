package com.layth.Library.Management.System.services;

import com.layth.Library.Management.System.utils.exceptions.ResourceNotFoundException;
import com.layth.Library.Management.System.entities.Book;
import com.layth.Library.Management.System.entities.Borrowing;
import com.layth.Library.Management.System.entities.Patron;
import com.layth.Library.Management.System.repositories.BookRepository;
import com.layth.Library.Management.System.repositories.BorrowingRepository;
import com.layth.Library.Management.System.repositories.PatronsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
@Service
public class BorrowingService {
    private final BorrowingRepository borrowingRepository;
    private final BookRepository bookRepository;
    private final PatronsRepository patronsRepository;

    public BorrowingService(BorrowingRepository borrowingRepository, BookRepository bookRepository,
                            PatronsRepository patronsRepository) {
        this.borrowingRepository = borrowingRepository;
        this.bookRepository = bookRepository;
        this.patronsRepository = patronsRepository;
    }

    @Transactional
    public Borrowing borrowBook(Integer patronId,Integer bookId) {
        Optional<Borrowing> optionalBorrowing = borrowingRepository.findActiveBorrowingByPatronIdAndBookId(patronId,bookId);
        if(optionalBorrowing.isPresent()){
            return null;
        }

        Optional<Patron> optionalPatron = patronsRepository.findById(patronId);
        if(optionalPatron.isEmpty()){
            throw new ResourceNotFoundException("There is no patron with such id");
        }
        Optional<Book> optionalBook = bookRepository.findById(bookId);
        if(optionalBook.isEmpty()){
            throw new ResourceNotFoundException("There is no book with such id");
        }

        Patron patron = optionalPatron.get();

        Book book = optionalBook.get();

        book.setBorrowed(true);
        Borrowing borrowing = new Borrowing(null,patron,book, LocalDate.now());
        bookRepository.save(book);
        return borrowingRepository.save(borrowing);
    }
    @Transactional
    public boolean returnBook(Integer bookId, Integer patronId){
        Optional<Borrowing> optionalBorrowing = borrowingRepository.findActiveBorrowingByPatronIdAndBookId(patronId,bookId);
        if(optionalBorrowing.isPresent()){
            Borrowing borrowing = optionalBorrowing.get();
            borrowing.getBook().setBorrowed(false);
            borrowing.setReturnedDate(LocalDate.now());
            borrowingRepository.save(borrowing);
            return true;
        }
        return false;
    }
}
