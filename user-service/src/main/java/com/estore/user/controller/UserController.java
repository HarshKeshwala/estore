package com.estore.user.controller;

import com.estore.user.dto.request.UserRegisterRequest;
import com.estore.user.dto.response.UserResponse;
import com.estore.user.entity.User;
import com.estore.user.mapper.UserMapper;
import com.estore.user.repository.UserRepository;
import com.estore.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PostMapping()
    public ResponseEntity<String> register(@RequestBody UserRegisterRequest userRegisterRequest) {
        User user = this.userMapper.toEntity(userRegisterRequest);
        this.userService.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @GetMapping()
    public ResponseEntity<List<User>> getAll() {
        List<User> users = this.userService.getAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {

        User user  = this.userService.getById(id);
        if  (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(this.userMapper.toDto(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id) {
        this.userService.delete(id);
        return ResponseEntity.ok().build();
    }
}
