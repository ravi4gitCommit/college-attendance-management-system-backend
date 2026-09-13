package com.attendance.system.dto.request;

import com.attendance.system.entity.AttendanceStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AttendanceSubmitRequest {

    private List<AttendanceItem> attendance;

    @Getter
    @Setter
    public static class AttendanceItem {

        private Long studentId;

        private AttendanceStatus status;
    }
}