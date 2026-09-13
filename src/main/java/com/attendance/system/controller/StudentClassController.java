package com.attendance.system.controller;

import com.attendance.system.dto.response.StudentClassResponse;
import com.attendance.system.service.StudentClassService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/classes")
@PreAuthorize("@roleService.isStudent(authentication)")
public class StudentClassController {

    private final StudentClassService studentClassService;

    public StudentClassController(
            StudentClassService studentClassService) {
        this.studentClassService = studentClassService;
    }

    @GetMapping
    public ResponseEntity<List<StudentClassResponse>> getMyClasses(
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                studentClassService.getMyClasses(userId)
        );
    }
}