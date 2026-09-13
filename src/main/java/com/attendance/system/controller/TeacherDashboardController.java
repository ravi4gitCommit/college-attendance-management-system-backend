package com.attendance.system.controller;

import com.attendance.system.dto.response.TeacherDashboardResponse;
import com.attendance.system.service.TeacherDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teacher/dashboard")
@PreAuthorize("@roleService.isTeacher(authentication)")
public class TeacherDashboardController {

    private final TeacherDashboardService teacherDashboardService;

    public TeacherDashboardController(
            TeacherDashboardService teacherDashboardService
    ) {
        this.teacherDashboardService = teacherDashboardService;
    }

    @GetMapping
    public ResponseEntity<TeacherDashboardResponse> getMyDashboard(
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                teacherDashboardService.getMyDashboard(userId)
        );
    }
}