package com.layth.Library.Management.System.services;

import com.layth.Library.Management.System.entities.User;
import com.layth.Library.Management.System.repositories.UserRepository;
import com.layth.Library.Management.System.requestsAndResponses.auth.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void registerStoresBcryptHashInsteadOfRawPassword() {
        RegisterRequest request = new RegisterRequest();
        request.setUserName("alice");
        request.setPassword("correct-horse");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.register(request);

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        String storedPassword = saved.getValue().getPassword();
        assertNotEquals("correct-horse", storedPassword);
        assertTrue(storedPassword.startsWith("$2"), "expected a BCrypt hash");
        assertTrue(passwordEncoder.matches("correct-horse", storedPassword));
    }

    @Test
    void authenticateReturnsUserWhenPasswordMatchesHash() {
        User alice = new User(1, "alice", passwordEncoder.encode("correct-horse"), 0);
        when(userRepository.findByUserName("alice")).thenReturn(Optional.of(alice));

        assertEquals(Optional.of(alice), userService.authenticate("alice", "correct-horse"));
    }

    @Test
    void authenticateRejectsWrongPassword() {
        User alice = new User(1, "alice", passwordEncoder.encode("correct-horse"), 0);
        when(userRepository.findByUserName("alice")).thenReturn(Optional.of(alice));

        assertTrue(userService.authenticate("alice", "wrong-password").isEmpty());
    }

    @Test
    void authenticateRejectsUnknownUser() {
        when(userRepository.findByUserName("nobody")).thenReturn(Optional.empty());

        assertTrue(userService.authenticate("nobody", "whatever").isEmpty());
    }
}
