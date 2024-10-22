package com.layth.Library.Management.System.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "books")

public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, length = 200)
    private String title;
    @Column(nullable = false, length = 200)

    private String author;
    @Column(nullable = false)

    private Integer publicationYear;
    @Column(nullable = false, length = 15)

    private String isbn;
    @Column(nullable = false)

    private LocalDate creationDate;
    @ManyToOne
    @JoinColumn(name = "added_by_user_id", nullable = false)
    private User addedByUser;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Borrowing> borrowings;
    private boolean isBorrowed;

    public Book() {

    }

    public Book(Integer id, String title, String author, Integer publicationYear, String isbn, LocalDate creationDate, User addedByUser, boolean isBorrowed) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.publicationYear = publicationYear;
        this.isbn = isbn;
        this.creationDate = creationDate;
        this.addedByUser = addedByUser;
        this.isBorrowed = isBorrowed;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public User getAddedByUser() {
        return addedByUser;
    }

    private void setAddedByUserId(User addedByUser) {
        this.addedByUser = addedByUser;
    }

    public List<Borrowing> getBorrowings() {
        return borrowings;
    }

    public void setBorrowings(List<Borrowing> borrowings) {
        this.borrowings = borrowings;
    }

    public boolean isBorrowed() {
        return isBorrowed;
    }

    public void setBorrowed(boolean borrowed) {
        isBorrowed = borrowed;
    }
}
