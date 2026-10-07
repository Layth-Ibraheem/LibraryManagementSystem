package com.layth.Library.Management.System.services;

import com.layth.Library.Management.System.utils.exceptions.ConflictException;
import com.layth.Library.Management.System.utils.exceptions.ResourceNotFoundException;
import com.layth.Library.Management.System.entities.Patron;
import com.layth.Library.Management.System.repositories.BorrowingRepository;
import com.layth.Library.Management.System.repositories.PatronsRepository;
import com.layth.Library.Management.System.requestsAndResponses.patrons.AddNewPatronRequest;
import com.layth.Library.Management.System.requestsAndResponses.patrons.UpdatePatronRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PatronService {
    private final PatronsRepository patronsRepository;
    private final BorrowingRepository borrowingRepository;

    public PatronService(PatronsRepository patronsRepository, BorrowingRepository borrowingRepository) {
        this.patronsRepository = patronsRepository;
        this.borrowingRepository = borrowingRepository;
    }

    public List<Patron> getAllPatrons(){
        return patronsRepository.findAll();
    }
    public Patron getPatronById(Integer id){
        Optional<Patron> patron = patronsRepository.findById(id);
        return patron.orElse(null);
    }
    public Patron addNewPatron(AddNewPatronRequest request){
        Patron patron = new Patron(null,request.getName(),request.getEmail(),request.getPhoneNumber());
        return patronsRepository.save(patron);
    }
    public Patron updatePatron(Integer id, UpdatePatronRequest request) {
        Optional<Patron> optionalPatron = patronsRepository.findById(id);
        if(optionalPatron.isPresent()){
            optionalPatron.get().setName(request.getName());
            optionalPatron.get().setEmail(request.getEmail());
            optionalPatron.get().setPhoneNumber(request.getPhoneNumber());

            return patronsRepository.save(optionalPatron.get());
        }
        throw new ResourceNotFoundException("There is no patron with id" + id);

    }
    /**
     * Deletes a patron who has never borrowed a book. A patron with loans is kept, because deleting
     * them would erase the loan history (409).
     */
    @Transactional
    public void deletePatron(Integer id){
        Patron patron = patronsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("There is no patron with id " + id));
        if (borrowingRepository.existsByPatronId(id)) {
            throw new ConflictException("Patron " + id + " has loan history and cannot be deleted");
        }
        patronsRepository.delete(patron);
    }
}
