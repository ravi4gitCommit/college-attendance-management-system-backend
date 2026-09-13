package com.attendance.system.dto.response;

public class AttendanceSummaryResponse {

    private long totalClasses;
    private long present;
    private long absent;
    private double attendancePercentage;

    public AttendanceSummaryResponse(
            long totalClasses,
            long present,
            long absent,
            double attendancePercentage) {

        this.totalClasses = totalClasses;
        this.present = present;
        this.absent = absent;
        this.attendancePercentage = attendancePercentage;
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