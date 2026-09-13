package com.attendance.system.service;

import com.attendance.system.entity.ClassSession;
import com.attendance.system.entity.ClassSessionStatus;
import com.attendance.system.repository.ClassSessionRepository;
import org.springframework.stereotype.Service;
import com.attendance.system.entity.Teacher;
import com.attendance.system.repository.TeacherRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import com.attendance.system.repository.TeacherAssignmentRepository;
import com.attendance.system.entity.AssignmentStatus;
import com.attendance.system.entity.TeacherAssignment;

import com.attendance.system.entity.ExtraClassRequest;
import com.attendance.system.entity.RequestStatus;
import com.attendance.system.repository.ExtraClassRequestRepository;

import com.attendance.system.entity.Department;
import com.attendance.system.entity.User;
import com.attendance.system.repository.DepartmentRepository;
import org.springframework.security.access.AccessDeniedException;

import com.attendance.system.entity.TimetableSlot;
import com.attendance.system.repository.TimetableSlotRepository;
@Service
public class ClassSessionService {

    private final ClassSessionRepository classSessionRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final TeacherRepository teacherRepository;
    private final ExtraClassRequestRepository extraClassRequestRepository;
    private final TimetableSlotRepository timetableSlotRepository;
    private final DepartmentRepository departmentRepository;
    private final UserService userService;

    public ClassSessionService(
            ClassSessionRepository classSessionRepository,
            TeacherAssignmentRepository teacherAssignmentRepository,
            TeacherRepository teacherRepository,
            ExtraClassRequestRepository extraClassRequestRepository,
            TimetableSlotRepository timetableSlotRepository,
            DepartmentRepository departmentRepository,
            UserService userService
    ) {
        this.classSessionRepository = classSessionRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.teacherRepository = teacherRepository;
        this.extraClassRequestRepository = extraClassRequestRepository;
        this.timetableSlotRepository = timetableSlotRepository;
        this.departmentRepository = departmentRepository;
        this.userService = userService;
    }

    // College-scoped: Create class session from approved extra class request
    public ClassSession createSessionFromApprovedRequestForCollege(
            Long extraClassRequestId,
            UUID adminUserId
    ) {
        Long collegeId = getAdminCollegeId(adminUserId);

        ExtraClassRequest request = extraClassRequestRepository
                .findById(extraClassRequestId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Extra class request not found"
                        ));

        if (!isAssignmentInCollege(
                request.getTeacherAssignmentId(),
                collegeId
        )) {
            throw new AccessDeniedException(
                    "Extra class request does not belong to your college"
            );
        }

        return createSessionFromApprovedRequest(extraClassRequestId);
    }

    public ClassSession createSessionFromApprovedRequest(
            Long extraClassRequestId
    ) {
        ExtraClassRequest request = extraClassRequestRepository
                .findById(extraClassRequestId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Extra class request not found"
                        ));

        if (request.getStatus() != RequestStatus.approved) {
            throw new IllegalStateException(
                    "Only approved extra class requests can create a class session"
            );
        }

        if (classSessionRepository.existsByExtraClassRequestId(
                extraClassRequestId
        )) {
            throw new IllegalStateException(
                    "Class session already exists for this extra class request"
            );
        }

        TeacherAssignment assignment = teacherAssignmentRepository
                .findById(request.getTeacherAssignmentId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Teacher assignment not found"
                        ));

        if (assignment.getStatus() != AssignmentStatus.active) {
            throw new IllegalStateException(
                    "Teacher assignment is not active"
            );
        }

        if (request.getRequestedStartTime() == null
                || request.getRequestedEndTime() == null) {
            throw new IllegalArgumentException(
                    "Requested start and end time are required"
            );
        }

        ClassSession session = new ClassSession();

        session.setTeacherAssignmentId(
                request.getTeacherAssignmentId()
        );

        session.setTimetableSlotId(null);

        session.setExtraClassRequestId(
                request.getId()
        );

        session.setSessionDate(
                request.getRequestedDate()
        );

        session.setStartTime(
                request.getRequestedStartTime()
        );

        session.setEndTime(
                request.getRequestedEndTime()
        );

        session.setStatus(
                ClassSessionStatus.scheduled
        );

        session.setCreatedBy(
                request.getRequestedBy()
        );

        return classSessionRepository.save(session);
    }

    // Get all class sessions
    public List<ClassSession> getAllSessions() {
        return classSessionRepository.findAll();
    }

    // Get class session by ID
    public Optional<ClassSession> getSessionById(Long id) {
        return classSessionRepository.findById(id);
    }

    // Get sessions by teacher assignment
    public List<ClassSession> getSessionsByTeacherAssignment(
            Long teacherAssignmentId
    ) {
        return classSessionRepository
                .findByTeacherAssignmentId(teacherAssignmentId);
    }

    // Get sessions by date
    public List<ClassSession> getSessionsByDate(
            java.time.LocalDate date
    ) {
        return classSessionRepository.findBySessionDate(date);
    }

    // Get sessions by status
    public List<ClassSession> getSessionsByStatus(
            ClassSessionStatus status
    ) {
        return classSessionRepository.findByStatus(status);
    }

    // College-scoped: Get all class sessions
    public List<ClassSession> getAllSessionsForCollege(
            java.util.UUID adminUserId
    ) {
        Long collegeId = getAdminCollegeId(adminUserId);

        return classSessionRepository.findAll()
                .stream()
                .filter(session ->
                        isClassSessionInCollege(
                                session,
                                collegeId
                        ))
                .toList();
    }

    // College-scoped: Get class session by ID
    public Optional<ClassSession> getSessionByIdForCollege(
            Long id,
            java.util.UUID adminUserId
    ) {
        Long collegeId = getAdminCollegeId(adminUserId);

        return classSessionRepository.findById(id)
                .filter(session ->
                        isClassSessionInCollege(
                                session,
                                collegeId
                        ));
    }

    // College-scoped: Get sessions by teacher assignment
    public List<ClassSession> getSessionsByTeacherAssignmentForCollege(
            Long teacherAssignmentId,
            java.util.UUID adminUserId
    ) {
        Long collegeId = getAdminCollegeId(adminUserId);

        return classSessionRepository
                .findByTeacherAssignmentId(teacherAssignmentId)
                .stream()
                .filter(session ->
                        isClassSessionInCollege(
                                session,
                                collegeId
                        ))
                .toList();
    }

    // College-scoped: Get sessions by date
    public List<ClassSession> getSessionsByDateForCollege(
            java.time.LocalDate date,
            java.util.UUID adminUserId
    ) {
        Long collegeId = getAdminCollegeId(adminUserId);

        return classSessionRepository
                .findBySessionDate(date)
                .stream()
                .filter(session ->
                        isClassSessionInCollege(
                                session,
                                collegeId
                        ))
                .toList();
    }

    // College-scoped: Get sessions by status
    public List<ClassSession> getSessionsByStatusForCollege(
            ClassSessionStatus status,
            java.util.UUID adminUserId
    ) {
        Long collegeId = getAdminCollegeId(adminUserId);

        return classSessionRepository
                .findByStatus(status)
                .stream()
                .filter(session ->
                        isClassSessionInCollege(
                                session,
                                collegeId
                        ))
                .toList();
    }

    // College-scoped: Create class session
    public ClassSession createSessionForCollege(
            ClassSession session,
            java.util.UUID adminUserId
    ) {
        Long collegeId = getAdminCollegeId(adminUserId);

        if (session.getTeacherAssignmentId() == null) {
            throw new IllegalArgumentException(
                    "teacherAssignmentId is required"
            );
        }

        if (!isAssignmentInCollege(
                session.getTeacherAssignmentId(),
                collegeId
        )) {
            throw new AccessDeniedException(
                    "Teacher assignment does not belong to your college"
            );
        }

        return createSession(
                session,
                adminUserId
        );
    }


    // Create class session
    public ClassSession createSession(
            ClassSession session,
            java.util.UUID createdBy
    ) {
        if (session.getStatus() == null) {
            session.setStatus(ClassSessionStatus.scheduled);
        }

        if (session.getStatus() != ClassSessionStatus.scheduled) {
            throw new IllegalArgumentException(
                    "New class session must have scheduled status"
            );
        }

        if (session.getTeacherAssignmentId() == null) {
            throw new IllegalArgumentException(
                    "teacherAssignmentId is required"
            );
        }

        TeacherAssignment assignment =
                teacherAssignmentRepository
                        .findById(session.getTeacherAssignmentId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Teacher assignment not found"
                                ));

        if (assignment.getStatus() != AssignmentStatus.active) {
            throw new IllegalStateException(
                    "Teacher assignment is not active"
            );
        }

        session.setCreatedBy(createdBy);

        if (session.getSessionDate() == null) {
            throw new IllegalArgumentException(
                    "sessionDate is required"
            );
        }

        if (session.getStartTime() == null
                || session.getEndTime() == null) {
            throw new IllegalArgumentException(
                    "startTime and endTime are required"
            );
        }

        if (!session.getEndTime().isAfter(
                session.getStartTime()
        )) {
            throw new IllegalArgumentException(
                    "endTime must be after startTime"
            );
        }

        boolean hasTimetableSlot =
                session.getTimetableSlotId() != null;

        boolean hasExtraClassRequest =
                session.getExtraClassRequestId() != null;

        if (hasTimetableSlot == hasExtraClassRequest) {
            throw new IllegalArgumentException(
                    "Exactly one of timetableSlotId or extraClassRequestId is required"
            );
        }

        if (hasTimetableSlot) {
            TimetableSlot timetableSlot =
                    timetableSlotRepository
                            .findById(session.getTimetableSlotId())
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Timetable slot not found"
                                    ));

            if (!timetableSlot.getTeacherAssignmentId()
                    .equals(session.getTeacherAssignmentId())) {

                throw new IllegalArgumentException(
                        "Timetable slot does not belong to the teacher assignment"
                );
            }

            if (timetableSlot.getStatus() != com.attendance.system.entity.RecordStatus.active) {
                throw new IllegalStateException(
                        "Timetable slot is not active"
                );
            }

            if (session.getSessionDate().isBefore(
                    timetableSlot.getEffectiveFrom()
            ) || session.getSessionDate().isAfter(
                    timetableSlot.getEffectiveTo()
            )) {

                throw new IllegalArgumentException(
                        "Session date is outside the timetable slot effective date range"
                );
            }

            short sessionDayOfWeek =
                    (short) session.getSessionDate()
                            .getDayOfWeek()
                            .getValue();

            if (timetableSlot.getDayOfWeek() != sessionDayOfWeek) {
                throw new IllegalArgumentException(
                        "Session date does not match the timetable slot day"
                );
            }

            if (!session.getStartTime().equals(
                    timetableSlot.getStartTime()
            ) || !session.getEndTime().equals(
                    timetableSlot.getEndTime()
            )) {

                throw new IllegalArgumentException(
                        "Session time does not match the timetable slot"
                );
            }
        }

        if (hasExtraClassRequest) {
            if (classSessionRepository.existsByExtraClassRequestId(
                    session.getExtraClassRequestId()
            )) {
                throw new IllegalStateException(
                        "Class session already exists for this extra class request"
                );
            }

            ExtraClassRequest request =
                    extraClassRequestRepository
                            .findById(session.getExtraClassRequestId())
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Extra class request not found"
                                    ));

            if (request.getStatus() != RequestStatus.approved) {
                throw new IllegalStateException(
                        "Only approved extra class requests can create a class session"
                );
            }

            if (!request.getTeacherAssignmentId()
                    .equals(session.getTeacherAssignmentId())) {

                throw new IllegalArgumentException(
                        "Extra class request does not belong to the teacher assignment"
                );
            }
        }

        return classSessionRepository.save(session);
    }

    // Start class session
    public ClassSession startSession(Long id, java.util.UUID startedBy) {

        ClassSession session = classSessionRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Class session not found"
                        )
                );

        if (session.getStatus() != ClassSessionStatus.scheduled) {
            throw new IllegalStateException(
                    "Only scheduled class sessions can be started"
            );
        }

        Teacher teacher = teacherRepository
                .findByUserId(startedBy)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Teacher profile not found"
                        ));

        TeacherAssignment assignment =
                teacherAssignmentRepository
                        .findById(session.getTeacherAssignmentId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Teacher assignment not found"
                                ));

        if (!assignment.getTeacherId().equals(teacher.getId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Teacher is not authorized for this class session"
            );
        }

        session.setStatus(ClassSessionStatus.in_progress);
        session.setStartedBy(startedBy);
        session.setStartedAt(OffsetDateTime.now());

        return classSessionRepository.save(session);
    }

    // Submit/conduct class session
    public ClassSession submitSession(Long id, java.util.UUID submittedBy) {

        ClassSession session = classSessionRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Class session not found"
                        )
                );

        if (session.getStatus() != ClassSessionStatus.in_progress) {
            throw new IllegalStateException(
                    "Only in-progress class sessions can be submitted"
            );
        }

        Teacher teacher = teacherRepository
                .findByUserId(submittedBy)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Teacher profile not found"
                        ));

        TeacherAssignment assignment =
                teacherAssignmentRepository
                        .findById(session.getTeacherAssignmentId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Teacher assignment not found"
                                ));

        if (!assignment.getTeacherId().equals(teacher.getId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Teacher is not authorized for this class session"
            );
        }

        session.setStatus(ClassSessionStatus.conducted);
        session.setSubmittedBy(submittedBy);
        session.setSubmittedAt(OffsetDateTime.now());

        return classSessionRepository.save(session);
    }

    // College-scoped: Update class session
    public Optional<ClassSession> updateSessionForCollege(
            Long id,
            ClassSession updatedSession,
            java.util.UUID adminUserId
    ) {
        Long collegeId = getAdminCollegeId(adminUserId);

        Optional<ClassSession> existingSession =
                classSessionRepository.findById(id);

        if (existingSession.isEmpty()) {
            return Optional.empty();
        }

        if (!isClassSessionInCollege(
                existingSession.get(),
                collegeId
        )) {
            return Optional.empty();
        }

        if (updatedSession.getTeacherAssignmentId() == null) {
            throw new IllegalArgumentException(
                    "teacherAssignmentId is required"
            );
        }

        if (!isAssignmentInCollege(
                updatedSession.getTeacherAssignmentId(),
                collegeId
        )) {
            throw new AccessDeniedException(
                    "Teacher assignment does not belong to your college"
            );
        }

        return updateSession(
                id,
                updatedSession
        );
    }


    // Update class session
    public Optional<ClassSession> updateSession(
            Long id,
            ClassSession updatedSession
    ) {
        if (updatedSession.getTeacherAssignmentId() == null) {
            throw new IllegalArgumentException(
                    "teacherAssignmentId is required"
            );
        }

        if (updatedSession.getSessionDate() == null) {
            throw new IllegalArgumentException(
                    "sessionDate is required"
            );
        }

        if (updatedSession.getStartTime() == null
                || updatedSession.getEndTime() == null) {
            throw new IllegalArgumentException(
                    "startTime and endTime are required"
            );
        }

        if (!updatedSession.getEndTime().isAfter(
                updatedSession.getStartTime()
        )) {
            throw new IllegalArgumentException(
                    "endTime must be after startTime"
            );
        }

        boolean hasTimetableSlot =
                updatedSession.getTimetableSlotId() != null;

        boolean hasExtraClassRequest =
                updatedSession.getExtraClassRequestId() != null;

        if (hasTimetableSlot == hasExtraClassRequest) {
            throw new IllegalArgumentException(
                    "Exactly one of timetableSlotId or extraClassRequestId is required"
            );
        }

        if (hasTimetableSlot) {
            TimetableSlot timetableSlot =
                    timetableSlotRepository
                            .findById(updatedSession.getTimetableSlotId())
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Timetable slot not found"
                                    ));

            if (!timetableSlot.getTeacherAssignmentId()
                    .equals(updatedSession.getTeacherAssignmentId())) {

                throw new IllegalArgumentException(
                        "Timetable slot does not belong to the teacher assignment"
                );
            }

            if (timetableSlot.getStatus()
                    != com.attendance.system.entity.RecordStatus.active) {

                throw new IllegalStateException(
                        "Timetable slot is not active"
                );
            }

            if (updatedSession.getSessionDate().isBefore(
                    timetableSlot.getEffectiveFrom()
            ) || updatedSession.getSessionDate().isAfter(
                    timetableSlot.getEffectiveTo()
            )) {

                throw new IllegalArgumentException(
                        "Session date is outside the timetable slot effective date range"
                );
            }

            short sessionDayOfWeek =
                    (short) updatedSession.getSessionDate()
                            .getDayOfWeek()
                            .getValue();

            if (timetableSlot.getDayOfWeek()
                    != sessionDayOfWeek) {

                throw new IllegalArgumentException(
                        "Session date does not match the timetable slot day"
                );
            }

            if (!updatedSession.getStartTime().equals(
                    timetableSlot.getStartTime()
            ) || !updatedSession.getEndTime().equals(
                    timetableSlot.getEndTime()
            )) {

                throw new IllegalArgumentException(
                        "Session time does not match the timetable slot"
                );
            }
        }
        if (hasExtraClassRequest) {
            ExtraClassRequest request =
                    extraClassRequestRepository
                            .findById(updatedSession.getExtraClassRequestId())
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Extra class request not found"
                                    ));

            if (request.getStatus() != RequestStatus.approved) {
                throw new IllegalStateException(
                        "Extra class request is not approved"
                );
            }

            if (!request.getTeacherAssignmentId()
                    .equals(updatedSession.getTeacherAssignmentId())) {

                throw new IllegalArgumentException(
                        "Extra class request does not belong to the teacher assignment"
                );
            }

            if (!request.getRequestedDate()
                    .equals(updatedSession.getSessionDate())) {

                throw new IllegalArgumentException(
                        "Session date does not match the extra class request date"
                );
            }

            if (request.getRequestedStartTime() != null
                    && request.getRequestedEndTime() != null) {

                if (!updatedSession.getStartTime().equals(
                        request.getRequestedStartTime()
                ) || !updatedSession.getEndTime().equals(
                        request.getRequestedEndTime()
                )) {

                    throw new IllegalArgumentException(
                            "Session time does not match the extra class request time"
                    );
                }
            }
        }

        TeacherAssignment assignment =
                teacherAssignmentRepository
                        .findById(updatedSession.getTeacherAssignmentId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Teacher assignment not found"
                                ));

        if (assignment.getStatus() != AssignmentStatus.active) {
            throw new IllegalStateException(
                    "Teacher assignment is not active"
            );
        }

        return classSessionRepository.findById(id)
                .map(existingSession -> {

                    if (existingSession.getStatus()
                            != ClassSessionStatus.scheduled) {

                        throw new IllegalStateException(
                                "Only scheduled class sessions can be updated"
                        );
                    }

                    if (updatedSession.getStatus() != null
                            && updatedSession.getStatus()
                            != ClassSessionStatus.scheduled) {

                        throw new IllegalStateException(
                                "Session status cannot be changed during update"
                        );
                    }

                    existingSession.setTeacherAssignmentId(
                            updatedSession.getTeacherAssignmentId()
                    );

                    existingSession.setTimetableSlotId(
                            updatedSession.getTimetableSlotId()
                    );

                    existingSession.setExtraClassRequestId(
                            updatedSession.getExtraClassRequestId()
                    );

                    existingSession.setSessionDate(
                            updatedSession.getSessionDate()
                    );

                    existingSession.setStartTime(
                            updatedSession.getStartTime()
                    );

                    existingSession.setEndTime(
                            updatedSession.getEndTime()
                    );

                    existingSession.setStatus(
                            ClassSessionStatus.scheduled
                    );

                    return classSessionRepository.save(
                            existingSession
                    );
                });
    }

    // College-scoped: Delete class session
    public boolean deleteSessionForCollege(
            Long id,
            java.util.UUID adminUserId
    ) {
        Long collegeId = getAdminCollegeId(adminUserId);

        Optional<ClassSession> existingSession =
                classSessionRepository.findById(id);

        if (existingSession.isEmpty()) {
            return false;
        }

        if (!isClassSessionInCollege(
                existingSession.get(),
                collegeId
        )) {
            return false;
        }

        return deleteSession(id);
    }

    // Delete class session
    // Delete class session
    public boolean deleteSession(Long id) {

        ClassSession session = classSessionRepository.findById(id)
                .orElse(null);

        if (session == null) {
            return false;
        }

        if (session.getStatus() == ClassSessionStatus.in_progress
                || session.getStatus() == ClassSessionStatus.conducted) {
            throw new IllegalStateException(
                    "Cannot delete an in-progress or conducted class session"
            );
        }

        classSessionRepository.delete(session);
        return true;
    }

    private Long getAdminCollegeId(
            java.util.UUID adminUserId
    ) {
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

    private boolean isAssignmentInCollege(
            Long teacherAssignmentId,
            Long collegeId
    ) {
        return teacherAssignmentRepository
                .findById(teacherAssignmentId)
                .flatMap(assignment ->
                        teacherRepository.findById(
                                assignment.getTeacherId()
                        ))
                .flatMap(teacher ->
                        departmentRepository.findById(
                                teacher.getDepartmentId()
                        ))
                .map(department ->
                        collegeId.equals(
                                department.getCollegeId()
                        ))
                .orElse(false);
    }

    private boolean isClassSessionInCollege(
            ClassSession session,
            Long collegeId
    ) {
        return teacherAssignmentRepository
                .findById(session.getTeacherAssignmentId())
                .flatMap(assignment ->
                        teacherRepository.findById(
                                assignment.getTeacherId()
                        ))
                .flatMap(teacher ->
                        departmentRepository.findById(
                                teacher.getDepartmentId()
                        ))
                .map(department ->
                        collegeId.equals(
                                department.getCollegeId()
                        ))
                .orElse(false);
    }

}