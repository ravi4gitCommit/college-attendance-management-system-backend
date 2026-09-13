package com.attendance.system.repository;

import com.attendance.system.entity.TimetableSlot;
import com.attendance.system.entity.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import java.time.LocalDate;

public interface TimetableSlotRepository
        extends JpaRepository<TimetableSlot, Long> {

    List<TimetableSlot> findByTeacherAssignmentId(Long teacherAssignmentId);

    List<TimetableSlot> findByDayOfWeekAndStatus(
            Short dayOfWeek,
            RecordStatus status
    );

    List<TimetableSlot> findByDayOfWeekAndStatusAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqual(
            Short dayOfWeek,
            RecordStatus status,
            LocalDate date1,
            LocalDate date2
    );
}