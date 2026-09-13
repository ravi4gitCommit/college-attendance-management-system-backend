package com.attendance.system.repository;

import com.attendance.system.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SemesterRepository extends JpaRepository<Semester, Short> {

    List<Semester> findAllByOrderByNumberAsc();
}