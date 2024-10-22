package com.layth.Library.Management.System.requestsAndResponses.books;

import com.layth.Library.Management.System.entities.User;

import java.io.Serializable;
import java.time.LocalDate;

    public class BookResponse implements Serializable {
        private Integer id;
        private String title;
        private String author;
        private Integer publicationYear;
        private String isbn;
        private LocalDate creationDate;
        private boolean isBorrowed;

        private Integer addedByUserId;

        public BookResponse(Integer id, String title, String author, Integer publicationYear, String isbn, LocalDate creationDate, boolean isBorrowed, Integer addedByUserId) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.publicationYear = publicationYear;
            this.isbn = isbn;
            this.creationDate = creationDate;
            this.isBorrowed = isBorrowed;
            this.addedByUserId = addedByUserId;
        }

        public BookResponse() {
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

        public Integer getAddedByUserId() {
            return addedByUserId;
        }

        public void setAddedByUserId(Integer addedByUserId) {
            this.addedByUserId = addedByUserId;
        }

        public boolean isBorrowed() {
            return isBorrowed;
        }

        public void setBorrowed(boolean borrowed) {
            isBorrowed = borrowed;
        }
    }


