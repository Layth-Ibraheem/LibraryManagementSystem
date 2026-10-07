package com.layth.Library.Management.System.services;

import com.layth.Library.Management.System.utils.exceptions.ResourceNotFoundException;
import com.layth.Library.Management.System.entities.Librarian;
import com.layth.Library.Management.System.repositories.LibrarianRepository;
import com.layth.Library.Management.System.requestsAndResponses.librarians.AddNewLibrarianRequest;
import com.layth.Library.Management.System.requestsAndResponses.librarians.UpdateLibrarianRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LibrarianService {
    private final LibrarianRepository librarianRepository;

    public LibrarianService(LibrarianRepository librarianRepository) {
        this.librarianRepository = librarianRepository;
    }

    public List<Librarian> getAllLibrarians(){
        return librarianRepository.findAll();
    }

    public Librarian addNewLibrarian(AddNewLibrarianRequest request){
        Librarian librarian = new Librarian(null,request.getFirstName(),request.getLastName());
        return librarianRepository.save(librarian);
    }
    public Librarian updateLibrarian(Integer id, UpdateLibrarianRequest request) {
        Optional<Librarian> optionalLibrarian = librarianRepository.findById(id);
        if(optionalLibrarian.isPresent()){
            Librarian librarian = optionalLibrarian.get();
            librarian.setFirstName(request.getFirstName());
            librarian.setLastName(request.getLastName());
            return librarianRepository.save(librarian);
        } else {
            throw new ResourceNotFoundException("There is no such librarian with id: " + id);
        }
    }

    public boolean deleteLibrarian(Integer id) {
        Optional<Librarian> librarian = librarianRepository.findById(id);

        if (librarian.isPresent()) {
            librarianRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    public Librarian getById(Integer id){
        Optional<Librarian> librarian = librarianRepository.findById(id);
        return librarian.orElse(null);
    }

}
