package com.layth.Library.Management.System.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "borrowing")
public class Borrowing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "patron_id",nullable = false)
    private Patron patron;
    @ManyToOne
    @JoinColumn(name = "book_id",nullable = false)
    private Book book;
    @Column(nullable = false)
    private LocalDate borrowingDate;
    @Column
    private LocalDate returnedDate;

    public Borrowing() {
    }

    public Borrowing(Integer id, Patron patron, Book book, LocalDate borrowingDate) {
        this.id = id;
        this.patron = patron;
        this.book = book;
        this.borrowingDate = borrowingDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Patron getPatron() {
        return patron;
    }

    public void setPatron(Patron patron) {
        this.patron = patron;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public LocalDate getBorrowingDate() {
        return borrowingDate;
    }

    public void setBorrowingDate(LocalDate borrowingDate) {
        this.borrowingDate = borrowingDate;
    }

    public LocalDate getReturnedDate() {
        return returnedDate;
    }

    public void setReturnedDate(LocalDate returnedDate) {
        this.returnedDate = returnedDate;
    }

    public Integer getDaysBetween(){
        if(this.returnedDate != null){
            return Math.toIntExact(ChronoUnit.DAYS.between(this.borrowingDate, this.returnedDate));
        }
        return 0;
    }
}
