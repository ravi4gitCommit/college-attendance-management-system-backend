package com.attendance.system.repository;

import com.attendance.system.entity.AttendanceCorrectionRequest;
import com.attendance.system.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceCorrectionRequestRepository
        extends JpaRepository<AttendanceCorrectionRequest, Long> {

    List<AttendanceCorrectionRequest> findByRequestedBy(
            UUID requestedBy
    );

    List<AttendanceCorrectionRequest> findByStatus(
            RequestStatus status
    );

    Optional<AttendanceCorrectionRequest>
    findByAttendanceRecordIdAndStatus(
            Long attendanceRecordId,
            RequestStatus status
    );

    List<AttendanceCorrectionRequest>
    findByAttendanceRecordId(
            Long attendanceRecordId
    );
}
