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
@RequestMapping("/api/v1/college-admin/teacher-assignments")
@PreAuthorize("@roleService.isCollegeAdmin(authentication)")
public class TeacherAssignmentController {

    private final TeacherAssignmentService teacherAssignmentService;

    public TeacherAssignmentController(
            TeacherAssignmentService teacherAssignmentService
    ) {
        this.teacherAssignmentService = teacherAssignmentService;
    }

    // Get all teacher assignments for the authenticated admin's college.
    @GetMapping
    public ResponseEntity<List<TeacherAssignment>> getAllAssignments(
            Authentication authentication
    ) {
        UUID adminUserId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                teacherAssignmentService.getAllAssignmentsForCollege(
                        adminUserId
                )
        );
    }

    // Get assignment by ID only if it belongs to the admin's college.
    @GetMapping("/{id}")
    public ResponseEntity<TeacherAssignment> getAssignmentById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        UUID adminUserId = UUID.fromString(authentication.getName());

        return teacherAssignmentService
                .getAssignmentByIdForCollege(id, adminUserId)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    // Get assignments by teacher, only for a teacher in the admin's college.
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<TeacherAssignment>> getByTeacher(
            @PathVariable Long teacherId,
            Authentication authentication
    ) {
        UUID adminUserId = UUID.fromString(authentication.getName());

        try {
            return ResponseEntity.ok(
                    teacherAssignmentService
                            .getAssignmentsByTeacherForCollege(
                                    teacherId,
                                    adminUserId
                            )
            );
        } catch (IllegalArgumentException e) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    e.getMessage()
            );
        }
    }

    // Get assignments by subject offering, only for the admin's college.
    @GetMapping("/subject-offering/{subjectOfferingId}")
    public ResponseEntity<List<TeacherAssignment>> getBySubjectOffering(
            @PathVariable Long subjectOfferingId,
            Authentication authentication
    ) {
        UUID adminUserId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                teacherAssignmentService
                        .getAssignmentsBySubjectOfferingForCollege(
                                subjectOfferingId,
                                adminUserId
                        )
        );
    }

    // Create assignment only when teacher and subject offering belong to the admin's college.
    @PostMapping
    public ResponseEntity<TeacherAssignment> createAssignment(
            @RequestBody TeacherAssignment assignment,
            Authentication authentication
    ) {
        UUID adminUserId = UUID.fromString(authentication.getName());

        TeacherAssignment createdAssignment =
                teacherAssignmentService.createAssignmentForCollege(
                        assignment,
                        adminUserId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdAssignment);
    }

    // Update assignment only when existing and new references belong to the admin's college.
    @PutMapping("/{id}")
    public ResponseEntity<TeacherAssignment> updateAssignment(
            @PathVariable Long id,
            @RequestBody TeacherAssignment assignment,
            Authentication authentication
    ) {
        UUID adminUserId = UUID.fromString(authentication.getName());

        return teacherAssignmentService
                .updateAssignmentForCollege(
                        id,
                        assignment,
                        adminUserId
                )
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }


// Delete assignment only if it belongs to the admin's college.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(
            @PathVariable Long id,
            Authentication authentication
    ) {
        UUID adminUserId = UUID.fromString(authentication.getName());

        try {
            boolean deleted =
                    teacherAssignmentService.deleteAssignmentForCollege(
                            id,
                            adminUserId
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
