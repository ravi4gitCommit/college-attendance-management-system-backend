package com.attendance.system.controller;

import com.attendance.system.entity.ExtraClassRequest;
import com.attendance.system.entity.RequestStatus;
import com.attendance.system.service.ExtraClassRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.attendance.system.entity.ClassSession;
import com.attendance.system.service.ClassSessionService;
import com.attendance.system.security.RoleService;

@RestController
@RequestMapping("/api/v1/extra-class-requests")
public class ExtraClassRequestController {

    private final ExtraClassRequestService extraClassRequestService;
    private final ClassSessionService classSessionService;
    private final RoleService roleService;

    public ExtraClassRequestController(
            ExtraClassRequestService extraClassRequestService,
            ClassSessionService classSessionService,
            RoleService roleService
    ) {
        this.extraClassRequestService = extraClassRequestService;
        this.classSessionService = classSessionService;
        this.roleService = roleService;
    }

    // Teacher: create extra class request
    @PostMapping
    @PreAuthorize("@roleService.isTeacher(authentication)")
    public ResponseEntity<ExtraClassRequest> createRequest(
            @RequestBody ExtraClassRequest request,
            Authentication authentication
    ) {
        UUID requestedBy = UUID.fromString(authentication.getName());

        try {
            ExtraClassRequest createdRequest =
                    extraClassRequestService.createRequest(
                            request,
                            requestedBy
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(createdRequest);

        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }
    }

    // Admin: view all requests from own college
    @GetMapping
    @PreAuthorize("@roleService.isCollegeAdmin(authentication)")
    public ResponseEntity<List<ExtraClassRequest>> getAllRequests(
            Authentication authentication
    ) {
        UUID adminUserId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                extraClassRequestService.getAllRequestsForCollege(
                        adminUserId
                )
        );
    }

    // Teacher: view own requests
    @GetMapping("/my")
    @PreAuthorize("@roleService.isTeacher(authentication)")
    public ResponseEntity<List<ExtraClassRequest>> getMyRequests(
            Authentication authentication
    ) {
        UUID requestedBy = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                extraClassRequestService.getRequestsByUser(requestedBy)
        );
    }

    // Admin: view requests by status from own college
    @GetMapping("/status/{status}")
    @PreAuthorize("@roleService.isCollegeAdmin(authentication)")
    public ResponseEntity<List<ExtraClassRequest>> getByStatus(
            @PathVariable RequestStatus status,
            Authentication authentication
    ) {
        UUID adminUserId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                extraClassRequestService.getRequestsByStatusForCollege(
                        status,
                        adminUserId
                )
        );
    }

    // Get single request
    @GetMapping("/{id}")
    @PreAuthorize(
            "@roleService.isCollegeAdmin(authentication) or " +
                    "@roleService.isTeacher(authentication)"
    )
    public ResponseEntity<ExtraClassRequest> getRequestById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());

        // College Admin can view requests from their own college
        if (roleService.isCollegeAdmin(authentication)) {
            return extraClassRequestService
                    .getRequestByIdForCollege(id, userId)
                    .map(ResponseEntity::ok)
                    .orElseGet(
                            () -> ResponseEntity.notFound().build()
                    );
        }

        // Teacher can view only their own request
        if (roleService.isTeacher(authentication)) {
            return extraClassRequestService
                    .getRequestByIdForTeacher(id, userId)
                    .map(ResponseEntity::ok)
                    .orElseGet(
                            () -> ResponseEntity.status(
                                    HttpStatus.FORBIDDEN
                            ).build()
                    );
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // Admin: approve request
    @PostMapping("/{id}/approve")
    @PreAuthorize("@roleService.isCollegeAdmin(authentication)")
    public ResponseEntity<ExtraClassRequest> approveRequest(
            @PathVariable Long id,
            @RequestParam(required = false) String reviewComment,
            @RequestBody(required = false) Map<String, String> requestBody,
            Authentication authentication
    ) {
        UUID reviewedBy = UUID.fromString(authentication.getName());

        String finalReviewComment = reviewComment;

        if (finalReviewComment == null && requestBody != null) {
            finalReviewComment = requestBody.get("reviewComment");
        }

        try {
            ExtraClassRequest approvedRequest =
                    extraClassRequestService.approveRequestForCollege(
                            id,
                            reviewedBy,
                            finalReviewComment
                    );

            return ResponseEntity.ok(approvedRequest);

        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .build();
        }
    }

    // Admin: reject request
    @PostMapping("/{id}/reject")
    @PreAuthorize("@roleService.isCollegeAdmin(authentication)")
    public ResponseEntity<ExtraClassRequest> rejectRequest(
            @PathVariable Long id,
            @RequestParam(required = false) String reviewComment,
            @RequestBody(required = false) Map<String, String> requestBody,
            Authentication authentication
    ) {
        UUID reviewedBy = UUID.fromString(authentication.getName());

        String finalReviewComment = reviewComment;

        if (finalReviewComment == null && requestBody != null) {
            finalReviewComment = requestBody.get("reviewComment");
        }

        try {
            ExtraClassRequest rejectedRequest =
                    extraClassRequestService.rejectRequestForCollege(
                            id,
                            reviewedBy,
                            finalReviewComment
                    );

            return ResponseEntity.ok(rejectedRequest);

        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .build();
        }
    }

    @PostMapping("/{id}/create-session")
    @PreAuthorize("@roleService.isCollegeAdmin(authentication)")
    public ResponseEntity<ClassSession> createSessionFromRequest(
            @PathVariable Long id,
            Authentication authentication
    ) {
        try {
            ClassSession session =
                    classSessionService.createSessionFromApprovedRequestForCollege(
                            id,
                            UUID.fromString(authentication.getName())
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(session);

        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .build();
        }
    }
}