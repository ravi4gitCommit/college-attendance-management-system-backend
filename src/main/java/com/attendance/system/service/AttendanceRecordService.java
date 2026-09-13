package com.attendance.system.service;

import com.attendance.system.dto.request.AttendanceSubmitRequest;
import com.attendance.system.dto.response.AttendanceSummaryResponse;
import com.attendance.system.dto.response.SubjectAttendanceSummaryResponse;
import com.attendance.system.dto.response.TeacherClassStudentResponse;
import com.attendance.system.entity.AttendanceRecord;
import com.attendance.system.entity.AttendanceStatus;
import com.attendance.system.entity.ClassSession;
import com.attendance.system.entity.ClassSessionStatus;
import com.attendance.system.entity.Department;
import com.attendance.system.entity.Student;
import com.attendance.system.entity.StudentEnrollment;
import com.attendance.system.entity.Subject;
import com.attendance.system.entity.SubjectOffering;
import com.attendance.system.entity.Teacher;
import com.attendance.system.entity.TeacherAssignment;
import com.attendance.system.entity.User;
import com.attendance.system.repository.AttendanceRecordRepository;
import com.attendance.system.repository.ClassSessionRepository;
import com.attendance.system.repository.DepartmentRepository;
import com.attendance.system.repository.StudentEnrollmentRepository;
import com.attendance.system.repository.StudentRepository;
import com.attendance.system.repository.SubjectOfferingRepository;
import com.attendance.system.repository.SubjectRepository;
import com.attendance.system.repository.TeacherAssignmentRepository;
import com.attendance.system.repository.TeacherRepository;
import com.attendance.system.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AttendanceRecordService {


    private final AttendanceRecordRepository attendanceRecordRepository;
    private final TeacherService teacherService;
    private final ClassSessionRepository classSessionRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final SubjectOfferingRepository subjectOfferingRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final DepartmentRepository departmentRepository;
    private final UserService userService;

    public AttendanceRecordService(
            AttendanceRecordRepository attendanceRecordRepository,
            TeacherService teacherService,
            ClassSessionRepository classSessionRepository,
            TeacherAssignmentRepository teacherAssignmentRepository,
            SubjectOfferingRepository subjectOfferingRepository,
            StudentEnrollmentRepository studentEnrollmentRepository,
            StudentRepository studentRepository,
            SubjectRepository subjectRepository,
            UserRepository userRepository,
            TeacherRepository teacherRepository,
            DepartmentRepository departmentRepository,
            UserService userService) {

        this.attendanceRecordRepository = attendanceRecordRepository;
        this.teacherService = teacherService;
        this.classSessionRepository = classSessionRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.subjectOfferingRepository = subjectOfferingRepository;
        this.studentEnrollmentRepository = studentEnrollmentRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.teacherRepository = teacherRepository;
        this.departmentRepository = departmentRepository;
        this.userService = userService;
    }

    public List<AttendanceRecord> getAllRecords(UUID userId) {

        Teacher teacher = teacherService
                .getTeacherByUserId(userId)
                .orElse(null);

        List<AttendanceRecord> records =
                attendanceRecordRepository.findAll();

        /*
         * College Admin can view only attendance records
         * belonging to their own college.
         */
        if (teacher == null) {

            Long collegeId = getAdminCollegeId(userId);

            return records.stream()
                    .filter(record -> {

                        ClassSession session =
                                classSessionRepository
                                        .findById(record.getClassSessionId())
                                        .orElse(null);

                        return session != null
                                && isClassSessionInCollege(
                                session,
                                collegeId
                        );
                    })
                    .toList();
        }

        // Teacher can view only records from their own class sessions.
        return records.stream()
                .filter(record -> {
                    ClassSession session =
                            classSessionRepository
                                    .findById(record.getClassSessionId())
                                    .orElse(null);

                    if (session == null) {
                        return false;
                    }

                    return teacherAssignmentRepository
                            .findById(session.getTeacherAssignmentId())
                            .map(assignment ->
                                    assignment.getTeacherId()
                                            .equals(teacher.getId()))
                            .orElse(false);
                })
                .toList();
    }

    public List<AttendanceRecord> getRecordsByUserId(UUID userId) {

        Student student = studentRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Student not found"
                        )
                );

        return attendanceRecordRepository
                .findByStudentId(student.getId());
    }

    public AttendanceSummaryResponse getSummaryByUserId(UUID userId) {

        List<AttendanceRecord> records =
                getRecordsByUserId(userId);

        long totalClasses = records.size();

        long present = records.stream()
                .filter(record ->
                        record.getStatus() == AttendanceStatus.present)
                .count();

        long absent = records.stream()
                .filter(record ->
                        record.getStatus() == AttendanceStatus.absent)
                .count();

        double attendancePercentage =
                totalClasses == 0
                        ? 0.0
                        : (present * 100.0) / totalClasses;

        return new AttendanceSummaryResponse(
                totalClasses,
                present,
                absent,
                attendancePercentage
        );
    }

    public List<SubjectAttendanceSummaryResponse> getSubjectWiseSummary(
            UUID userId) {

        List<AttendanceRecord> records =
                getRecordsByUserId(userId);

        Map<Long, SubjectAttendanceSummaryResponse> summaryMap =
                new LinkedHashMap<>();

        for (AttendanceRecord record : records) {

            ClassSession session = classSessionRepository
                    .findById(record.getClassSessionId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Class session not found"
                            )
                    );

            TeacherAssignment assignment = teacherAssignmentRepository
                    .findById(session.getTeacherAssignmentId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Teacher assignment not found"
                            )
                    );

            SubjectOffering offering = subjectOfferingRepository
                    .findById(assignment.getSubjectOfferingId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Subject offering not found"
                            )
                    );

            Subject subject = subjectRepository
                    .findById(offering.getSubjectId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Subject not found"
                            )
                    );

            SubjectAttendanceSummaryResponse current =
                    summaryMap.get(subject.getId());

            long totalClasses = current == null
                    ? 0
                    : current.getTotalClasses();

            long present = current == null
                    ? 0
                    : current.getPresent();

            long absent = current == null
                    ? 0
                    : current.getAbsent();

            totalClasses++;

            if (record.getStatus() == AttendanceStatus.present) {
                present++;
            } else if (record.getStatus() == AttendanceStatus.absent) {
                absent++;
            }

            double percentage =
                    totalClasses == 0
                            ? 0.0
                            : (present * 100.0) / totalClasses;

            summaryMap.put(
                    subject.getId(),
                    new SubjectAttendanceSummaryResponse(
                            subject.getName(),
                            subject.getCode(),
                            totalClasses,
                            present,
                            absent,
                            percentage
                    )
            );
        }

        return new ArrayList<>(summaryMap.values());
    }

    public Optional<AttendanceRecord> getRecordById(
            Long id,
            UUID userId) {

        Optional<AttendanceRecord> record =
                attendanceRecordRepository.findById(id);

        if (record.isEmpty()) {
            return Optional.empty();
        }

        Teacher teacher = teacherService
                .getTeacherByUserId(userId)
                .orElse(null);

        /*
         * College Admin can view only attendance records
         * belonging to their own college.
         */
        if (teacher == null) {

            Long collegeId = getAdminCollegeId(userId);

            ClassSession session =
                    classSessionRepository
                            .findById(record.get().getClassSessionId())
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Class session not found"
                                    )
                            );

            /*
             * Return empty instead of revealing that a record
             * exists in another college.
             */
            if (!isClassSessionInCollege(session, collegeId)) {
                return Optional.empty();
            }

            return record;
        }

        ClassSession session =
                classSessionRepository
                        .findById(record.get().getClassSessionId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Class session not found"
                                )
                        );

        boolean ownsSession =
                teacherAssignmentRepository
                        .findById(session.getTeacherAssignmentId())
                        .map(assignment ->
                                assignment.getTeacherId()
                                        .equals(teacher.getId()))
                        .orElse(false);

        if (!ownsSession) {
            throw new AccessDeniedException(
                    "Teacher is not authorized for this attendance record"
            );
        }

        return record;
    }

    public List<AttendanceRecord> getRecordsByClassSession(
            Long classSessionId,
            UUID userId) {

        ClassSession classSession =
                classSessionRepository.findById(classSessionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Class session not found"
                                )
                        );

        Teacher teacher = teacherService
                .getTeacherByUserId(userId)
                .orElse(null);

        /*
         * College Admin can access only sessions
         * belonging to their own college.
         */
        if (teacher == null) {

            Long collegeId = getAdminCollegeId(userId);

            if (!isClassSessionInCollege(
                    classSession,
                    collegeId)) {

                throw new AccessDeniedException(
                        "Class session does not belong to your college"
                );
            }
        }

        if (teacher != null
                && !teacherAssignmentRepository
                .findById(classSession.getTeacherAssignmentId())
                .map(assignment ->
                        assignment.getTeacherId()
                                .equals(teacher.getId()))
                .orElse(false)) {

            throw new AccessDeniedException(
                    "Teacher is not authorized for this class session"
            );
        }

        return attendanceRecordRepository
                .findByClassSessionId(classSessionId);
    }

    public List<TeacherClassStudentResponse> getStudentsByClassSession(
            Long classSessionId,
            UUID userId) {

        ClassSession classSession =
                classSessionRepository.findById(classSessionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Class session not found"
                                )
                        );

        Teacher teacher = teacherService
                .getTeacherByUserId(userId)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Teacher profile not found"
                        )
                );

        TeacherAssignment assignment =
                teacherAssignmentRepository
                        .findById(classSession.getTeacherAssignmentId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Teacher assignment not found"
                                )
                        );

        if (!assignment.getTeacherId().equals(teacher.getId())) {
            throw new AccessDeniedException(
                    "Teacher is not authorized for this class session"
            );
        }

        SubjectOffering offering =
                subjectOfferingRepository
                        .findById(assignment.getSubjectOfferingId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Subject offering not found"
                                )
                        );

        List<StudentEnrollment> enrollments =
                studentEnrollmentRepository
                        .findByProgramIdAndAcademicSessionIdAndSemesterIdAndSectionIdAndStatus(
                                offering.getProgramId(),
                                offering.getAcademicSessionId(),
                                offering.getSemesterId(),
                                offering.getSectionId(),
                                com.attendance.system.entity.EnrollmentStatus.active
                        );

        List<TeacherClassStudentResponse> result =
                new ArrayList<>();

        for (StudentEnrollment enrollment : enrollments) {

            Student student =
                    studentRepository.findById(
                            enrollment.getStudentId()
                    ).orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Student not found: "
                                            + enrollment.getStudentId()
                            )
                    );

            String studentName =
                    userRepository.findById(student.getUserId())
                            .map(user -> user.getFullName())
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "User not found for student: "
                                                    + student.getId()
                                    )
                            );

            AttendanceStatus attendanceStatus =
                    attendanceRecordRepository
                            .findByClassSessionIdAndStudentId(
                                    classSessionId,
                                    student.getId()
                            )
                            .map(AttendanceRecord::getStatus)
                            .orElse(null);

            result.add(
                    new TeacherClassStudentResponse(
                            student.getId(),
                            studentName,
                            student.getRollNumber(),
                            attendanceStatus
                    )
            );
        }

        return result;
    }

    public List<AttendanceRecord> getRecordsByStudent(
            Long studentId,
            UUID userId) {

        Teacher teacher = teacherService
                .getTeacherByUserId(userId)
                .orElse(null);

        List<AttendanceRecord> records =
                attendanceRecordRepository.findByStudentId(studentId);

        /*
         * College Admin can view only attendance records
         * belonging to their own college.
         */
        if (teacher == null) {

            Long collegeId = getAdminCollegeId(userId);

            return records.stream()
                    .filter(record -> {

                        ClassSession session =
                                classSessionRepository
                                        .findById(record.getClassSessionId())
                                        .orElse(null);

                        return session != null
                                && isClassSessionInCollege(
                                session,
                                collegeId
                        );
                    })
                    .toList();
        }

        // Teacher can view only records from their own class sessions.
        return records.stream()
                .filter(record -> {
                    ClassSession session =
                            classSessionRepository
                                    .findById(record.getClassSessionId())
                                    .orElse(null);

                    if (session == null) {
                        return false;
                    }

                    return teacherAssignmentRepository
                            .findById(session.getTeacherAssignmentId())
                            .map(assignment ->
                                    assignment.getTeacherId()
                                            .equals(teacher.getId()))
                            .orElse(false);
                })
                .toList();
    }

    public Optional<AttendanceRecord> getRecord(
            Long classSessionId,
            Long studentId) {

        return attendanceRecordRepository
                .findByClassSessionIdAndStudentId(
                        classSessionId,
                        studentId
                );
    }

    @Transactional
    public List<AttendanceRecord> submitAttendance(
            Long classSessionId,
            AttendanceSubmitRequest request,
            UUID submittedBy) {

        /*
         * 1. Find class session
         */
        ClassSession classSession = classSessionRepository
                .findById(classSessionId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Class session not found"
                        )
                );

        /*
         * 2. Attendance can only be submitted
         *    while the class session is in progress.
         */
        if (classSession.getStatus() != ClassSessionStatus.in_progress) {
            throw new IllegalStateException(
                    "Attendance can only be submitted while a class session is in progress"
            );
        }

        /*
         * 3. Find teacher using authenticated user ID.
         */
        Teacher teacher = teacherService
                .getTeacherByUserId(submittedBy)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Teacher profile not found"
                        )
                );

        /*
         * 4. Find teacher assignment.
         */
        TeacherAssignment assignment =
                teacherAssignmentRepository
                        .findById(classSession.getTeacherAssignmentId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Teacher assignment not found"
                                )
                        );

        /*
         * 5. Verify that the submitting teacher
         *    owns this class session.
         */
        if (!assignment.getTeacherId().equals(teacher.getId())) {
            throw new AccessDeniedException(
                    "Teacher is not authorized for this class session"
            );
        }

        /*
         * 6. Find subject offering.
         */
        SubjectOffering offering =
                subjectOfferingRepository
                        .findById(assignment.getSubjectOfferingId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Subject offering not found"
                                )
                        );

        /*
         * 7. Get all active students belonging
         *    to this exact academic context.
         */
        List<StudentEnrollment> enrollments =
                studentEnrollmentRepository
                        .findByProgramIdAndAcademicSessionIdAndSemesterIdAndSectionIdAndStatus(
                                offering.getProgramId(),
                                offering.getAcademicSessionId(),
                                offering.getSemesterId(),
                                offering.getSectionId(),
                                com.attendance.system.entity.EnrollmentStatus.active
                        );

        /*
         * 8. Request must contain attendance data.
         */
        if (request == null
                || request.getAttendance() == null
                || request.getAttendance().isEmpty()) {

            throw new IllegalArgumentException(
                    "Attendance data is required"
            );
        }

        /*
         * 9. Number of submitted students must exactly
         *    match the number of active enrolled students.
         */
        if (request.getAttendance().size() != enrollments.size()) {
            throw new IllegalArgumentException(
                    "Attendance must be submitted for all active students"
            );
        }

        /*
         * 10. Build the list of valid enrolled student IDs.
         */
        java.util.Set<Long> enrolledStudentIds =
                enrollments.stream()
                        .map(StudentEnrollment::getStudentId)
                        .collect(java.util.stream.Collectors.toSet());

        /*
         * 11. Prepare result list.
         */
        List<AttendanceRecord> savedRecords =
                new ArrayList<>();

        /*
         * 12. Process every submitted attendance item.
         */
        for (AttendanceSubmitRequest.AttendanceItem item
                : request.getAttendance()) {

            if (item.getStudentId() == null) {
                throw new IllegalArgumentException(
                        "studentId is required"
                );
            }

            if (item.getStatus() == null) {
                throw new IllegalArgumentException(
                        "Attendance status is required"
                );
            }

            /*
             * Student must belong to this class context.
             */
            if (!enrolledStudentIds.contains(item.getStudentId())) {
                throw new AccessDeniedException(
                        "Student is not enrolled in this class session"
                );
            }

            /*
             * Prevent duplicate student IDs in the same request.
             */
            long occurrences = request.getAttendance()
                    .stream()
                    .filter(attendance ->
                            item.getStudentId()
                                    .equals(attendance.getStudentId()))
                    .count();

            if (occurrences != 1) {
                throw new IllegalArgumentException(
                        "Duplicate studentId in attendance request: "
                                + item.getStudentId()
                );
            }

            /*
             * Find existing attendance record.
             * If it does not exist, create a new one.
             */
            AttendanceRecord record =
                    attendanceRecordRepository
                            .findByClassSessionIdAndStudentId(
                                    classSessionId,
                                    item.getStudentId()
                            )
                            .orElseGet(() -> {

                                AttendanceRecord newRecord =
                                        new AttendanceRecord();

                                newRecord.setClassSessionId(
                                        classSessionId
                                );

                                newRecord.setStudentId(
                                        item.getStudentId()
                                );

                                return newRecord;
                            });

            /*
             * Apply final P/A selection.
             */
            record.setStatus(item.getStatus());
            record.setMarkedBy(submittedBy);

            savedRecords.add(
                    attendanceRecordRepository.save(record)
            );
        }

        /*
         * 13. Save final attendance first.
         *     Then conduct the class session.
         */
        attendanceRecordRepository.flush();

        classSession.setStatus(
                ClassSessionStatus.conducted
        );

        classSession.setSubmittedBy(submittedBy);
        classSession.setSubmittedAt(
                OffsetDateTime.now()
        );

        classSessionRepository.save(classSession);

        /*
         * 14. Return saved attendance records.
         */
        return savedRecords;
    }

    /*
     * Resolve the college of the authenticated College Admin.
     */
    private Long getAdminCollegeId(UUID adminUserId) {

        User admin = userService
                .findById(adminUserId)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Admin user not found"
                        )
                );

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

    /*
     * Check whether a class session belongs to the
     * requested college.
     *
     * Path:
     * ClassSession
     *      -> TeacherAssignment
     *      -> Teacher
     *      -> Department
     *      -> College
     */
    private boolean isClassSessionInCollege(
            ClassSession classSession,
            Long collegeId) {

        TeacherAssignment assignment =
                teacherAssignmentRepository
                        .findById(classSession.getTeacherAssignmentId())
                        .orElse(null);

        if (assignment == null) {
            return false;
        }

        Teacher teacher =
                teacherRepository
                        .findById(assignment.getTeacherId())
                        .orElse(null);

        if (teacher == null) {
            return false;
        }

        Department department =
                departmentRepository
                        .findById(teacher.getDepartmentId())
                        .orElse(null);

        return department != null
                && collegeId.equals(department.getCollegeId());
    }


}
