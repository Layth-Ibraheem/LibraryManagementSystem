package com.layth.Library.Management.System.controllers;

import com.layth.Library.Management.System.entities.User;
import com.layth.Library.Management.System.requestsAndResponses.auth.AuthResponse;
import com.layth.Library.Management.System.requestsAndResponses.auth.LoginRequest;
import com.layth.Library.Management.System.requestsAndResponses.auth.RegisterRequest;
import com.layth.Library.Management.System.services.UserService;
import com.layth.Library.Management.System.utils.jwt.JwtTokenUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);
            String token = jwtTokenUtils.generateToken(user);
            return new ResponseEntity<>(new AuthResponse(user.getId(), user.getUserName(), user.getRoles(), token), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.FAILED_DEPENDENCY);
//            return ResponseEntity.f().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userService.getByUserNameAndPassword(request.getUserName(), request.getPassword());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials");
        }
        String token = jwtTokenUtils.generateToken(user);
        return new ResponseEntity<>(new AuthResponse(user.getId(), user.getUserName(), user.getRoles(), token), HttpStatus.OK);
    }
}
