package com.attendance.system.service;

import com.attendance.system.entity.ExtraClassRequest;
import com.attendance.system.entity.RequestStatus;
import com.attendance.system.repository.ExtraClassRequestRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.attendance.system.entity.TeacherAssignment;
import com.attendance.system.repository.TeacherAssignmentRepository;
import com.attendance.system.entity.Teacher;
import com.attendance.system.repository.TeacherRepository;

import com.attendance.system.entity.Department;
import com.attendance.system.entity.User;
import com.attendance.system.repository.DepartmentRepository;
import org.springframework.security.access.AccessDeniedException;
@Service
public class ExtraClassRequestService {

    private final ExtraClassRequestRepository extraClassRequestRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final TeacherRepository teacherRepository;
    private final DepartmentRepository departmentRepository;
    private final UserService userService;

    public ExtraClassRequestService(
            ExtraClassRequestRepository extraClassRequestRepository,
            TeacherAssignmentRepository teacherAssignmentRepository,
            TeacherRepository teacherRepository,
            DepartmentRepository departmentRepository,
            UserService userService
    ) {
        this.extraClassRequestRepository = extraClassRequestRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.teacherRepository = teacherRepository;
        this.departmentRepository = departmentRepository;
        this.userService = userService;
    }

    public List<ExtraClassRequest> getAllRequests() {
        return extraClassRequestRepository.findAll();
    }

    public Optional<ExtraClassRequest> getRequestById(Long id) {
        return extraClassRequestRepository.findById(id);
    }

    public Optional<ExtraClassRequest> getRequestByIdForTeacher(
            Long id,
            UUID requestedBy
    ) {
        return extraClassRequestRepository
                .findById(id)
                .filter(request ->
                        request.getRequestedBy().equals(requestedBy)
                );
    }

    public List<ExtraClassRequest> getRequestsByUser(UUID requestedBy) {
        return extraClassRequestRepository.findByRequestedBy(requestedBy);
    }

    public List<ExtraClassRequest> getRequestsByStatus(
            RequestStatus status
    ) {
        return extraClassRequestRepository.findByStatus(status);
    }
    // College-scoped: Get all extra class requests
    public List<ExtraClassRequest> getAllRequestsForCollege(
            UUID adminUserId
    ) {
        Long collegeId = getAdminCollegeId(adminUserId);

        return extraClassRequestRepository.findAll()
                .stream()
                .filter(request ->
                        isRequestInCollege(request, collegeId)
                )
                .toList();
    }

    // College-scoped: Get extra class requests by status
    public List<ExtraClassRequest> getRequestsByStatusForCollege(
            RequestStatus status,
            UUID adminUserId
    ) {
        Long collegeId = getAdminCollegeId(adminUserId);

        return extraClassRequestRepository
                .findByStatus(status)
                .stream()
                .filter(request ->
                        isRequestInCollege(request, collegeId)
                )
                .toList();
    }

    // College-scoped: Get extra class request by ID
    public Optional<ExtraClassRequest> getRequestByIdForCollege(
            Long id,
            UUID adminUserId
    ) {
        Long collegeId = getAdminCollegeId(adminUserId);

        return extraClassRequestRepository
                .findById(id)
                .filter(request ->
                        isRequestInCollege(request, collegeId)
                );
    }

    // College-scoped: Approve extra class request
    public ExtraClassRequest approveRequestForCollege(
            Long id,
            UUID reviewedBy,
            String reviewComment
    ) {
        Long collegeId = getAdminCollegeId(reviewedBy);

        ExtraClassRequest request =
                extraClassRequestRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Extra class request not found"
                                ));

        if (!isRequestInCollege(request, collegeId)) {
            throw new AccessDeniedException(
                    "Extra class request does not belong to your college"
            );
        }

        return approveRequest(id, reviewedBy, reviewComment);
    }

    // College-scoped: Reject extra class request
    public ExtraClassRequest rejectRequestForCollege(
            Long id,
            UUID reviewedBy,
            String reviewComment
    ) {
        Long collegeId = getAdminCollegeId(reviewedBy);

        ExtraClassRequest request =
                extraClassRequestRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Extra class request not found"
                                ));

        if (!isRequestInCollege(request, collegeId)) {
            throw new AccessDeniedException(
                    "Extra class request does not belong to your college"
            );
        }

        return rejectRequest(id, reviewedBy, reviewComment);
    }

    public List<ExtraClassRequest> getRequestsByTeacherAssignment(
            Long teacherAssignmentId
    ) {
        return extraClassRequestRepository
                .findByTeacherAssignmentId(teacherAssignmentId);
    }

    public ExtraClassRequest createRequest(
            ExtraClassRequest request,
            UUID requestedBy
    ) {
        if (request.getTeacherAssignmentId() == null) {
            throw new IllegalArgumentException(
                    "teacherAssignmentId is required"
            );
        }

        Teacher teacher = teacherRepository
                .findByUserId(requestedBy)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Teacher profile not found"
                        ));

        TeacherAssignment assignment = teacherAssignmentRepository
                .findById(request.getTeacherAssignmentId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Teacher assignment not found"
                        ));

        if (!assignment.getTeacherId().equals(teacher.getId())) {
            throw new IllegalStateException(
                    "Teacher is not assigned to this teacher assignment"
            );
        }



        if (request.getRequestedDate() == null) {
            throw new IllegalArgumentException(
                    "requestedDate is required"
            );
        }

        if (request.getReason() == null
                || request.getReason().isBlank()) {
            throw new IllegalArgumentException(
                    "reason is required"
            );
        }

        if (request.getRequestedStartTime() != null
                && request.getRequestedEndTime() != null
                && !request.getRequestedEndTime().isAfter(
                request.getRequestedStartTime()
        )) {
            throw new IllegalArgumentException(
                    "requestedEndTime must be after requestedStartTime"
            );
        }

        request.setRequestedBy(requestedBy);

        if (request.getStatus() == null) {
            request.setStatus(RequestStatus.pending);
        }

        request.setRequestedAt(OffsetDateTime.now());

        return extraClassRequestRepository.save(request);
    }

    public ExtraClassRequest approveRequest(
            Long id,
            UUID reviewedBy,
            String reviewComment
    ) {
        ExtraClassRequest request =
                extraClassRequestRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Extra class request not found"
                                ));

        if (request.getStatus() != RequestStatus.pending) {
            throw new IllegalStateException(
                    "Only pending requests can be approved"
            );
        }

        request.setStatus(RequestStatus.approved);
        request.setReviewedBy(reviewedBy);
        request.setReviewedAt(OffsetDateTime.now());
        request.setReviewComment(reviewComment);

        return extraClassRequestRepository.save(request);
    }

    public ExtraClassRequest rejectRequest(
            Long id,
            UUID reviewedBy,
            String reviewComment
    ) {
        ExtraClassRequest request =
                extraClassRequestRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Extra class request not found"
                                ));

        if (request.getStatus() != RequestStatus.pending) {
            throw new IllegalStateException(
                    "Only pending requests can be rejected"
            );
        }

        request.setStatus(RequestStatus.rejected);
        request.setReviewedBy(reviewedBy);
        request.setReviewedAt(OffsetDateTime.now());
        request.setReviewComment(reviewComment);

        return extraClassRequestRepository.save(request);
    }

        // Get college ID of the authenticated college admin
        private Long getAdminCollegeId(UUID adminUserId) {
            User admin = userService
                    .findById(adminUserId)
                    .orElseThrow(() ->
                            new AccessDeniedException("Admin user not found"));

            if (!"college_admin".equals(admin.getRole())) {
                throw new AccessDeniedException(
                        "User is not a college admin"
                );
            }

            if (!"active".equals(admin.getStatus())) {
                throw new AccessDeniedException(
                        "Admin user is not active"
                );
            }

            if (admin.getCollegeId() == null) {
                throw new AccessDeniedException(
                        "Admin user is not assigned to a college"
                );
            }

            return admin.getCollegeId();
        }

// Check whether an extra class request belongs to the admin's college
        private boolean isRequestInCollege(
                ExtraClassRequest request,
                Long collegeId
) {
            if (request.getTeacherAssignmentId() == null) {
                return false;
            }

            return teacherAssignmentRepository
                    .findById(request.getTeacherAssignmentId())
                    .flatMap(assignment ->
                            teacherRepository.findById(
                                    assignment.getTeacherId()
                            ))
                    .flatMap(teacher ->
                            departmentRepository.findById(
                                    teacher.getDepartmentId()
                            ))
                    .map(department ->
                            collegeId.equals(department.getCollegeId())
                    )
                    .orElse(false);
        }

}