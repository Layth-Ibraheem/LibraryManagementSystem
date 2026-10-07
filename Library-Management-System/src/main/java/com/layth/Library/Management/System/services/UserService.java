package com.layth.Library.Management.System.services;

import com.layth.Library.Management.System.entities.User;
import com.layth.Library.Management.System.repositories.UserRepository;
import com.layth.Library.Management.System.requestsAndResponses.auth.RegisterRequest;
import com.layth.Library.Management.System.utils.exceptions.UserNameAlreadyExistsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest request) {
        if (userRepository.existsByUserName(request.getUserName())) {
            throw new UserNameAlreadyExistsException(request.getUserName());
        }
        String passwordHash = passwordEncoder.encode(request.getPassword());
        User user = new User(null, request.getUserName(), passwordHash, request.getRoles());
        return userRepository.save(user);
    }

    /**
     * Returns the user when the user name exists and the raw password matches its stored hash.
     * The lookup is by user name only: every BCrypt hash has its own salt, so it cannot be queried.
     */
    public Optional<User> authenticate(String userName, String rawPassword) {
        return userRepository.findByUserName(userName)
                .filter(user -> passwordEncoder.matches(rawPassword, user.getPassword()));
    }
}
