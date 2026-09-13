
package com.attendance.system.controller;

import com.attendance.system.entity.Department;
import com.attendance.system.entity.User;
import com.attendance.system.service.DepartmentService;
import com.attendance.system.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/college-admin/departments")
@PreAuthorize("@roleService.isCollegeAdmin(authentication)")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final UserService userService;

    public DepartmentController(
            DepartmentService departmentService,
            UserService userService
    ) {
        this.departmentService = departmentService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<Department>> getDepartments(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long collegeId = getCollegeId(jwt);

        return ResponseEntity.ok(
                departmentService.getDepartmentsByCollege(collegeId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Department> getDepartment(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long collegeId = getCollegeId(jwt);

        return departmentService
                .getDepartmentById(id, collegeId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Department> createDepartment(
            @RequestBody Department department,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long collegeId = getCollegeId(jwt);

        try {
            Department createdDepartment =
                    departmentService.createDepartment(
                            department,
                            collegeId
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(createdDepartment);

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    e.getMessage()
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Department> updateDepartment(
            @PathVariable Long id,
            @RequestBody Department department,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long collegeId = getCollegeId(jwt);

        try {
            return departmentService
                    .updateDepartment(id, department, collegeId)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    e.getMessage()
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long collegeId = getCollegeId(jwt);

        boolean deleted =
                departmentService.deleteDepartment(id, collegeId);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    private Long getCollegeId(Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());

        User user = userService.findById(userId)
                .orElseThrow(() ->
                        new IllegalStateException("User not found")
                );

        if (user.getCollegeId() == null) {
            throw new IllegalStateException(
                    "User is not assigned to a college"
            );
        }

        return user.getCollegeId();
    }
}