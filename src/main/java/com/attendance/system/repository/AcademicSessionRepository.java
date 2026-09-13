package com.attendance.system.repository;

import com.attendance.system.entity.AcademicSession;
import com.attendance.system.entity.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcademicSessionRepository
        extends JpaRepository<AcademicSession, Long> {

    List<AcademicSession> findByCollegeIdAndStatus(
            Long collegeId,
            RecordStatus status
    );
}