package com.attendance.system.repository;

import com.attendance.system.entity.TeacherAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import com.attendance.system.entity.AssignmentStatus;

public interface TeacherAssignmentRepository
        extends JpaRepository<TeacherAssignment, Long> {

    List<TeacherAssignment> findByTeacherId(Long teacherId);

    List<TeacherAssignment> findBySubjectOfferingId(
            Long subjectOfferingId
    );

    boolean existsByTeacherIdAndSubjectOfferingIdAndStatus(
            Long teacherId,
            Long subjectOfferingId,
            AssignmentStatus status
    );
}