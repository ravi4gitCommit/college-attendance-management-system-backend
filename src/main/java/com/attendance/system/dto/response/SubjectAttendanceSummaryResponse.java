package com.attendance.system.dto.response;

public class SubjectAttendanceSummaryResponse {

    private String subjectName;
    private String subjectCode;
    private long totalClasses;
    private long present;
    private long absent;
    private double attendancePercentage;

    public SubjectAttendanceSummaryResponse(
            String subjectName,
            String subjectCode,
            long totalClasses,
            long present,
            long absent,
            double attendancePercentage) {

        this.subjectName = subjectName;
        this.subjectCode = subjectCode;
        this.totalClasses = totalClasses;
        this.present = present;
        this.absent = absent;
        this.attendancePercentage = attendancePercentage;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public long getTotalClasses() {
        return totalClasses;
    }

    public long getPresent() {
        return present;
    }

    public long getAbsent() {
        return absent;
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }
}