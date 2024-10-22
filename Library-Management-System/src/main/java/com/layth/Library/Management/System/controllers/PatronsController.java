package com.layth.Library.Management.System.controllers;

import com.layth.Library.Management.System.entities.Patron;
import com.layth.Library.Management.System.entities.UserRoles;
import com.layth.Library.Management.System.requestsAndResponses.patrons.AddNewPatronRequest;
import com.layth.Library.Management.System.requestsAndResponses.patrons.PatronResponse;
import com.layth.Library.Management.System.requestsAndResponses.patrons.UpdatePatronRequest;
import com.layth.Library.Management.System.services.PatronService;
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
@RequestMapping("/api/patrons")
public class PatronsController {
    @Autowired
    private PatronService _patronService;

    @GetMapping("/{id}")
    @RequireRole(role = UserRoles.ManagePatrons)
    public ResponseEntity<?> getById(@PathVariable(name = "id") Integer id) {
        Patron patron = _patronService.getPatronById(id);
        if (patron == null) {
            return new ResponseEntity<>("There is no patron with such id", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(MapToPatronResponse(patron), HttpStatus.OK);
    }

    @GetMapping
    @RequireRole(role = UserRoles.ManagePatrons)
    public ResponseEntity<?> getAllPatrons() {
        List<Patron> patrons = _patronService.getAllPatrons();
        List<PatronResponse> patronResponses = new ArrayList<>();

        for (Patron patron : patrons) {
            patronResponses.add(MapToPatronResponse(patron));
        }
        return new ResponseEntity<>(patronResponses, HttpStatus.OK);
    }

    @PostMapping
    @RequireRole(role = UserRoles.ManagePatrons)
    public ResponseEntity<?> addNewPatron(@Valid @RequestBody AddNewPatronRequest request) {
        Patron addedPatron = _patronService.addNewPatron(request);
        if (addedPatron.getId() == null) {
            return new ResponseEntity<>("Error happened, could not add the book", HttpStatus.FAILED_DEPENDENCY);
        } else {
            return new ResponseEntity<>(MapToPatronResponse(addedPatron), HttpStatus.CREATED);
        }
    }

    @PutMapping("/{id}")
    @RequireRole(role = UserRoles.ManagePatrons)
    public ResponseEntity<?> updatePatron(@PathVariable(name = "id") Integer id, @Valid @RequestBody UpdatePatronRequest request) throws ResourceNotFoundException {
        Patron updatedPatron = _patronService.updatePatron(id, request);
        if (updatedPatron != null) {
            return new ResponseEntity<>(MapToPatronResponse(updatedPatron), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("There is no patron with such id", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    @RequireRole(role = UserRoles.ManagePatrons)
    public ResponseEntity<?> deletePatron(@PathVariable(name = "id") Integer id) {
        boolean isDeleted = _patronService.deletePatron(id);
        return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    private static PatronResponse MapToPatronResponse(Patron patron) {
        return new PatronResponse(patron.getId(), patron.getName(), patron.getEmail(), patron.getPhoneNumber());
    }
}
