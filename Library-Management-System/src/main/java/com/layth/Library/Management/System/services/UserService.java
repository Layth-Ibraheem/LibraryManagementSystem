package com.layth.Library.Management.System.services;

import com.layth.Library.Management.System.entities.User;
import com.layth.Library.Management.System.repositories.UserRepository;
import com.layth.Library.Management.System.requestsAndResponses.auth.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository _repo;

    public User register(RegisterRequest request){
        User user = new User(null,request.getUserName(),request.getPassword(),request.getRoles());
        return _repo.save(user);
    }
    public User getByUserNameAndPassword(String userName,String password){
        Optional<User> user = _repo.findByUserNameAndPassword(userName,password);
        return user.orElse(null);
    }
}
