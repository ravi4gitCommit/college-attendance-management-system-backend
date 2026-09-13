
package com.attendance.system.repository;

import com.attendance.system.entity.SubjectOffering;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SubjectOfferingRepository
        extends JpaRepository<SubjectOffering, Long> {

    Optional<SubjectOffering> findById(Long id);

    List<SubjectOffering> findByProgramIdAndAcademicSessionIdAndSemesterIdAndSectionId(
            Long programId,
            Long academicSessionId,
            Short semesterId,
            Long sectionId
    );
}