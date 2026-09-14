package com.attendance.system.controller;

import com.attendance.system.entity.Subject;
import com.attendance.system.service.SubjectService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hod/subjects")
@PreAuthorize("@roleService.isHOD(authentication)")
public class HodSubjectController {

    private final SubjectService subjectService;

    public HodSubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    public ResponseEntity<List<Subject>> getAllSubjects(
            Authentication authentication
    ) {
        UUID hodUserId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                subjectService.getAllSubjectsForHod(hodUserId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subject> getSubjectById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        UUID hodUserId = UUID.fromString(authentication.getName());

        return subjectService
                .getSubjectByIdForHod(id, hodUserId)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    @PostMapping
    public ResponseEntity<Subject> createSubject(
            @RequestBody Subject subject,
            Authentication authentication
    ) {
        try {
            UUID hodUserId = UUID.fromString(authentication.getName());

            Subject createdSubject =
                    subjectService.createSubjectForHod(
                            subject,
                            hodUserId
                    );

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
            Authentication authentication
    ) {
        try {
            UUID hodUserId = UUID.fromString(authentication.getName());

            return subjectService
                    .updateSubjectForHod(
                            id,
                            subject,
                            hodUserId
                    )
                    .map(ResponseEntity::ok)
                    .orElseGet(() ->
                            ResponseEntity.notFound().build()
                    );

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
            Authentication authentication
    ) {
        try {
            UUID hodUserId = UUID.fromString(authentication.getName());

            boolean deleted =
                    subjectService.deleteSubjectForHod(
                            id,
                            hodUserId
                    );

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
}