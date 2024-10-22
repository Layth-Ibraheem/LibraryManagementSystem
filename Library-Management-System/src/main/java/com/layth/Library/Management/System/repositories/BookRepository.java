package com.layth.Library.Management.System.repositories;

import com.layth.Library.Management.System.entities.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book,Integer> {
}
