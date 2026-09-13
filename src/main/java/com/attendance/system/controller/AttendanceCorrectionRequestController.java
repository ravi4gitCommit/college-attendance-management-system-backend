package com.attendance.system.controller;

import com.attendance.system.dto.request.AttendanceCorrectionRequestDto;
import com.attendance.system.dto.request.AttendanceCorrectionReviewDto;
import com.attendance.system.entity.AttendanceCorrectionRequest;
import com.attendance.system.entity.RequestStatus;
import com.attendance.system.security.RoleService;
import com.attendance.system.service.AttendanceCorrectionRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attendance-corrections")
public class AttendanceCorrectionRequestController {

    private final AttendanceCorrectionRequestService correctionRequestService;
    private final RoleService roleService;

    public AttendanceCorrectionRequestController(
            AttendanceCorrectionRequestService correctionRequestService,
            RoleService roleService
    ) {
        this.correctionRequestService = correctionRequestService;
        this.roleService = roleService;
    }

    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize(
            "@roleService.isTeacher(authentication)"
    )
    public ResponseEntity<AttendanceCorrectionRequest> createRequest(
            @RequestBody AttendanceCorrectionRequestDto dto,
            Authentication authentication
    ) {
        UUID requestedBy =
                UUID.fromString(authentication.getName());

        try {
            AttendanceCorrectionRequest request =
                    correctionRequestService.createRequest(
                            dto,
                            requestedBy
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(request);

        } catch (AccessDeniedException e) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    e.getMessage()
            );

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );

        } catch (IllegalStateException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    e.getMessage()
            );
        }
    }

    @GetMapping("/my")
    @org.springframework.security.access.prepost.PreAuthorize(
            "@roleService.isTeacher(authentication)"
    )
    public ResponseEntity<List<AttendanceCorrectionRequest>> getMyRequests(
            Authentication authentication
    ) {
        UUID requestedBy =
                UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                correctionRequestService.getRequestsByUser(
                        requestedBy
                )
        );
    }

    @GetMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize(
            "@roleService.isCollegeAdmin(authentication) or " +
                    "@roleService.isTeacher(authentication)"
    )
    public ResponseEntity<AttendanceCorrectionRequest> getRequestById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        UUID userId =
                UUID.fromString(authentication.getName());

        if (roleService.isCollegeAdmin(authentication)) {
            return correctionRequestService
                    .getRequestByIdForCollege(id, userId)
                    .map(ResponseEntity::ok)
                    .orElseGet(
                            () -> ResponseEntity.notFound().build()
                    );
        }

        if (roleService.isTeacher(authentication)) {
            return correctionRequestService
                    .getRequestByIdForTeacher(id, userId)
                    .map(ResponseEntity::ok)
                    .orElseGet(
                            () -> ResponseEntity
                                    .status(HttpStatus.FORBIDDEN)
                                    .build()
                    );
        }

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .build();
    }

    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize(
            "@roleService.isCollegeAdmin(authentication)"
    )
    public ResponseEntity<List<AttendanceCorrectionRequest>> getAllRequests(
            Authentication authentication
    ) {
        UUID adminUserId =
                UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                correctionRequestService.getAllRequestsForCollege(
                        adminUserId
                )
        );
    }

    @GetMapping("/status/{status}")
    @org.springframework.security.access.prepost.PreAuthorize(
            "@roleService.isCollegeAdmin(authentication)"
    )
    public ResponseEntity<List<AttendanceCorrectionRequest>> getByStatus(
            @PathVariable RequestStatus status,
            Authentication authentication
    ) {
        UUID adminUserId =
                UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                correctionRequestService
                        .getRequestsByStatusForCollege(
                                status,
                                adminUserId
                        )
        );
    }

    @PostMapping("/{id}/approve")
    @org.springframework.security.access.prepost.PreAuthorize(
            "@roleService.isCollegeAdmin(authentication)"
    )
    public ResponseEntity<AttendanceCorrectionRequest> approveRequest(
            @PathVariable Long id,
            @RequestBody(required = false)
            AttendanceCorrectionReviewDto dto,
            Authentication authentication
    ) {
        UUID reviewedBy =
                UUID.fromString(authentication.getName());

        try {
            AttendanceCorrectionRequest request =
                    correctionRequestService
                            .approveRequestForCollege(
                                    id,
                                    reviewedBy,
                                    dto
                            );

            return ResponseEntity.ok(request);

        } catch (AccessDeniedException e) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    e.getMessage()
            );

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );

        } catch (IllegalStateException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    e.getMessage()
            );
        }
    }

    @PostMapping("/{id}/reject")
    @org.springframework.security.access.prepost.PreAuthorize(
            "@roleService.isCollegeAdmin(authentication)"
    )
    public ResponseEntity<AttendanceCorrectionRequest> rejectRequest(
            @PathVariable Long id,
            @RequestBody(required = false)
            AttendanceCorrectionReviewDto dto,
            Authentication authentication
    ) {
        UUID reviewedBy =
                UUID.fromString(authentication.getName());

        try {
            AttendanceCorrectionRequest request =
                    correctionRequestService
                            .rejectRequestForCollege(
                                    id,
                                    reviewedBy,
                                    dto
                            );

            return ResponseEntity.ok(request);

        } catch (AccessDeniedException e) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    e.getMessage()
            );

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );

        } catch (IllegalStateException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    e.getMessage()
            );
        }
    }
}
