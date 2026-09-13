package com.attendance.system.controller;

import com.attendance.system.entity.Subject;
import com.attendance.system.entity.User;
import com.attendance.system.service.SubjectService;
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
import org.springframework.dao.DataIntegrityViolationException;
@RestController
@RequestMapping("/api/v1/college-admin/subjects")
@PreAuthorize("@roleService.isCollegeAdmin(authentication)")
public class SubjectController {

    private final SubjectService subjectService;
    private final UserService userService;

    public SubjectController(
            SubjectService subjectService,
            UserService userService
    ) {
        this.subjectService = subjectService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<Subject>> getAllSubjects(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long collegeId = getCollegeId(jwt);

        return ResponseEntity.ok(
                subjectService.getAllSubjects(collegeId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subject> getSubjectById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long collegeId = getCollegeId(jwt);

        return subjectService.getSubjectById(id, collegeId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<Subject>> getSubjectsByDepartment(
            @PathVariable Long departmentId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long collegeId = getCollegeId(jwt);

        return ResponseEntity.ok(
                subjectService.getSubjectsByDepartment(
                        departmentId,
                        collegeId
                )
        );
    }

    @PostMapping
    public ResponseEntity<Subject> createSubject(
            @RequestBody Subject subject,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long collegeId = getCollegeId(jwt);

        try {
            Subject createdSubject =
                    subjectService.createSubject(subject, collegeId);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(createdSubject);

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Subject> updateSubject(
            @PathVariable Long id,
            @RequestBody Subject subject,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long collegeId = getCollegeId(jwt);

        try {
            return subjectService.updateSubject(
                            id,
                            subject,
                            collegeId
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


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubject(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long collegeId = getCollegeId(jwt);

        try {
            boolean deleted =
                    subjectService.deleteSubject(id, collegeId);

            if (!deleted) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.noContent().build();

        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Subject cannot be deleted because it is used by a subject offering"
            );
        }
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
