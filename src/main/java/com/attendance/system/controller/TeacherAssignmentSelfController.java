package com.attendance.system.controller;

import com.attendance.system.entity.TeacherAssignment;
import com.attendance.system.entity.Teacher;
import com.attendance.system.repository.TeacherRepository;
import com.attendance.system.service.TeacherAssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teacher/assignments")
@PreAuthorize("@roleService.isTeacher(authentication)")
public class TeacherAssignmentSelfController {

    private final TeacherAssignmentService teacherAssignmentService;
    private final TeacherRepository teacherRepository;

    public TeacherAssignmentSelfController(
            TeacherAssignmentService teacherAssignmentService,
            TeacherRepository teacherRepository) {

        this.teacherAssignmentService = teacherAssignmentService;
        this.teacherRepository = teacherRepository;
    }

    @GetMapping
    public ResponseEntity<List<TeacherAssignment>> getMyAssignments(
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        Teacher teacher = teacherRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Teacher profile not found"));

        return ResponseEntity.ok(
                teacherAssignmentService.getMyAssignments(teacher.getId())
        );
    }
}