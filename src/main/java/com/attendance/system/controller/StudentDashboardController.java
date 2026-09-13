package com.attendance.system.controller;

import com.attendance.system.dto.response.StudentDashboardResponse;
import com.attendance.system.service.StudentDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/dashboard")
@PreAuthorize("@roleService.isStudent(authentication)")
public class StudentDashboardController {

    private final StudentDashboardService studentDashboardService;

    public StudentDashboardController(
            StudentDashboardService studentDashboardService) {
        this.studentDashboardService = studentDashboardService;
    }

    @GetMapping
    public ResponseEntity<StudentDashboardResponse> getMyDashboard(
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                studentDashboardService.getMyDashboard(userId)
        );
    }
}