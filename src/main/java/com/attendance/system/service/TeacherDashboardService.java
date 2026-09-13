package com.attendance.system.service;

import com.attendance.system.dto.response.TeacherDashboardResponse;
import com.attendance.system.dto.response.TeacherProfileResponse;
import com.attendance.system.entity.ClassSession;
import com.attendance.system.entity.Teacher;
import com.attendance.system.entity.TeacherAssignment;
import com.attendance.system.entity.TimetableSlot;
import com.attendance.system.repository.TeacherAssignmentRepository;
import com.attendance.system.repository.TeacherRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TeacherDashboardService {

    private final TeacherProfileService teacherProfileService;
    private final TeacherAssignmentService teacherAssignmentService;
    private final TimetableSlotService timetableSlotService;
    private final ClassSessionService classSessionService;
    private final TeacherRepository teacherRepository;

    public TeacherDashboardService(
            TeacherProfileService teacherProfileService,
            TeacherAssignmentService teacherAssignmentService,
            TimetableSlotService timetableSlotService,
            ClassSessionService classSessionService,
            TeacherRepository teacherRepository
    ) {
        this.teacherProfileService = teacherProfileService;
        this.teacherAssignmentService = teacherAssignmentService;
        this.timetableSlotService = timetableSlotService;
        this.classSessionService = classSessionService;
        this.teacherRepository = teacherRepository;
    }

    public TeacherDashboardResponse getMyDashboard(UUID userId) {

        // 1. Get teacher profile
        TeacherProfileResponse profile =
                teacherProfileService.getMyProfile(userId);

        // 2. Get teacher profile entity
        Teacher teacher = teacherRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Teacher profile not found"
                        ));

        // 3. Get teacher assignments
        List<TeacherAssignment> assignments =
                teacherAssignmentService.getMyAssignments(
                        teacher.getId()
                );

        // 4. Get timetable
        List<TimetableSlot> timetable =
                assignments.stream()
                        .flatMap(assignment ->
                                timetableSlotService
                                        .getSlotsByTeacherAssignment(
                                                assignment.getId()
                                        )
                                        .stream()
                        )
                        .toList();

        // 5. Get teacher classes
        List<ClassSession> classes = new ArrayList<>();

        assignments.forEach(assignment ->
                classes.addAll(
                        classSessionService
                                .getSessionsByTeacherAssignment(
                                        assignment.getId()
                                )
                )
        );

        // 6. Build dashboard response
        return new TeacherDashboardResponse(
                profile,
                assignments,
                timetable,
                classes
        );
    }
}