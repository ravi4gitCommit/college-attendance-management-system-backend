package com.attendance.system.dto.response;

import com.attendance.system.entity.AttendanceStatus;

public class TeacherClassStudentResponse {

    private Long studentId;
    private String studentName;
    private String rollNumber;
    private AttendanceStatus attendanceStatus;

    public TeacherClassStudentResponse(
            Long studentId,
            String studentName,
            String rollNumber,
            AttendanceStatus attendanceStatus
    ) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.rollNumber = rollNumber;
        this.attendanceStatus = attendanceStatus;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public AttendanceStatus getAttendanceStatus() {
        return attendanceStatus;
    }
}