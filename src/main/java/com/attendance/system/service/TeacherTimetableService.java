package com.attendance.system.service;

import com.attendance.system.entity.AssignmentStatus;
import com.attendance.system.entity.Teacher;
import com.attendance.system.entity.TeacherAssignment;
import com.attendance.system.entity.TimetableSlot;
import com.attendance.system.entity.RecordStatus;
import com.attendance.system.repository.TeacherAssignmentRepository;
import com.attendance.system.repository.TeacherRepository;
import com.attendance.system.repository.TimetableSlotRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TeacherTimetableService {

    private final TeacherRepository teacherRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final TimetableSlotRepository timetableSlotRepository;

    public TeacherTimetableService(
            TeacherRepository teacherRepository,
            TeacherAssignmentRepository teacherAssignmentRepository,
            TimetableSlotRepository timetableSlotRepository
    ) {
        this.teacherRepository = teacherRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.timetableSlotRepository = timetableSlotRepository;
    }

    public List<TimetableSlot> getMyTodayTimetable(UUID userId) {

        Teacher teacher = teacherRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Teacher profile not found"
                        ));

        LocalDate today = LocalDate.now();

        short dayOfWeek =
                (short) today.getDayOfWeek().getValue();

        List<TimetableSlot> todaySlots =
                timetableSlotRepository
                        .findByDayOfWeekAndStatusAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqual(
                                dayOfWeek,
                                RecordStatus.active,
                                today,
                                today
                        );

        List<TimetableSlot> response = new ArrayList<>();

        List<TeacherAssignment> assignments =
                teacherAssignmentRepository
                        .findByTeacherId(teacher.getId());

        for (TimetableSlot slot : todaySlots) {

            boolean belongsToTeacher = assignments.stream()
                    .anyMatch(assignment ->
                            assignment.getId().equals(
                                    slot.getTeacherAssignmentId()
                            )
                                    && assignment.getStatus()
                                    == AssignmentStatus.active
                    );

            if (!belongsToTeacher) {
                continue;
            }

            response.add(slot);
        }

        response.sort(
                (a, b) -> a.getStartTime()
                        .compareTo(b.getStartTime())
        );

        return response;
    }
}