package com.attendance.system.service;

import com.attendance.system.dto.request.AttendanceCorrectionRequestDto;
import com.attendance.system.dto.request.AttendanceCorrectionReviewDto;
import com.attendance.system.entity.AttendanceCorrectionRequest;
import com.attendance.system.entity.AttendanceRecord;
import com.attendance.system.entity.AttendanceStatus;
import com.attendance.system.entity.ClassSession;
import com.attendance.system.entity.ClassSessionStatus;
import com.attendance.system.entity.RequestStatus;
import com.attendance.system.entity.Teacher;
import com.attendance.system.entity.TeacherAssignment;
import com.attendance.system.entity.Department;
import com.attendance.system.entity.User;
import com.attendance.system.repository.AttendanceCorrectionRequestRepository;
import com.attendance.system.repository.AttendanceRecordRepository;
import com.attendance.system.repository.ClassSessionRepository;
import com.attendance.system.repository.TeacherAssignmentRepository;
import com.attendance.system.repository.TeacherRepository;
import com.attendance.system.repository.DepartmentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AttendanceCorrectionRequestService {

    private final AttendanceCorrectionRequestRepository correctionRequestRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final ClassSessionRepository classSessionRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final TeacherRepository teacherRepository;
    private final DepartmentRepository departmentRepository;
    private final UserService userService;

    @PersistenceContext
    private EntityManager entityManager;

    public AttendanceCorrectionRequestService(
            AttendanceCorrectionRequestRepository correctionRequestRepository,
            AttendanceRecordRepository attendanceRecordRepository,
            ClassSessionRepository classSessionRepository,
            TeacherAssignmentRepository teacherAssignmentRepository,
            TeacherRepository teacherRepository,
            DepartmentRepository departmentRepository,
            UserService userService
    ) {
        this.correctionRequestRepository = correctionRequestRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.classSessionRepository = classSessionRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.teacherRepository = teacherRepository;
        this.departmentRepository = departmentRepository;
        this.userService = userService;
    }

    // Teacher: create an attendance correction request
    public AttendanceCorrectionRequest createRequest(
            AttendanceCorrectionRequestDto dto,
            UUID requestedBy
    ) {
        if (dto == null) {
            throw new IllegalArgumentException(
                    "Correction request is required"
            );
        }

        if (dto.getAttendanceRecordId() == null) {
            throw new IllegalArgumentException(
                    "attendanceRecordId is required"
            );
        }

        if (dto.getNewStatus() == null) {
            throw new IllegalArgumentException(
                    "newStatus is required"
            );
        }

        if (dto.getReason() == null || dto.getReason().isBlank()) {
            throw new IllegalArgumentException(
                    "reason is required"
            );
        }

        AttendanceRecord attendanceRecord =
                attendanceRecordRepository
                        .findById(dto.getAttendanceRecordId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Attendance record not found"
                                ));

        ClassSession classSession =
                classSessionRepository
                        .findById(attendanceRecord.getClassSessionId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Class session not found"
                                ));

        if (classSession.getStatus() != ClassSessionStatus.conducted) {
            throw new IllegalStateException(
                    "Attendance correction is allowed only after attendance is submitted"
            );
        }

        Teacher teacher = teacherRepository
                .findByUserId(requestedBy)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Teacher profile not found"
                        ));

        TeacherAssignment assignment =
                teacherAssignmentRepository
                        .findById(classSession.getTeacherAssignmentId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Teacher assignment not found"
                                ));

        if (!assignment.getTeacherId().equals(teacher.getId())) {
            throw new AccessDeniedException(
                    "Teacher is not assigned to this class session"
            );
        }

        AttendanceStatus currentStatus = attendanceRecord.getStatus();

        if (currentStatus == null) {
            throw new IllegalStateException(
                    "Attendance record has no current status"
            );
        }

        if (currentStatus == dto.getNewStatus()) {
            throw new IllegalArgumentException(
                    "New attendance status must be different from current status"
            );
        }

        Optional<AttendanceCorrectionRequest> pendingRequest =
                correctionRequestRepository
                        .findByAttendanceRecordIdAndStatus(
                                attendanceRecord.getId(),
                                RequestStatus.pending
                        );

        if (pendingRequest.isPresent()) {
            throw new IllegalStateException(
                    "A correction request is already pending for this attendance record"
            );
        }

        AttendanceCorrectionRequest request =
                new AttendanceCorrectionRequest();

        request.setAttendanceRecordId(attendanceRecord.getId());
        request.setOldStatus(currentStatus);
        request.setNewStatus(dto.getNewStatus());
        request.setReason(dto.getReason().trim());
        request.setStatus(RequestStatus.pending);
        request.setRequestedBy(requestedBy);
        request.setRequestedAt(OffsetDateTime.now());

        return correctionRequestRepository.save(request);
    }

    // Teacher: view own correction requests
    public List<AttendanceCorrectionRequest> getRequestsByUser(
            UUID requestedBy
    ) {
        return correctionRequestRepository.findByRequestedBy(requestedBy);
    }

    // Teacher: view own correction request by ID
    public Optional<AttendanceCorrectionRequest> getRequestByIdForTeacher(
            Long id,
            UUID requestedBy
    ) {
        return correctionRequestRepository
                .findById(id)
                .filter(request ->
                        requestedBy.equals(request.getRequestedBy())
                );
    }

    // Admin: view all correction requests from own college
    public List<AttendanceCorrectionRequest> getAllRequestsForCollege(
            UUID adminUserId
    ) {
        Long collegeId = getAdminCollegeId(adminUserId);

        return correctionRequestRepository.findAll()
                .stream()
                .filter(request ->
                        isRequestInCollege(request, collegeId)
                )
                .toList();
    }

    // Admin: view correction requests by status from own college
    public List<AttendanceCorrectionRequest> getRequestsByStatusForCollege(
            RequestStatus status,
            UUID adminUserId
    ) {
        Long collegeId = getAdminCollegeId(adminUserId);

        return correctionRequestRepository
                .findByStatus(status)
                .stream()
                .filter(request ->
                        isRequestInCollege(request, collegeId)
                )
                .toList();
    }

    // Admin: view one correction request from own college
    public Optional<AttendanceCorrectionRequest> getRequestByIdForCollege(
            Long id,
            UUID adminUserId
    ) {
        Long collegeId = getAdminCollegeId(adminUserId);

        return correctionRequestRepository
                .findById(id)
                .filter(request ->
                        isRequestInCollege(request, collegeId)
                );
    }

    // Admin: approve correction request
    @Transactional
    public AttendanceCorrectionRequest approveRequestForCollege(
            Long id,
            UUID reviewedBy,
            AttendanceCorrectionReviewDto dto
    ) {
        Long collegeId = getAdminCollegeId(reviewedBy);

        AttendanceCorrectionRequest request =
                correctionRequestRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Attendance correction request not found"
                                ));

        if (!isRequestInCollege(request, collegeId)) {
            throw new AccessDeniedException(
                    "Attendance correction request does not belong to your college"
            );
        }

        if (request.getStatus() != RequestStatus.pending) {
            throw new IllegalStateException(
                    "Only pending correction requests can be approved"
            );
        }

        AttendanceRecord attendanceRecord =
                attendanceRecordRepository
                        .findById(request.getAttendanceRecordId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Attendance record not found"
                                ));

        if (attendanceRecord.getStatus() != request.getOldStatus()) {
            throw new IllegalStateException(
                    "Attendance status has changed since the correction request was created"
            );
        }

        ClassSession classSession =
                classSessionRepository
                        .findById(attendanceRecord.getClassSessionId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Class session not found"
                                ));

        if (classSession.getStatus() != ClassSessionStatus.conducted) {
            throw new IllegalStateException(
                    "Attendance correction is allowed only after attendance is submitted"
            );
        }

        /*
         * First approve the correction request while the attendance record
         * still has its old status.
         *
         * The database trigger validates that old_status matches the
         * current attendance status when the correction request is updated.
         */
        request.setStatus(RequestStatus.approved);
        request.setReviewedBy(reviewedBy);
        request.setReviewedAt(OffsetDateTime.now());

        if (dto != null
                && dto.getReviewComment() != null
                && !dto.getReviewComment().isBlank()) {
            request.setReviewComment(dto.getReviewComment().trim());
        } else {
            request.setReviewComment(null);
        }

        correctionRequestRepository.save(request);
        correctionRequestRepository.flush();

        // Enable the database correction bypass only for this transaction.
        entityManager.createNativeQuery(
                "SELECT set_config('app.allow_attendance_correction', 'on', true)"
        ).getSingleResult();

        // Now apply the approved correction to the locked attendance record.
        attendanceRecord.setStatus(request.getNewStatus());
        attendanceRecordRepository.save(attendanceRecord);
        attendanceRecordRepository.flush();

        return request;
    }

    // Admin: reject correction request
    @Transactional
    public AttendanceCorrectionRequest rejectRequestForCollege(
            Long id,
            UUID reviewedBy,
            AttendanceCorrectionReviewDto dto
    ) {
        Long collegeId = getAdminCollegeId(reviewedBy);

        AttendanceCorrectionRequest request =
                correctionRequestRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Attendance correction request not found"
                                ));

        if (!isRequestInCollege(request, collegeId)) {
            throw new AccessDeniedException(
                    "Attendance correction request does not belong to your college"
            );
        }

        if (request.getStatus() != RequestStatus.pending) {
            throw new IllegalStateException(
                    "Only pending correction requests can be rejected"
            );
        }

        String reviewComment =
                dto == null ? null : dto.getReviewComment();

        if (reviewComment == null || reviewComment.isBlank()) {
            throw new IllegalArgumentException(
                    "reviewComment is required when rejecting a correction request"
            );
        }

        request.setStatus(RequestStatus.rejected);
        request.setReviewedBy(reviewedBy);
        request.setReviewedAt(OffsetDateTime.now());
        request.setReviewComment(reviewComment.trim());

        return correctionRequestRepository.save(request);
    }

    private Long getAdminCollegeId(UUID adminUserId) {
        User admin = userService
                .findById(adminUserId)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Admin user not found"
                        ));

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

    private boolean isRequestInCollege(
            AttendanceCorrectionRequest request,
            Long collegeId
    ) {
        if (request.getAttendanceRecordId() == null) {
            return false;
        }

        return attendanceRecordRepository
                .findById(request.getAttendanceRecordId())
                .flatMap(attendanceRecord ->
                        classSessionRepository.findById(
                                attendanceRecord.getClassSessionId()
                        ))
                .flatMap(classSession ->
                        teacherAssignmentRepository.findById(
                                classSession.getTeacherAssignmentId()
                        ))
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
