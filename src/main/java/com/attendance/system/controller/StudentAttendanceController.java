package com.attendance.system.controller;

import com.attendance.system.entity.AttendanceRecord;
import com.attendance.system.dto.response.AttendanceSummaryResponse;
import com.attendance.system.dto.response.SubjectAttendanceSummaryResponse;
import com.attendance.system.service.AttendanceRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/attendance")
@PreAuthorize("@roleService.isStudent(authentication)")
public class StudentAttendanceController {

    private final AttendanceRecordService attendanceRecordService;

    public StudentAttendanceController(
            AttendanceRecordService attendanceRecordService) {
        this.attendanceRecordService = attendanceRecordService;
    }

    @GetMapping
    public ResponseEntity<List<AttendanceRecord>> getMyAttendance(
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                attendanceRecordService.getRecordsByUserId(userId)
        );
    }
    @GetMapping("/summary")
    public ResponseEntity<AttendanceSummaryResponse> getMyAttendanceSummary(
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                attendanceRecordService.getSummaryByUserId(userId)
        );
    }

    @GetMapping("/subject-wise")
    public ResponseEntity<List<SubjectAttendanceSummaryResponse>> getMySubjectWiseAttendance(
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                attendanceRecordService.getSubjectWiseSummary(userId)
        );
    }

    @GetMapping("/session/{classSessionId}")
    public ResponseEntity<List<AttendanceRecord>> getMyAttendanceBySession(
            @PathVariable Long classSessionId,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                attendanceRecordService.getRecordsByUserId(userId)
                        .stream()
                        .filter(record -> record.getClassSessionId().equals(classSessionId))
                        .toList()
        );
    }
}