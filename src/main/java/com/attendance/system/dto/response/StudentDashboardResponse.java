package com.attendance.system.dto.response;

import java.util.List;

public class StudentDashboardResponse {

    private StudentProfileResponse profile;
    private AttendanceSummaryResponse attendanceSummary;
    private List<StudentClassResponse> classes;

    public StudentDashboardResponse(
            StudentProfileResponse profile,
            AttendanceSummaryResponse attendanceSummary,
            List<StudentClassResponse> classes) {

        this.profile = profile;
        this.attendanceSummary = attendanceSummary;
        this.classes = classes;
    }

    public StudentProfileResponse getProfile() {
        return profile;
    }

    public AttendanceSummaryResponse getAttendanceSummary() {
        return attendanceSummary;
    }

    public List<StudentClassResponse> getClasses() {
        return classes;
    }
}