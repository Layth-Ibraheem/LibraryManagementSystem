package com.layth.Library.Management.System;

import com.layth.Library.Management.System.entities.Book;
import com.layth.Library.Management.System.entities.Librarian;
import com.layth.Library.Management.System.repositories.LibrarianRepository;
import com.layth.Library.Management.System.requestsAndResponses.auth.AuthResponse;
import com.layth.Library.Management.System.services.LibrarianService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class LibraryManagementSystemApplication {

	public static void main(String[] args) {

		SpringApplication.run(LibraryManagementSystemApplication.class, args);
	}

}
