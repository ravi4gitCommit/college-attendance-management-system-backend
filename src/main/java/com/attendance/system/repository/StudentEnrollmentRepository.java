package com.attendance.system.repository;

import com.attendance.system.entity.EnrollmentStatus;
import com.attendance.system.entity.StudentEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentEnrollmentRepository
        extends JpaRepository<StudentEnrollment, Long> {

    Optional<StudentEnrollment> findByStudentIdAndStatus(
            Long studentId,
            EnrollmentStatus status
    );

    List<StudentEnrollment> findByProgramIdAndAcademicSessionIdAndSemesterIdAndSectionIdAndStatus(
            Long programId,
            Long academicSessionId,
            Short semesterId,
            Long sectionId,
            EnrollmentStatus status
    );
}