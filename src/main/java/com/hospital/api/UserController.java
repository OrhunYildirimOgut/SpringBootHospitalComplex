// src/main/java/com/hospital/api/UserController.java
package com.hospital.api;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.api.projection.DoctorSummary;
import com.hospital.application.dto.UserCreateRequest;
import com.hospital.application.dto.UserResponse;
import com.hospital.application.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    public UserController(UserService userService) { this.userService = userService; }

    // Create user
    @PostMapping
    public UserResponse create(@RequestBody @Valid UserCreateRequest userCreateRequest) {
        return userService.createUser(userCreateRequest);
    }

    // List all users
    @GetMapping
    public List<UserResponse> list() {
        return userService.listUsers();
    }

    // Get user acc ID
    @GetMapping("/{id}")
    public UserResponse get(@PathVariable UUID id) {
        return userService.getUser(id);
    }

    @GetMapping("/doctors")
    public List<DoctorSummary> listDoctors() {
        return userService.listDoctorsWithRatings();
    }
}
