package com.layth.Library.Management.System.services;

import com.layth.Library.Management.System.entities.Patron;
import com.layth.Library.Management.System.repositories.BorrowingRepository;
import com.layth.Library.Management.System.repositories.PatronsRepository;
import com.layth.Library.Management.System.requestsAndResponses.patrons.AddNewPatronRequest;
import com.layth.Library.Management.System.requestsAndResponses.patrons.UpdatePatronRequest;
import com.layth.Library.Management.System.utils.exceptions.ConflictException;
import com.layth.Library.Management.System.utils.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PatronService {
    private final PatronsRepository patronsRepository;
    private final BorrowingRepository borrowingRepository;

    public PatronService(PatronsRepository patronsRepository, BorrowingRepository borrowingRepository) {
        this.patronsRepository = patronsRepository;
        this.borrowingRepository = borrowingRepository;
    }

    public List<Patron> getAllPatrons() {
        return patronsRepository.findAll();
    }

    /** @throws ResourceNotFoundException if there is no patron with this id */
    public Patron getPatronById(Integer id) {
        return findPatron(id);
    }

    public Patron addNewPatron(AddNewPatronRequest request) {
        Patron patron = new Patron(null, request.getName(), request.getEmail(), request.getPhoneNumber());
        return patronsRepository.save(patron);
    }

    /** @throws ResourceNotFoundException if there is no patron with this id */
    @Transactional
    public Patron updatePatron(Integer id, UpdatePatronRequest request) {
        Patron patron = findPatron(id);
        patron.setName(request.getName());
        patron.setEmail(request.getEmail());
        patron.setPhoneNumber(request.getPhoneNumber());
        return patron; // managed entity: dirty checking writes the changes at commit
    }

    /**
     * Deletes a patron who has never borrowed a book. A patron with loans is kept, because deleting
     * them would erase the loan history (409).
     *
     * @throws ResourceNotFoundException if there is no patron with this id
     */
    @Transactional
    public void deletePatron(Integer id) {
        Patron patron = findPatron(id);
        if (borrowingRepository.existsByPatronId(id)) {
            throw new ConflictException("Patron " + id + " has loan history and cannot be deleted");
        }
        patronsRepository.delete(patron);
    }

    private Patron findPatron(Integer id) {
        return patronsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("There is no patron with id " + id));
    }
}
