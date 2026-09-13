package com.attendance.system.repository;

import com.attendance.system.entity.Program;
import com.attendance.system.entity.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProgramRepository extends JpaRepository<Program, Long> {

    Optional<Program> findByIdAndStatus(
            Long id,
            RecordStatus status
    );
}