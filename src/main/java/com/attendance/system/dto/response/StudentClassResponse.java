package com.attendance.system.dto.response;

import com.attendance.system.entity.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public class StudentClassResponse {

    private Long classSessionId;
    private String subjectName;
    private String subjectCode;
    private String teacherName;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String room;
    private String sessionStatus;
    private AttendanceStatus attendanceStatus;

    public StudentClassResponse(
            Long classSessionId,
            String subjectName,
            String subjectCode,
            String teacherName,
            LocalDate sessionDate,
            LocalTime startTime,
            LocalTime endTime,
            String room,
            String sessionStatus,
            AttendanceStatus attendanceStatus) {

        this.classSessionId = classSessionId;
        this.subjectName = subjectName;
        this.subjectCode = subjectCode;
        this.teacherName = teacherName;
        this.sessionDate = sessionDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
        this.sessionStatus = sessionStatus;
        this.attendanceStatus = attendanceStatus;
    }

    public Long getClassSessionId() {
        return classSessionId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getRoom() {
        return room;
    }

    public String getSessionStatus() {
        return sessionStatus;
    }

    public AttendanceStatus getAttendanceStatus() {
        return attendanceStatus;
    }
}