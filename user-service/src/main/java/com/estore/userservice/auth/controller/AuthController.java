package com.estore.userservice.auth.controller;

import com.estore.userservice.auth.dto.request.AuthRequest;
import com.estore.userservice.auth.dto.response.AuthResponse;
import com.estore.userservice.auth.service.AuthService;
import com.estore.userservice.user.dto.request.UserRegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegisterRequest userRegisterRequest){
        return ResponseEntity.ok(authService.register(userRegisterRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest){
        return ResponseEntity.ok(authService.authenticate(authRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestHeader("Authorization") String refreshToken
    ) {
        String token = refreshToken.substring(7); // Remove "Bearer " prefix
        return ResponseEntity.ok(authService.refreshToken(token));
    }
}
