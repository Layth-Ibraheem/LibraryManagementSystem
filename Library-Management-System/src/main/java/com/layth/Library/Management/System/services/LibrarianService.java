package com.layth.Library.Management.System.services;

import com.layth.Library.Management.System.utils.exceptions.ResourceNotFoundException;
import com.layth.Library.Management.System.entities.Librarian;
import com.layth.Library.Management.System.repositories.LibrarianRepository;
import com.layth.Library.Management.System.requestsAndResponses.librarians.AddNewLibrarianRequest;
import com.layth.Library.Management.System.requestsAndResponses.librarians.UpdateLibrarianRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LibrarianService {
    @Autowired
    private LibrarianRepository _repo;

    public List<Librarian> getAllLibrarians(){
        return _repo.findAll();
    }

    public Librarian addNewLibrarian(AddNewLibrarianRequest request){
        Librarian librarian = new Librarian(null,request.getFirstName(),request.getLastName());
        return _repo.save(librarian);
    }
    public Librarian updateLibrarian(Integer id, UpdateLibrarianRequest request) throws ResourceNotFoundException {
        Optional<Librarian> optionalLibrarian = _repo.findById(id);
        if(optionalLibrarian.isPresent()){
            Librarian librarian = optionalLibrarian.get();
            librarian.setFirstName(request.getFirstName());
            librarian.setLastName(request.getLastName());
            return _repo.save(librarian);
        } else {
            throw new ResourceNotFoundException("There is no such librarian with id: " + id);
        }
    }

    public boolean deleteLibrarian(Integer id) {
        Optional<Librarian> librarian = _repo.findById(id);

        if (librarian.isPresent()) {
            _repo.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    public Librarian getById(Integer id){
        Optional<Librarian> librarian = _repo.findById(id);
        return librarian.orElse(null);
    }

}
