package com.estore.user.dto.request;

public record UserRegisterRequest(
        String email,
        String password,
        String firstName,
        String lastName
) {}
