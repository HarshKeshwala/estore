package com.estore.user.service.impl;

import com.estore.user.dto.request.UserRegisterRequest;
import com.estore.user.entity.User;
import com.estore.user.repository.UserRepository;
import com.estore.user.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean save(UserRegisterRequest userRegisterRequest) {
        User user = new User();
        user.setEmail(userRegisterRequest.email());
        user.setPassword(userRegisterRequest.password());
        user.setFirstName(userRegisterRequest.firstName());
        user.setLastName(userRegisterRequest.lastName());
       this.userRepository.saveAndFlush(user);
       return true;
    }

    @Override
    public User getById(Long id) {
        return this.userRepository.findById(id).orElse(null);
    }

    @Override
    public User getByEmail(String email) {
        return null;
    }

    @Override
    public List<User> getAll() {
        return this.userRepository.findAll();
    }

    @Override
    public boolean delete(Long id) {
        this.userRepository.deleteById(id);
        return true;
    }
}
