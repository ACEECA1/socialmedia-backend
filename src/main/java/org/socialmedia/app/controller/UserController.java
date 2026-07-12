package org.socialmedia.app.controller;

import lombok.AllArgsConstructor;
import org.socialmedia.app.dto.ApiResponse;
import org.socialmedia.app.model.user.User;
import org.socialmedia.app.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {
    private final UserService userService;
    @GetMapping("/health")
    public ApiResponse<String> healthCheck() {
        return new ApiResponse<>(true, 200, "User service is healthy and running!", null);
    }

    @PostMapping
    public ApiResponse<User> createUser(@RequestBody User user) {
        return new ApiResponse<>(true, 200, "User created successfully", userService.createUser(user).orElse(null));
    }

    @GetMapping
    public ApiResponse<List<User>> getUsers() {
        return new ApiResponse<>(true, 200, "Users retrieved successfully", userService.getAllUsers().orElse(null));
    }
}
