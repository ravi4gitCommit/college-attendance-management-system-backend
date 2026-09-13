package com.attendance.system.controller;

import com.attendance.system.entity.User;
import com.attendance.system.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Get user by ID.
    // Access is allowed only when the logged-in user has permission
    // to access the requested user's record.
    @GetMapping("/{id}")
    @PreAuthorize("@roleService.canAccessUser(authentication, #id)")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {

        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}