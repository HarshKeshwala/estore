package com.estore.userservice.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserSigninRequest(
        @NotBlank
        @Email
        String email,

        @NotBlank
        String password
) {}
