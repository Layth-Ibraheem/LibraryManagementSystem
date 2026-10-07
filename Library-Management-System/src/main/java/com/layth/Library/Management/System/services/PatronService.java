package com.layth.Library.Management.System.services;

import com.layth.Library.Management.System.utils.exceptions.ResourceNotFoundException;
import com.layth.Library.Management.System.entities.Patron;
import com.layth.Library.Management.System.repositories.PatronsRepository;
import com.layth.Library.Management.System.requestsAndResponses.patrons.AddNewPatronRequest;
import com.layth.Library.Management.System.requestsAndResponses.patrons.UpdatePatronRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatronService {
    private final PatronsRepository patronsRepository;

    public PatronService(PatronsRepository patronsRepository) {
        this.patronsRepository = patronsRepository;
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
    public Patron updatePatron(Integer id, UpdatePatronRequest request) throws ResourceNotFoundException {
        Optional<Patron> optionalPatron = patronsRepository.findById(id);
        if(optionalPatron.isPresent()){
            optionalPatron.get().setName(request.getName());
            optionalPatron.get().setEmail(request.getEmail());
            optionalPatron.get().setPhoneNumber(request.getPhoneNumber());

            return patronsRepository.save(optionalPatron.get());
        }
        throw new ResourceNotFoundException("There is no patron with id" + id);

    }
    public boolean deletePatron(Integer id){
        Optional<Patron> patron = patronsRepository.findById(id);
        if(patron.isPresent()){
            patronsRepository.delete(patron.get());
            return true;
        }
        return false;
    }
}
