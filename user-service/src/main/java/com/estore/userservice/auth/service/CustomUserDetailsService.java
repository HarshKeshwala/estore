package com.estore.userservice.auth.service;

import com.estore.userservice.auth.security.CustomUserDetails;
import com.estore.userservice.user.entity.User;
import com.estore.userservice.user.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public  CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        Optional<User> user = this.userRepository.findByEmail(username);
        if (user.isEmpty()) {
            throw  new UsernameNotFoundException(username);
        }
        return new CustomUserDetails(user.get());
    }
}
