package com.attendance.system.controller;

import com.attendance.system.entity.Student;
import com.attendance.system.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/college-admin/students")
@PreAuthorize("@roleService.isCollegeAdmin(authentication)")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // Get all students belonging to the logged-in College Admin's college
    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents(
            Authentication authentication
    ) {

        UUID adminUserId =
                UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                studentService.getAllStudents(adminUserId)
        );
    }

    // Get one student only if it belongs to the admin's college
    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(
            @PathVariable Long id,
            Authentication authentication
    ) {

        UUID adminUserId =
                UUID.fromString(authentication.getName());

        return studentService
                .getStudentById(id, adminUserId)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    // Get students by department
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<Student>> getStudentsByDepartment(
            @PathVariable Long departmentId,
            Authentication authentication
    ) {

        UUID adminUserId =
                UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                studentService.getStudentsByDepartment(
                        departmentId,
                        adminUserId
                )
        );
    }

    // Create student
    @PostMapping
    public ResponseEntity<Student> createStudent(
            @RequestBody Student student,
            Authentication authentication
    ) {

        UUID adminUserId =
                UUID.fromString(authentication.getName());

        try {
            Student createdStudent =
                    studentService.createStudent(
                            student,
                            adminUserId
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(createdStudent);

        } catch (ResponseStatusException e) {
            throw e;

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }
    }

    // Update student
    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(
            @PathVariable Long id,
            @RequestBody Student student,
            Authentication authentication
    ) {

        UUID adminUserId =
                UUID.fromString(authentication.getName());

        return studentService
                .updateStudent(
                        id,
                        student,
                        adminUserId
                )
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    // Delete student
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(
            @PathVariable Long id,
            Authentication authentication
    ) {

        UUID adminUserId =
                UUID.fromString(authentication.getName());

        boolean deleted =
                studentService.deleteStudent(
                        id,
                        adminUserId
                );

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
