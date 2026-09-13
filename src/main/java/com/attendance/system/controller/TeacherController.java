package com.attendance.system.controller;

import com.attendance.system.entity.Teacher;
import com.attendance.system.service.TeacherService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/college-admin/teachers")
@PreAuthorize("@roleService.isCollegeAdmin(authentication)")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    // Get all teachers belonging to the logged-in College Admin's college.
    @GetMapping
    public ResponseEntity<List<Teacher>> getAllTeachers(
            Authentication authentication
    ) {

        UUID adminUserId =
                UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                teacherService.getAllTeachers(adminUserId)
        );
    }

    // Get one teacher only if the teacher belongs to
    // the logged-in College Admin's college.
    @GetMapping("/{id}")
    public ResponseEntity<Teacher> getTeacherById(
            @PathVariable Long id,
            Authentication authentication
    ) {

        UUID adminUserId =
                UUID.fromString(authentication.getName());

        return teacherService
                .getTeacherById(id, adminUserId)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    // Get teachers by department.
    // The service verifies that the department belongs
    // to the logged-in College Admin's college.


    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<Teacher>> getTeachersByDepartment(
            @PathVariable Long departmentId,
            Authentication authentication
    ) {
        UUID adminUserId =
                UUID.fromString(authentication.getName());

        try {
            return ResponseEntity.ok(
                    teacherService.getTeachersByDepartment(
                            departmentId,
                            adminUserId
                    )
            );
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    e.getMessage()
            );
        }
    }


    // Create teacher inside the logged-in College Admin's college.
    @PostMapping
    public ResponseEntity<Teacher> createTeacher(
            @RequestBody Teacher teacher,
            Authentication authentication
    ) {
        UUID adminUserId =
                UUID.fromString(authentication.getName());

        try {
            Teacher createdTeacher =
                    teacherService.createTeacher(
                            teacher,
                            adminUserId
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(createdTeacher);

        } catch (ResponseStatusException e) {
            throw e;

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }
    }


    // Update teacher only if the teacher belongs
    // to the logged-in College Admin's college.
    @PutMapping("/{id}")
    public ResponseEntity<Teacher> updateTeacher(
            @PathVariable Long id,
            @RequestBody Teacher teacher,
            Authentication authentication
    ) {

        UUID adminUserId =
                UUID.fromString(authentication.getName());

        return teacherService
                .updateTeacher(
                        id,
                        teacher,
                        adminUserId
                )
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    // Delete teacher only if the teacher belongs
    // to the logged-in College Admin's college.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeacher(
            @PathVariable Long id,
            Authentication authentication
    ) {

        UUID adminUserId =
                UUID.fromString(authentication.getName());

        boolean deleted =
                teacherService.deleteTeacher(
                        id,
                        adminUserId
                );

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
