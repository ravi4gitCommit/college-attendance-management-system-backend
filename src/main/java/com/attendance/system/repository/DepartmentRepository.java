package com.attendance.system.repository;

import com.attendance.system.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findByCollegeId(Long collegeId);

    boolean existsByCollegeIdAndCodeIgnoreCase(Long collegeId, String code);
}