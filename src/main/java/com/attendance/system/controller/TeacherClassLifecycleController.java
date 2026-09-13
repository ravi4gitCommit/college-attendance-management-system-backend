package com.attendance.system.controller;

import com.attendance.system.entity.ClassSession;
import com.attendance.system.service.ClassSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teacher/classes")
@PreAuthorize("@roleService.isTeacher(authentication)")
public class TeacherClassLifecycleController {

    private final ClassSessionService classSessionService;

    public TeacherClassLifecycleController(
            ClassSessionService classSessionService
    ) {
        this.classSessionService = classSessionService;
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<?> startClass(
            @PathVariable Long id,
            Authentication authentication
    ) {
        UUID startedBy = UUID.fromString(authentication.getName());

        try {
            ClassSession session =
                    classSessionService.startSession(id, startedBy);

            return ResponseEntity.ok(session);

        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(409)
                    .body(e.getMessage());
        }
    }
}