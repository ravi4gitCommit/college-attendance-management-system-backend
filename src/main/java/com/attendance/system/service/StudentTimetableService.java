package com.attendance.system.service;

import com.attendance.system.dto.response.StudentTimetableResponse;
import com.attendance.system.entity.*;
import com.attendance.system.repository.*;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class StudentTimetableService {

    private final StudentRepository studentRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;
    private final TimetableSlotRepository timetableSlotRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final SubjectOfferingRepository subjectOfferingRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;

    public StudentTimetableService(
            StudentRepository studentRepository,
            StudentEnrollmentRepository studentEnrollmentRepository,
            TimetableSlotRepository timetableSlotRepository,
            TeacherAssignmentRepository teacherAssignmentRepository,
            SubjectOfferingRepository subjectOfferingRepository,
            SubjectRepository subjectRepository,
            TeacherRepository teacherRepository,
            UserRepository userRepository) {

        this.studentRepository = studentRepository;
        this.studentEnrollmentRepository = studentEnrollmentRepository;
        this.timetableSlotRepository = timetableSlotRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.subjectOfferingRepository = subjectOfferingRepository;
        this.subjectRepository = subjectRepository;
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
    }

    public List<StudentTimetableResponse> getMyTimetable(UUID userId) {

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

        List<SubjectOffering> offerings =
                subjectOfferingRepository
                        .findByProgramIdAndAcademicSessionIdAndSemesterIdAndSectionId(
                                enrollment.getProgramId(),
                                enrollment.getAcademicSessionId(),
                                enrollment.getSemesterId(),
                                enrollment.getSectionId()
                        );

        List<StudentTimetableResponse> response = new ArrayList<>();

        LocalDate today = LocalDate.now();

        for (SubjectOffering offering : offerings) {

            if (offering.getStatus() != RecordStatus.active) {
                continue;
            }

            List<TeacherAssignment> assignments =
                    teacherAssignmentRepository
                            .findBySubjectOfferingId(offering.getId());

            Subject subject = subjectRepository
                    .findById(offering.getSubjectId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Subject not found"));

            for (TeacherAssignment assignment : assignments) {

                if (assignment.getStatus() != AssignmentStatus.active) {
                    continue;
                }

                List<TimetableSlot> slots =
                        timetableSlotRepository
                                .findByTeacherAssignmentId(
                                        assignment.getId()
                                );

                Teacher teacher = teacherRepository
                        .findById(assignment.getTeacherId())
                        .orElseThrow(() ->
                                new IllegalArgumentException("Teacher not found"));

                User user = userRepository
                        .findById(teacher.getUserId())
                        .orElseThrow(() ->
                                new IllegalArgumentException("Teacher user not found"));

                for (TimetableSlot slot : slots) {

                    if (slot.getStatus() != RecordStatus.active) {
                        continue;
                    }

                    if (today.isBefore(slot.getEffectiveFrom())
                            || today.isAfter(slot.getEffectiveTo())) {
                        continue;
                    }

                    response.add(
                            new StudentTimetableResponse(
                                    slot.getId(),
                                    subject.getName(),
                                    subject.getCode(),
                                    user.getFullName(),
                                    slot.getDayOfWeek(),
                                    slot.getStartTime(),
                                    slot.getEndTime(),
                                    slot.getRoom()
                            )
                    );
                }
            }
        }

        response.sort(
                (a, b) -> {
                    int dayCompare =
                            Short.compare(
                                    a.getDayOfWeek(),
                                    b.getDayOfWeek()
                            );

                    if (dayCompare != 0) {
                        return dayCompare;
                    }

                    return a.getStartTime()
                            .compareTo(b.getStartTime());
                }
        );

        return response;
    }

    public List<StudentTimetableResponse> getMyTodayTimetable(UUID userId) {

        LocalDate today = LocalDate.now();

        short dayOfWeek = (short) today.getDayOfWeek().getValue();

        List<TimetableSlot> todaySlots =
                timetableSlotRepository
                        .findByDayOfWeekAndStatusAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqual(
                                dayOfWeek,
                                RecordStatus.active,
                                today,
                                today
                        );

        List<StudentTimetableResponse> response = new ArrayList<>();

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

        for (TimetableSlot slot : todaySlots) {

            TeacherAssignment assignment =
                    teacherAssignmentRepository
                            .findById(slot.getTeacherAssignmentId())
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

            if (offering.getStatus() != RecordStatus.active) {
                continue;
            }

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

            User user = userRepository
                    .findById(teacher.getUserId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Teacher user not found"));

            response.add(
                    new StudentTimetableResponse(
                            slot.getId(),
                            subject.getName(),
                            subject.getCode(),
                            user.getFullName(),
                            slot.getDayOfWeek(),
                            slot.getStartTime(),
                            slot.getEndTime(),
                            slot.getRoom()
                    )
            );
        }

        response.sort(
                (a, b) -> a.getStartTime()
                        .compareTo(b.getStartTime())
        );

        return response;
    }
}