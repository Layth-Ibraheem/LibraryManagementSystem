package com.layth.Library.Management.System.services;

import com.layth.Library.Management.System.entities.User;
import com.layth.Library.Management.System.entities.UserRoles;
import com.layth.Library.Management.System.repositories.UserRepository;
import com.layth.Library.Management.System.requestsAndResponses.auth.RegisterRequest;
import com.layth.Library.Management.System.utils.exceptions.ResourceNotFoundException;
import com.layth.Library.Management.System.utils.exceptions.UserNameAlreadyExistsException;
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
import static org.mockito.Mockito.never;
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
        assertEquals(UserRoles.NO_PERMISSIONS, saved.getValue().getRoles());
    }

    @Test
    void registerRejectsTakenUserName() {
        RegisterRequest request = new RegisterRequest();
        request.setUserName("alice");
        request.setPassword("correct-horse");
        when(userRepository.existsByUserName("alice")).thenReturn(true);

        assertThrows(UserNameAlreadyExistsException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any(User.class));
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

    @Test
    void updateRolesReplacesThePermissionSet() {
        User bob = new User(2, "bob", "hash", UserRoles.NO_PERMISSIONS);
        when(userRepository.findById(2)).thenReturn(Optional.of(bob));

        User updated = userService.updateRoles(2, UserRoles.ManageBooks.getRole() | UserRoles.ManagePatrons.getRole());

        assertEquals(6, updated.getRoles());
    }

    @Test
    void updateRolesOfUnknownUserThrowsNotFound() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateRoles(99, 1));
    }
}
