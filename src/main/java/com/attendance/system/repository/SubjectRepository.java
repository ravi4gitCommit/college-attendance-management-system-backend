package com.attendance.system.repository;

import com.attendance.system.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findByDepartmentId(Long departmentId);

    boolean existsByDepartmentIdAndCodeIgnoreCase(
            Long departmentId,
            String code
    );
}