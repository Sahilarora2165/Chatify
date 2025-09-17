package com.chatify.chat_backend.controller;

import com.chatify.chat_backend.dto.AuthResponseDTO;
import com.chatify.chat_backend.dto.UserLoginDTO;
import com.chatify.chat_backend.dto.UserRegistrationDTO;
import com.chatify.chat_backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }

    // Register a new user
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationDTO registrationDTO){
        try{
            String result = authService.register(registrationDTO);
            return ResponseEntity.ok(result); // ✅ 200 OK + token
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage()); // ❌ 400 Bad Request
        }
    }

    // Login user with email and password
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDTO loginDTO){
        try {
            AuthResponseDTO response = authService.login(loginDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // SHOW THE ACTUAL ERROR - don't hide it
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

}
