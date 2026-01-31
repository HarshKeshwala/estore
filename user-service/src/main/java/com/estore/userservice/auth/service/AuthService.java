package com.estore.userservice.auth.service;

import com.estore.userservice.auth.dto.request.AuthRequest;
import com.estore.userservice.auth.dto.response.AuthResponse;
import com.estore.userservice.auth.security.CustomUserDetails;
import com.estore.userservice.user.dto.request.UserRegisterRequest;
import com.estore.userservice.user.entity.User;
import com.estore.userservice.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/*
    Service to handle Authentication (later authorization as well) related actions
    i.e. generating JWT token, validating JWT token, in-validating JWT tokens
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }



    // register a new user
    public ResponseEntity<?> register(UserRegisterRequest request) {
        // check if user already exists
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already registered");
        }

        // Create new user
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        userRepository.save(user);

//        // Generate tokens
//        CustomUserDetails userDetails = new CustomUserDetails(user);
//        String accessToken = jwtService.generateToken(userDetails);
//        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return ResponseEntity.ok().build();
    }

    // authenticate user and generate jwt token
    public AuthResponse authenticate(AuthRequest request) {
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        System.out.println("RIGHT BEFORE -----------> ");
        // Load user details
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("RIGHT AFTER -----------> ");
        CustomUserDetails userDetails = new CustomUserDetails(user);

        // Generate tokens
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return new AuthResponse(accessToken, refreshToken);
    }

    // refresh access token
    public AuthResponse refreshToken(String refreshToken) {
        // Extract username from refresh token
        String userEmail = jwtService.extractUsername(refreshToken);

        // Load user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CustomUserDetails userDetails = new CustomUserDetails(user);

        // Validate refresh token
        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new RuntimeException("Invalid refresh token");
        }

        // Generate new access token
        String accessToken = jwtService.generateToken(userDetails);

        return new AuthResponse(accessToken, refreshToken);
    }
}
