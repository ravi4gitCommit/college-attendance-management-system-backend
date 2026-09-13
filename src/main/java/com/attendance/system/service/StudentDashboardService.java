package com.attendance.system.service;

import com.attendance.system.dto.response.AttendanceSummaryResponse;
import com.attendance.system.dto.response.StudentClassResponse;
import com.attendance.system.dto.response.StudentDashboardResponse;
import com.attendance.system.dto.response.StudentProfileResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class StudentDashboardService {

    private final StudentProfileService studentProfileService;
    private final AttendanceRecordService attendanceRecordService;
    private final StudentClassService studentClassService;

    public StudentDashboardService(
            StudentProfileService studentProfileService,
            AttendanceRecordService attendanceRecordService,
            StudentClassService studentClassService) {

        this.studentProfileService = studentProfileService;
        this.attendanceRecordService = attendanceRecordService;
        this.studentClassService = studentClassService;
    }

    public StudentDashboardResponse getMyDashboard(UUID userId) {

        // 1. Get student profile
        StudentProfileResponse profile =
                studentProfileService.getMyProfile(userId);

        // 2. Get attendance summary
        AttendanceSummaryResponse attendanceSummary =
                attendanceRecordService.getSummaryByUserId(userId);

        // 3. Get student's conducted classes
        List<StudentClassResponse> classes =
                studentClassService.getMyClasses(userId);

        // 4. Build dashboard response
        return new StudentDashboardResponse(
                profile,
                attendanceSummary,
                classes
        );
    }
}