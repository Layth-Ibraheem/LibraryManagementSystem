package com.layth.Library.Management.System.controllers;

import com.layth.Library.Management.System.entities.User;
import com.layth.Library.Management.System.requestsAndResponses.auth.AuthResponse;
import com.layth.Library.Management.System.requestsAndResponses.auth.LoginRequest;
import com.layth.Library.Management.System.requestsAndResponses.auth.RegisterRequest;
import com.layth.Library.Management.System.services.UserService;
import com.layth.Library.Management.System.utils.jwt.JwtTokenUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final UserService userService;
    private final JwtTokenUtils jwtTokenUtils;

    public AuthenticationController(UserService userService, JwtTokenUtils jwtTokenUtils) {
        this.userService = userService;
        this.jwtTokenUtils = jwtTokenUtils;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toAuthResponse(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return userService.authenticate(request.getUserName(), request.getPassword())
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(toAuthResponse(user)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid user name or password"));
    }

    private AuthResponse toAuthResponse(User user) {
        String token = jwtTokenUtils.generateToken(user);
        return new AuthResponse(user.getId(), user.getUserName(), user.getRoles(), token);
    }
}
