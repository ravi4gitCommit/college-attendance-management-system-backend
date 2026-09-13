package com.attendance.system.controller;

import com.attendance.system.dto.response.TeacherProfileResponse;
import com.attendance.system.service.TeacherProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teacher/profile")
@PreAuthorize("@roleService.isTeacher(authentication)")
public class TeacherProfileController {

    private final TeacherProfileService teacherProfileService;

    public TeacherProfileController(
            TeacherProfileService teacherProfileService) {
        this.teacherProfileService = teacherProfileService;
    }

    @GetMapping
    public ResponseEntity<TeacherProfileResponse> getMyProfile(
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                teacherProfileService.getMyProfile(userId)
        );
    }
}