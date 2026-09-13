package com.attendance.system.service;

import com.attendance.system.dto.response.StudentClassResponse;
import com.attendance.system.entity.*;
import com.attendance.system.repository.*;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class StudentClassService {

    private final StudentRepository studentRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;
    private final ClassSessionRepository classSessionRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final SubjectOfferingRepository subjectOfferingRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final TimetableSlotRepository timetableSlotRepository;

    public StudentClassService(
            StudentRepository studentRepository,
            StudentEnrollmentRepository studentEnrollmentRepository,
            ClassSessionRepository classSessionRepository,
            TeacherAssignmentRepository teacherAssignmentRepository,
            SubjectOfferingRepository subjectOfferingRepository,
            SubjectRepository subjectRepository,
            TeacherRepository teacherRepository,
            UserRepository userRepository,
            AttendanceRecordRepository attendanceRecordRepository,
            TimetableSlotRepository timetableSlotRepository) {

        this.studentRepository = studentRepository;
        this.studentEnrollmentRepository = studentEnrollmentRepository;
        this.classSessionRepository = classSessionRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.subjectOfferingRepository = subjectOfferingRepository;
        this.subjectRepository = subjectRepository;
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.timetableSlotRepository = timetableSlotRepository;
    }

    public List<StudentClassResponse> getMyClasses(UUID userId) {

        Student student = studentRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Student not found"));

        StudentEnrollment enrollment = studentEnrollmentRepository
                .findByStudentIdAndStatus(
                        student.getId(),
                        EnrollmentStatus.active
                )
                .orElseThrow(() ->
                        new IllegalArgumentException("Active enrollment not found"));

        List<StudentClassResponse> response = new ArrayList<>();

        // Get all class sessions.
        // We will include only scheduled, in_progress and conducted sessions.
        List<ClassSession> sessions =
                classSessionRepository.findAll();

        for (ClassSession session : sessions) {

            // Cancelled classes should not appear for students.
            if (session.getStatus() == ClassSessionStatus.cancelled) {
                continue;
            }

            TeacherAssignment assignment =
                    teacherAssignmentRepository
                            .findById(session.getTeacherAssignmentId())
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Teacher assignment not found"
                                    ));

            if (assignment.getStatus() != AssignmentStatus.active) {
                continue;
            }

            SubjectOffering offering =
                    subjectOfferingRepository
                            .findById(assignment.getSubjectOfferingId())
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Subject offering not found"
                                    ));

            if (!offering.getProgramId().equals(enrollment.getProgramId())
                    || !offering.getAcademicSessionId().equals(
                    enrollment.getAcademicSessionId())
                    || !offering.getSemesterId().equals(
                    enrollment.getSemesterId())
                    || !offering.getSectionId().equals(
                    enrollment.getSectionId())) {
                continue;
            }

            Subject subject = subjectRepository
                    .findById(offering.getSubjectId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Subject not found"));

            Teacher teacher = teacherRepository
                    .findById(assignment.getTeacherId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Teacher not found"));

            User teacherUser = userRepository
                    .findById(teacher.getUserId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Teacher user not found"
                            ));

            AttendanceStatus attendanceStatus =
                    attendanceRecordRepository
                            .findByClassSessionIdAndStudentId(
                                    session.getId(),
                                    student.getId()
                            )
                            .map(AttendanceRecord::getStatus)
                            .orElse(null);

            String room = null;

            if (session.getTimetableSlotId() != null) {
                room = timetableSlotRepository
                        .findById(session.getTimetableSlotId())
                        .map(TimetableSlot::getRoom)
                        .orElse(null);
            }

            response.add(
                    new StudentClassResponse(
                            session.getId(),
                            subject.getName(),
                            subject.getCode(),
                            teacherUser.getFullName(),
                            session.getSessionDate(),
                            session.getStartTime(),
                            session.getEndTime(),
                            room,
                            session.getStatus().name(),
                            attendanceStatus
                    )
            );
        }

        response.sort(
                Comparator
                        .comparing(
                                StudentClassResponse::getSessionDate
                        )
                        .thenComparing(
                                StudentClassResponse::getStartTime
                        )
                        .reversed()
        );

        return response;
    }
}
