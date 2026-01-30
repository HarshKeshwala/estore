package com.estore.userservice.user.dto.request.validator;

import com.estore.userservice.user.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    private final UserRepository userRepository;

    public UniqueEmailValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        // null/empty values are handled by @NotBlank
        if (email == null || email.isEmpty()) {
            return true;
        }

        // check if email exists in database
        return !userRepository.existsByEmail(email);
    }
}
