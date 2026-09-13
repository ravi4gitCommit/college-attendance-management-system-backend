package com.attendance.system.repository;

import com.attendance.system.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    List<Teacher> findByDepartmentId(Long departmentId);

    Optional<Teacher> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    boolean existsByEmployeeIdIgnoreCase(String employeeId);

    boolean existsByEmployeeIdIgnoreCaseAndIdNot(
            String employeeId,
            Long id
    );
}
