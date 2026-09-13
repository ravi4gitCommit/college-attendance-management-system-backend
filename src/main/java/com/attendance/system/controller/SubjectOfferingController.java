package com.attendance.system.controller;

import com.attendance.system.entity.SubjectOffering;
import com.attendance.system.entity.User;
import com.attendance.system.service.SubjectOfferingService;
import com.attendance.system.service.UserService;
import org.springframework.dao.DataIntegrityViolationException;
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
@RequestMapping("/api/v1/college-admin/subject-offerings")
@PreAuthorize("@roleService.isCollegeAdmin(authentication)")
public class SubjectOfferingController {

    private final SubjectOfferingService subjectOfferingService;
    private final UserService userService;

    public SubjectOfferingController(
            SubjectOfferingService subjectOfferingService,
            UserService userService
    ) {
        this.subjectOfferingService = subjectOfferingService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<SubjectOffering>> getAllOfferings(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long collegeId = getCollegeId(jwt);

        return ResponseEntity.ok(
                subjectOfferingService.getAllOfferings(collegeId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubjectOffering> getOfferingById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long collegeId = getCollegeId(jwt);

        return subjectOfferingService
                .getOfferingById(id, collegeId)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    @GetMapping("/context")
    public ResponseEntity<List<SubjectOffering>> getOfferingsByContext(
            @RequestParam Long programId,
            @RequestParam Long academicSessionId,
            @RequestParam Short semesterId,
            @RequestParam Long sectionId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long collegeId = getCollegeId(jwt);

        return ResponseEntity.ok(
                subjectOfferingService.getOfferingsByContext(
                        programId,
                        academicSessionId,
                        semesterId,
                        sectionId,
                        collegeId
                )
        );
    }

    @PostMapping
    public ResponseEntity<SubjectOffering> createOffering(
            @RequestBody SubjectOffering offering,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long collegeId = getCollegeId(jwt);

        try {
            SubjectOffering createdOffering =
                    subjectOfferingService.createOffering(
                            offering,
                            collegeId
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(createdOffering);

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );

        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Subject offering already exists or violates a database constraint"
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubjectOffering> updateOffering(
            @PathVariable Long id,
            @RequestBody SubjectOffering offering,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long collegeId = getCollegeId(jwt);

        try {
            return subjectOfferingService
                    .updateOffering(
                            id,
                            offering,
                            collegeId
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

        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Subject offering already exists or violates a database constraint"
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffering(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long collegeId = getCollegeId(jwt);

        try {
            boolean deleted =
                    subjectOfferingService.deleteOffering(
                            id,
                            collegeId
                    );

            if (!deleted) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.noContent().build();

        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Subject offering cannot be deleted because it is being used"
            );
        }
    }

    private Long getCollegeId(Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());

        User user = userService.findById(userId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "User not found"
                        )
                );

        if (user.getCollegeId() == null) {
            throw new IllegalStateException(
                    "User is not assigned to a college"
            );
        }

        return user.getCollegeId();
    }
}