package com.attendance.system.dto.request;

import com.attendance.system.entity.AttendanceStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttendanceCorrectionRequestDto {

    private Long attendanceRecordId;

    private AttendanceStatus newStatus;

    private String reason;
}
