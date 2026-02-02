package com.estore.userservice.auth.service;

import com.estore.userservice.auth.dto.request.UserSigninRequest;
import com.estore.userservice.auth.dto.request.UserRegisterRequest;
import com.estore.userservice.auth.dto.response.AuthResponse;
import com.estore.userservice.auth.security.CustomUserDetails;
import com.estore.userservice.user.entity.Role;
import com.estore.userservice.user.entity.User;
import com.estore.userservice.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

        // Create new user with default USER role
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRole(Role.USER);
        userRepository.save(user);

        return ResponseEntity.noContent().build();
    }

    // authenticate user and generate jwt token
    public AuthResponse authenticate(UserSigninRequest request) {
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        // load user details
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        // translate to custom UserDetails
        CustomUserDetails userDetails = new CustomUserDetails(user);

        // generate tokens
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return new AuthResponse(accessToken, refreshToken);
    }

    // refresh access token
    public AuthResponse refreshToken(String refreshToken) {
        // extract username from refresh token
        String userEmail = jwtService.extractUsername(refreshToken);

        // load user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

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
