package com.layth.Library.Management.System.controllers;

import com.layth.Library.Management.System.entities.Patron;
import com.layth.Library.Management.System.entities.UserRoles;
import com.layth.Library.Management.System.requestsAndResponses.patrons.AddNewPatronRequest;
import com.layth.Library.Management.System.requestsAndResponses.patrons.PatronResponse;
import com.layth.Library.Management.System.requestsAndResponses.patrons.UpdatePatronRequest;
import com.layth.Library.Management.System.services.PatronService;
import com.layth.Library.Management.System.utils.annotations.RequireRole;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/patrons")
public class PatronsController {
    private final PatronService patronService;

    public PatronsController(PatronService patronService) {
        this.patronService = patronService;
    }

    @GetMapping("/{id}")
    @RequireRole(role = UserRoles.ManagePatrons)
    public ResponseEntity<PatronResponse> getById(@PathVariable(name = "id") Integer id) {
        return ResponseEntity.ok(toResponse(patronService.getPatronById(id)));
    }

    @GetMapping
    @RequireRole(role = UserRoles.ManagePatrons)
    public ResponseEntity<List<PatronResponse>> getAllPatrons() {
        return ResponseEntity.ok(patronService.getAllPatrons().stream().map(PatronsController::toResponse).toList());
    }

    @PostMapping
    @RequireRole(role = UserRoles.ManagePatrons)
    public ResponseEntity<PatronResponse> addNewPatron(@Valid @RequestBody AddNewPatronRequest request) {
        Patron addedPatron = patronService.addNewPatron(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(addedPatron.getId()).toUri();
        return ResponseEntity.created(location).body(toResponse(addedPatron));
    }

    @PutMapping("/{id}")
    @RequireRole(role = UserRoles.ManagePatrons)
    public ResponseEntity<PatronResponse> updatePatron(@PathVariable(name = "id") Integer id, @Valid @RequestBody UpdatePatronRequest request) {
        return ResponseEntity.ok(toResponse(patronService.updatePatron(id, request)));
    }

    @DeleteMapping("/{id}")
    @RequireRole(role = UserRoles.ManagePatrons)
    public ResponseEntity<Void> deletePatron(@PathVariable(name = "id") Integer id) {
        patronService.deletePatron(id);
        return ResponseEntity.noContent().build();
    }

    private static PatronResponse toResponse(Patron patron) {
        return new PatronResponse(patron.getId(), patron.getName(), patron.getEmail(), patron.getPhoneNumber());
    }
}
