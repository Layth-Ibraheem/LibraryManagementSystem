package com.layth.Library.Management.System.controllers;

import com.layth.Library.Management.System.entities.User;
import com.layth.Library.Management.System.requestsAndResponses.auth.AuthResponse;
import com.layth.Library.Management.System.requestsAndResponses.auth.LoginRequest;
import com.layth.Library.Management.System.requestsAndResponses.auth.RegisterRequest;
import com.layth.Library.Management.System.services.UserService;
import com.layth.Library.Management.System.utils.jwt.JwtTokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    @Autowired
    private UserService _userService;

    @Autowired
    private JwtTokenUtils _jwt;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = _userService.register(request);
            String token = _jwt.generateToken(user);
            return new ResponseEntity<>(new AuthResponse(user.getId(), user.getUserName(), user.getRoles(), token), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.FAILED_DEPENDENCY);
//            return ResponseEntity.f().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = _userService.getByUserNameAndPassword(request.getUserName(), request.getPassword());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials");
        }
        String token = _jwt.generateToken(user);
        return new ResponseEntity<>(new AuthResponse(user.getId(), user.getUserName(), user.getRoles(), token), HttpStatus.OK);
    }
}
