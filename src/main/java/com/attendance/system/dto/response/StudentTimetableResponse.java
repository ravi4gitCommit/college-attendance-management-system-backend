package com.attendance.system.dto.response;

import java.time.LocalTime;

public class StudentTimetableResponse {

    private Long timetableSlotId;
    private String subjectName;
    private String subjectCode;
    private String teacherName;
    private Short dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String room;

    public StudentTimetableResponse(
            Long timetableSlotId,
            String subjectName,
            String subjectCode,
            String teacherName,
            Short dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            String room) {

        this.timetableSlotId = timetableSlotId;
        this.subjectName = subjectName;
        this.subjectCode = subjectCode;
        this.teacherName = teacherName;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
    }

    public Long getTimetableSlotId() {
        return timetableSlotId;
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

    public Short getDayOfWeek() {
        return dayOfWeek;
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
}