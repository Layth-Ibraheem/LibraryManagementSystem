package com.layth.Library.Management.System.services;

import com.layth.Library.Management.System.entities.Librarian;
import com.layth.Library.Management.System.repositories.LibrarianRepository;
import com.layth.Library.Management.System.requestsAndResponses.librarians.AddNewLibrarianRequest;
import com.layth.Library.Management.System.requestsAndResponses.librarians.UpdateLibrarianRequest;
import com.layth.Library.Management.System.utils.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LibrarianService {
    private final LibrarianRepository librarianRepository;

    public LibrarianService(LibrarianRepository librarianRepository) {
        this.librarianRepository = librarianRepository;
    }

    public List<Librarian> getAllLibrarians() {
        return librarianRepository.findAll();
    }

    public Librarian addNewLibrarian(AddNewLibrarianRequest request) {
        Librarian librarian = new Librarian(null, request.getFirstName(), request.getLastName());
        return librarianRepository.save(librarian);
    }

    /** @throws ResourceNotFoundException if there is no librarian with this id */
    @Transactional
    public Librarian updateLibrarian(Integer id, UpdateLibrarianRequest request) {
        Librarian librarian = findLibrarian(id);
        librarian.setFirstName(request.getFirstName());
        librarian.setLastName(request.getLastName());
        return librarian; // managed entity: dirty checking writes the changes at commit
    }

    /** @throws ResourceNotFoundException if there is no librarian with this id */
    @Transactional
    public void deleteLibrarian(Integer id) {
        librarianRepository.delete(findLibrarian(id));
    }

    /** @throws ResourceNotFoundException if there is no librarian with this id */
    public Librarian getById(Integer id) {
        return findLibrarian(id);
    }

    private Librarian findLibrarian(Integer id) {
        return librarianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("There is no librarian with id " + id));
    }
}
