package com.layth.Library.Management.System.controllers;

import com.layth.Library.Management.System.entities.Librarian;
import com.layth.Library.Management.System.entities.UserRoles;
import com.layth.Library.Management.System.requestsAndResponses.librarians.AddNewLibrarianRequest;
import com.layth.Library.Management.System.requestsAndResponses.librarians.LibrarianResponse;
import com.layth.Library.Management.System.requestsAndResponses.librarians.UpdateLibrarianRequest;
import com.layth.Library.Management.System.services.LibrarianService;
import com.layth.Library.Management.System.utils.annotations.RequireRole;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/librarians")
public class LibrarianController {
    private final LibrarianService librarianService;

    public LibrarianController(LibrarianService librarianService) {
        this.librarianService = librarianService;
    }

    @PostMapping
    @RequireRole(role = UserRoles.ManageLibrarians)
    public ResponseEntity<LibrarianResponse> createLibrarian(@Valid @RequestBody AddNewLibrarianRequest request) {
        Librarian addedLibrarian = librarianService.addNewLibrarian(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(addedLibrarian.getId()).toUri();
        return ResponseEntity.created(location).body(toResponse(addedLibrarian));
    }

    @GetMapping("/{id}")
    @RequireRole(role = UserRoles.ManageLibrarians)
    public ResponseEntity<LibrarianResponse> getLibrarian(@PathVariable(name = "id") Integer id) {
        return ResponseEntity.ok(toResponse(librarianService.getById(id)));
    }

    @GetMapping
    @RequireRole(role = UserRoles.ManageLibrarians)
    public ResponseEntity<List<LibrarianResponse>> getAllLibrarians() {
        return ResponseEntity.ok(librarianService.getAllLibrarians().stream().map(LibrarianController::toResponse).toList());
    }

    @DeleteMapping("/{id}")
    @RequireRole(role = UserRoles.ManageLibrarians)
    public ResponseEntity<Void> deleteLibrarian(@PathVariable(name = "id") Integer id) {
        librarianService.deleteLibrarian(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @RequireRole(role = UserRoles.ManageLibrarians)
    public ResponseEntity<LibrarianResponse> updateLibrarian(@PathVariable(name = "id") Integer id, @Valid @RequestBody UpdateLibrarianRequest request) {
        return ResponseEntity.ok(toResponse(librarianService.updateLibrarian(id, request)));
    }

    private static LibrarianResponse toResponse(Librarian librarian) {
        return new LibrarianResponse(librarian.getId(), librarian.getFirstName(), librarian.getLastName());
    }
}
