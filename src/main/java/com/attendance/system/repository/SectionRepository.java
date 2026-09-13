package com.attendance.system.repository;

import com.attendance.system.entity.RecordStatus;
import com.attendance.system.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {

    List<Section> findByProgramIdAndAcademicSessionIdAndSemesterIdAndStatus(
            Long programId,
            Long academicSessionId,
            Short semesterId,
            RecordStatus status
    );
}
