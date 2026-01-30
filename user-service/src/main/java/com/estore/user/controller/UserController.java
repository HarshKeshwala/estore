package com.estore.user.controller;

import com.estore.user.dto.request.UserRegisterRequest;
import com.estore.user.entity.User;
import com.estore.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping()
    public ResponseEntity<String> register(@RequestBody UserRegisterRequest userRegisterRequest) {
        this.userService.save(userRegisterRequest);
        return ResponseEntity.ok("User registered successfully");
    }

    @GetMapping()
    public ResponseEntity<List<User>> getAll() {

        List<User> users = this.userService.getAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {

        User user  = this.userService.getById(id);
        if  (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id) {
        this.userService.delete(id);
        return ResponseEntity.ok().build();
    }
}
