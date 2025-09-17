// Location: src/main/java/com/chatify/chat_backend/service/AuthService.java

package com.chatify.chat_backend.service;

import com.chatify.chat_backend.dto.UserLoginDTO;
import com.chatify.chat_backend.dto.UserRegistrationDTO;
import com.chatify.chat_backend.dto.AuthResponseDTO;
import com.chatify.chat_backend.entity.User;
import com.chatify.chat_backend.repository.UserRepository;
import com.chatify.chat_backend.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    // ✅ Best Practice: Constructor Injection
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Register a new user with email.
     * @param request registration data (username, email, password)
     * @return success message
     * @throws RuntimeException if username or email already exists
     */
    @Transactional
    public String register(UserRegistrationDTO request) {
        // ✅ Best Practice: Check for duplicate username
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already taken: " + request.getUsername());
        }

        // ✅ Best Practice: Check for duplicate email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }

        // ✅ Best Practice: Hash password before saving
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        return "User registered successfully";
    }

    /**
     * Authenticate user by email and password, generate JWT token.
     * @param request login credentials (email, password)
     * @return JWT response with token and username
     * @throws AuthenticationException if credentials are invalid
     */
    public AuthResponseDTO login(UserLoginDTO request) {
        String email = request.getEmail();
        String password = request.getPassword();

        try {
            // Find user
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // authenticate the password
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );


            // Now generate token
            String token = jwtUtil.generateToken(email);
            Long userId = user.getId();

            return new AuthResponseDTO(token, user.getUsername(), userId, user.getEmail());

        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid email or password", e);
        }
    }
}