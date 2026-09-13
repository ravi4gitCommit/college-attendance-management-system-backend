package com.attendance.system.controller;

import com.attendance.system.entity.Teacher;
import com.attendance.system.entity.TimetableSlot;
import com.attendance.system.repository.TeacherAssignmentRepository;
import com.attendance.system.repository.TeacherRepository;
import com.attendance.system.service.TeacherTimetableService;
import com.attendance.system.service.TimetableSlotService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teacher/timetable")
@PreAuthorize("@roleService.isTeacher(authentication)")
public class TeacherTimetableController {

    private final TimetableSlotService timetableSlotService;
    private final TeacherRepository teacherRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final TeacherTimetableService teacherTimetableService;

    public TeacherTimetableController(
            TimetableSlotService timetableSlotService,
            TeacherRepository teacherRepository,
            TeacherAssignmentRepository teacherAssignmentRepository,
            TeacherTimetableService teacherTimetableService
    ) {
        this.timetableSlotService = timetableSlotService;
        this.teacherRepository = teacherRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.teacherTimetableService = teacherTimetableService;
    }

    @GetMapping
    public ResponseEntity<List<TimetableSlot>> getMyTimetable(
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());

        Teacher teacher = teacherRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Teacher profile not found"));

        List<TimetableSlot> slots = teacherAssignmentRepository
                .findByTeacherId(teacher.getId())
                .stream()
                .flatMap(assignment ->
                        timetableSlotService
                                .getSlotsByTeacherAssignment(assignment.getId())
                                .stream()
                )
                .toList();

        return ResponseEntity.ok(slots);
    }

    @GetMapping("/today")
    public ResponseEntity<List<TimetableSlot>> getMyTodayTimetable(
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                teacherTimetableService.getMyTodayTimetable(userId)
        );
    }
}