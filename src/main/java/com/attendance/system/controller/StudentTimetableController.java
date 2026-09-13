package com.attendance.system.controller;

import com.attendance.system.dto.response.StudentTimetableResponse;
import com.attendance.system.service.StudentTimetableService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/timetable")
@PreAuthorize("@roleService.isStudent(authentication)")
public class StudentTimetableController {

    private final StudentTimetableService studentTimetableService;

    public StudentTimetableController(
            StudentTimetableService studentTimetableService) {
        this.studentTimetableService = studentTimetableService;
    }

    @GetMapping
    public ResponseEntity<List<StudentTimetableResponse>> getMyTimetable(
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                studentTimetableService.getMyTimetable(userId)
        );
    }

    @GetMapping("/today")
    public ResponseEntity<List<StudentTimetableResponse>> getMyTodayTimetable(
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                studentTimetableService.getMyTodayTimetable(userId)
        );
    }
}