package com.attendance.system.controller;

import com.attendance.system.entity.ClassSession;
import com.attendance.system.entity.Teacher;
import com.attendance.system.repository.TeacherAssignmentRepository;
import com.attendance.system.repository.TeacherRepository;
import com.attendance.system.service.ClassSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teacher/classes")
@PreAuthorize("@roleService.isTeacher(authentication)")
public class TeacherClassSessionController {

    private final ClassSessionService classSessionService;
    private final TeacherRepository teacherRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;

    public TeacherClassSessionController(
            ClassSessionService classSessionService,
            TeacherRepository teacherRepository,
            TeacherAssignmentRepository teacherAssignmentRepository
    ) {
        this.classSessionService = classSessionService;
        this.teacherRepository = teacherRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
    }

    @GetMapping
    public ResponseEntity<List<ClassSession>> getMyClasses(
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());

        Teacher teacher = teacherRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Teacher profile not found"));

        List<ClassSession> sessions = new ArrayList<>();

        teacherAssignmentRepository
                .findByTeacherId(teacher.getId())
                .forEach(assignment ->
                        sessions.addAll(
                                classSessionService
                                        .getSessionsByTeacherAssignment(
                                                assignment.getId()
                                        )
                        )
                );

        return ResponseEntity.ok(sessions);
    }
}