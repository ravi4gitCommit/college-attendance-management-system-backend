package com.attendance.system.controller;

import com.attendance.system.entity.SubjectOffering;
import com.attendance.system.service.SubjectOfferingService;
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
@RequestMapping("/api/v1/hod/subject-offerings")
@PreAuthorize("@roleService.isHOD(authentication)")
public class HodSubjectOfferingController {

    private final SubjectOfferingService subjectOfferingService;

    public HodSubjectOfferingController(
            SubjectOfferingService subjectOfferingService
    ) {
        this.subjectOfferingService = subjectOfferingService;
    }

    @GetMapping
    public ResponseEntity<List<SubjectOffering>> getAllOfferings(
            Authentication authentication
    ) {
        UUID hodUserId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                subjectOfferingService.getAllOfferingsForHod(
                        hodUserId
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubjectOffering> getOfferingById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        UUID hodUserId = UUID.fromString(authentication.getName());

        return subjectOfferingService
                .getOfferingByIdForHod(id, hodUserId)
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
            Authentication authentication
    ) {
        UUID hodUserId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                subjectOfferingService.getOfferingsByContextForHod(
                        programId,
                        academicSessionId,
                        semesterId,
                        sectionId,
                        hodUserId
                )
        );
    }

    @PostMapping
    public ResponseEntity<SubjectOffering> createOffering(
            @RequestBody SubjectOffering offering,
            Authentication authentication
    ) {
        try {
            UUID hodUserId = UUID.fromString(authentication.getName());

            SubjectOffering createdOffering =
                    subjectOfferingService.createOfferingForHod(
                            offering,
                            hodUserId
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
            Authentication authentication
    ) {
        try {
            UUID hodUserId = UUID.fromString(authentication.getName());

            return subjectOfferingService
                    .updateOfferingForHod(
                            id,
                            offering,
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
            Authentication authentication
    ) {
        try {
            UUID hodUserId = UUID.fromString(authentication.getName());

            boolean deleted =
                    subjectOfferingService.deleteOfferingForHod(
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
                    "Subject offering cannot be deleted because it is being used"
            );
        }
    }
}