package com.attendance.system.dto.response;

import com.attendance.system.entity.ClassSession;
import com.attendance.system.entity.TeacherAssignment;
import com.attendance.system.entity.TimetableSlot;

import java.util.List;

public class TeacherDashboardResponse {

    private TeacherProfileResponse profile;
    private List<TeacherAssignment> assignments;
    private List<TimetableSlot> timetable;
    private List<ClassSession> classes;

    public TeacherDashboardResponse(
            TeacherProfileResponse profile,
            List<TeacherAssignment> assignments,
            List<TimetableSlot> timetable,
            List<ClassSession> classes
    ) {
        this.profile = profile;
        this.assignments = assignments;
        this.timetable = timetable;
        this.classes = classes;
    }

    public TeacherProfileResponse getProfile() {
        return profile;
    }

    public List<TeacherAssignment> getAssignments() {
        return assignments;
    }

    public List<TimetableSlot> getTimetable() {
        return timetable;
    }

    public List<ClassSession> getClasses() {
        return classes;
    }
}