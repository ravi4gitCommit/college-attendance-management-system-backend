package com.attendance.system.repository;

import com.attendance.system.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findByDepartmentId(Long departmentId);

    boolean existsByUserId(UUID userId);

    Optional<Student> findByUserId(UUID userId);

    boolean existsByRollNumberIgnoreCase(String rollNumber);

    boolean existsByRegistrationNumberIgnoreCase(String registrationNumber);

    boolean existsByRollNumberIgnoreCaseAndIdNot(
            String rollNumber,
            Long id
    );

    boolean existsByRegistrationNumberIgnoreCaseAndIdNot(
            String registrationNumber,
            Long id
    );
}
