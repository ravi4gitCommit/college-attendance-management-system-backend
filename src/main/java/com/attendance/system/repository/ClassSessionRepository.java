
package com.attendance.system.repository;

import com.attendance.system.entity.ClassSession;
import com.attendance.system.entity.ClassSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ClassSessionRepository
        extends JpaRepository<ClassSession, Long> {

    List<ClassSession> findByTeacherAssignmentId(
            Long teacherAssignmentId
    );

    List<ClassSession> findBySessionDate(
            LocalDate sessionDate
    );

    List<ClassSession> findByStatus(
            ClassSessionStatus status
    );

    boolean existsByExtraClassRequestId(Long extraClassRequestId);
}