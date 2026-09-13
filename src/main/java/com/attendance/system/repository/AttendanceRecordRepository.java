package com.attendance.system.repository;

import com.attendance.system.entity.AttendanceRecord;
import com.attendance.system.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository
        extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findByClassSessionId(Long classSessionId);

    List<AttendanceRecord> findByStudentId(Long studentId);

    Optional<AttendanceRecord> findByClassSessionIdAndStudentId(
            Long classSessionId,
            Long studentId
    );

    List<AttendanceRecord> findByStudentIdAndStatus(
            Long studentId,
            AttendanceStatus status
    );

    boolean existsByClassSessionIdAndStudentId(
            Long classSessionId,
            Long studentId
    );
}