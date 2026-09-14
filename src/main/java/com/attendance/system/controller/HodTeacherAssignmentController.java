package com.attendance.system.controller;

import com.attendance.system.entity.TeacherAssignment;
import com.attendance.system.service.TeacherAssignmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hod/teacher-assignments")
@PreAuthorize("@roleService.isHOD(authentication)")
public class HodTeacherAssignmentController {

    private final TeacherAssignmentService teacherAssignmentService;

    public HodTeacherAssignmentController(
            TeacherAssignmentService teacherAssignmentService
    ) {
        this.teacherAssignmentService = teacherAssignmentService;
    }

    // Get all teacher assignments for the HOD's department.
    @GetMapping
    public ResponseEntity<List<TeacherAssignment>> getAllAssignments(
            Authentication authentication
    ) {
        UUID hodUserId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                teacherAssignmentService.getAllAssignmentsForHod(hodUserId)
        );
    }

    // Get one assignment only if it belongs to the HOD's department.
    @GetMapping("/{id}")
    public ResponseEntity<TeacherAssignment> getAssignmentById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        UUID hodUserId = UUID.fromString(authentication.getName());

        return teacherAssignmentService
                .getAssignmentByIdForHod(id, hodUserId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Create a teacher -> subject assignment inside the HOD's department.
    @PostMapping
    public ResponseEntity<TeacherAssignment> createAssignment(
            @RequestBody TeacherAssignment assignment,
            Authentication authentication
    ) {
        try {
            UUID hodUserId = UUID.fromString(authentication.getName());

            TeacherAssignment createdAssignment =
                    teacherAssignmentService.createAssignmentForHod(
                            assignment,
                            hodUserId
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(createdAssignment);

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }
    }

    // Update an assignment only inside the HOD's department.
    @PutMapping("/{id}")
    public ResponseEntity<TeacherAssignment> updateAssignment(
            @PathVariable Long id,
            @RequestBody TeacherAssignment assignment,
            Authentication authentication
    ) {
        try {
            UUID hodUserId = UUID.fromString(authentication.getName());

            return teacherAssignmentService
                    .updateAssignmentForHod(
                            id,
                            assignment,
                            hodUserId
                    )
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }
    }

    // Delete an assignment only when existing business rules allow deletion.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(
            @PathVariable Long id,
            Authentication authentication
    ) {
        try {
            UUID hodUserId = UUID.fromString(authentication.getName());

            boolean deleted =
                    teacherAssignmentService.deleteAssignmentForHod(
                            id,
                            hodUserId
                    );

            if (!deleted) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.noContent().build();

        } catch (IllegalStateException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    e.getMessage()
            );
        }
    }
}