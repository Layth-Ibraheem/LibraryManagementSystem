package com.layth.Library.Management.System.controllers;

import com.layth.Library.Management.System.entities.Librarian;
import com.layth.Library.Management.System.entities.UserRoles;
import com.layth.Library.Management.System.requestsAndResponses.librarians.AddNewLibrarianRequest;
import com.layth.Library.Management.System.requestsAndResponses.librarians.LibrarianResponse;
import com.layth.Library.Management.System.requestsAndResponses.librarians.UpdateLibrarianRequest;
import com.layth.Library.Management.System.services.LibrarianService;
import com.layth.Library.Management.System.utils.annotations.RequireRole;
import com.layth.Library.Management.System.utils.exceptions.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/librarians")
public class LibrarianController {
    @Autowired
    private LibrarianService _librarianService;

    @PostMapping
    @RequireRole(role = UserRoles.ManageLibrarians)
    public ResponseEntity<?> createLibrarian(@Valid @RequestBody AddNewLibrarianRequest request) {
        Librarian addedLibrarian = _librarianService.addNewLibrarian(request);
        if (addedLibrarian.getId() == null) {
            return new ResponseEntity<>("Error happened, could not add the librarian", HttpStatus.FAILED_DEPENDENCY);
        }
        return new ResponseEntity<>(addedLibrarian, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @RequireRole(role = UserRoles.ManageLibrarians)
    public ResponseEntity<?> getLibrarian(@PathVariable(name = "id") Integer id) {
        Librarian librarian = _librarianService.getById(id);
        if (librarian != null) {
            return new ResponseEntity<>(MapToLibrarianResponse(librarian), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("No book with such id", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    @RequireRole(role = UserRoles.ManageLibrarians)
    public ResponseEntity<?> getAllLibrarians() {
        List<Librarian> librarians = _librarianService.getAllLibrarians();
        List<LibrarianResponse> librarianResponses = new ArrayList<>();
        for (Librarian librarian : librarians) {
            librarianResponses.add(MapToLibrarianResponse(librarian));
        }

        return new ResponseEntity<>(librarianResponses, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @RequireRole(role = UserRoles.ManageLibrarians)
    public ResponseEntity<Void> deleteLibrarian(@PathVariable(name = "id") Integer id) {
        boolean isDeleted = _librarianService.deleteLibrarian(id);
        return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @RequireRole(role = UserRoles.ManageLibrarians)
    public ResponseEntity<?> updateLibrarian(@PathVariable(name = "id") Integer id, @Valid @RequestBody UpdateLibrarianRequest request) throws ResourceNotFoundException {
        Librarian updatedLibrarian = _librarianService.updateLibrarian(id, request);
        if (updatedLibrarian != null) {
            return new ResponseEntity<>(MapToLibrarianResponse(updatedLibrarian), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static LibrarianResponse MapToLibrarianResponse(Librarian librarian) {
        return new LibrarianResponse(librarian.getId(), librarian.getFirstName(), librarian.getLastName());
    }
}
