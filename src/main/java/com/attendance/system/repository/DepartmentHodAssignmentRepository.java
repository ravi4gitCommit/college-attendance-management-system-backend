package com.attendance.system.repository;

import com.attendance.system.entity.DepartmentHodAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface DepartmentHodAssignmentRepository
        extends JpaRepository<DepartmentHodAssignment, Long> {

    @Query(value = """
            SELECT dha.department_id
            FROM department_hod_assignments dha
            JOIN hods h ON h.id = dha.hod_id
            WHERE h.user_id = :userId
              AND h.status = 'active'
              AND dha.status = 'active'
              AND dha.start_date <= :date
              AND (dha.end_date IS NULL OR dha.end_date >= :date)
            ORDER BY dha.start_date DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<Long> findActiveDepartmentIdByHodUserId(
            @Param("userId") UUID userId,
            @Param("date") LocalDate date
    );
}
