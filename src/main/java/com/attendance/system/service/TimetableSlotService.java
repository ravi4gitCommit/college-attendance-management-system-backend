package com.attendance.system.service;

import com.attendance.system.entity.Department;
import com.attendance.system.entity.RecordStatus;
import com.attendance.system.entity.TimetableSlot;
import com.attendance.system.entity.User;
import com.attendance.system.repository.DepartmentRepository;
import com.attendance.system.repository.TeacherAssignmentRepository;
import com.attendance.system.repository.TeacherRepository;
import com.attendance.system.repository.TimetableSlotRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TimetableSlotService {

    private final TimetableSlotRepository timetableSlotRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final TeacherRepository teacherRepository;
    private final DepartmentRepository departmentRepository;
    private final UserService userService;

    public TimetableSlotService(
            TimetableSlotRepository timetableSlotRepository,
            TeacherAssignmentRepository teacherAssignmentRepository,
            TeacherRepository teacherRepository,
            DepartmentRepository departmentRepository,
            UserService userService
    ) {
        this.timetableSlotRepository = timetableSlotRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.teacherRepository = teacherRepository;
        this.departmentRepository = departmentRepository;
        this.userService = userService;
    }

    public List<TimetableSlot> getAllSlots() {
        return timetableSlotRepository.findAll();
    }

    public Optional<TimetableSlot> getSlotById(Long id) {
        return timetableSlotRepository.findById(id);
    }

    public List<TimetableSlot> getSlotsByTeacherAssignment(
            Long teacherAssignmentId
    ) {
        return timetableSlotRepository
                .findByTeacherAssignmentId(teacherAssignmentId);
    }

    public List<TimetableSlot> getActiveSlotsForDate(
            Short dayOfWeek,
            LocalDate date
    ) {
        return timetableSlotRepository
                .findByDayOfWeekAndStatusAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqual(
                        dayOfWeek,
                        RecordStatus.active,
                        date,
                        date
                );
    }

    // Get all timetable slots belonging to the admin's college
    public List<TimetableSlot> getAllSlotsForCollege(
            UUID adminUserId
    ) {
        Long collegeId = getAdminCollegeId(adminUserId);

        return timetableSlotRepository.findAll()
                .stream()
                .filter(slot ->
                        isTimetableSlotInCollege(slot, collegeId)
                )
                .toList();
    }

    // Get one timetable slot only if it belongs to the admin's college
    public Optional<TimetableSlot> getSlotByIdForCollege(
            Long id,
            UUID adminUserId
    ) {
        Long collegeId = getAdminCollegeId(adminUserId);

        return timetableSlotRepository.findById(id)
                .filter(slot ->
                        isTimetableSlotInCollege(slot, collegeId)
                );
    }

    // Create timetable slot only for an assignment belonging to the admin's college
    public TimetableSlot createSlotForCollege(
            TimetableSlot slot,
            UUID adminUserId
    ) {
        Long collegeId = getAdminCollegeId(adminUserId);

        if (slot.getTeacherAssignmentId() == null) {
            throw new IllegalArgumentException(
                    "teacherAssignmentId is required"
            );
        }

        if (!isAssignmentInCollege(
                slot.getTeacherAssignmentId(),
                collegeId
        )) {
            throw new AccessDeniedException(
                    "Teacher assignment does not belong to your college"
            );
        }

        return createSlot(slot);
    }

    // Update timetable slot only inside the admin's college
    public Optional<TimetableSlot> updateSlotForCollege(
            Long id,
            TimetableSlot updatedSlot,
            UUID adminUserId
    ) {
        Long collegeId = getAdminCollegeId(adminUserId);

        Optional<TimetableSlot> existingSlot =
                timetableSlotRepository.findById(id);

        if (existingSlot.isEmpty()) {
            return Optional.empty();
        }

        if (!isTimetableSlotInCollege(
                existingSlot.get(),
                collegeId
        )) {
            return Optional.empty();
        }

        if (updatedSlot.getTeacherAssignmentId() == null) {
            throw new IllegalArgumentException(
                    "teacherAssignmentId is required"
            );
        }

        if (!isAssignmentInCollege(
                updatedSlot.getTeacherAssignmentId(),
                collegeId
        )) {
            throw new AccessDeniedException(
                    "Teacher assignment does not belong to your college"
            );
        }

        return updateSlot(id, updatedSlot);
    }

    // Delete timetable slot only inside the admin's college
    public boolean deleteSlotForCollege(
            Long id,
            UUID adminUserId
    ) {
        Long collegeId = getAdminCollegeId(adminUserId);

        Optional<TimetableSlot> existingSlot =
                timetableSlotRepository.findById(id);

        if (existingSlot.isEmpty()) {
            return false;
        }

        if (!isTimetableSlotInCollege(
                existingSlot.get(),
                collegeId
        )) {
            return false;
        }

        return deleteSlot(id);
    }

    public TimetableSlot createSlot(TimetableSlot slot) {

        validateSlot(slot);

        if (slot.getStatus() == null) {
            slot.setStatus(RecordStatus.active);
        }

        return timetableSlotRepository.save(slot);
    }

    public Optional<TimetableSlot> updateSlot(
            Long id,
            TimetableSlot updatedSlot
    ) {

        return timetableSlotRepository.findById(id)
                .map(existingSlot -> {

                    validateSlot(updatedSlot);

                    existingSlot.setTeacherAssignmentId(
                            updatedSlot.getTeacherAssignmentId()
                    );

                    existingSlot.setDayOfWeek(
                            updatedSlot.getDayOfWeek()
                    );

                    existingSlot.setStartTime(
                            updatedSlot.getStartTime()
                    );

                    existingSlot.setEndTime(
                            updatedSlot.getEndTime()
                    );

                    existingSlot.setRoom(
                            updatedSlot.getRoom()
                    );

                    existingSlot.setEffectiveFrom(
                            updatedSlot.getEffectiveFrom()
                    );

                    existingSlot.setEffectiveTo(
                            updatedSlot.getEffectiveTo()
                    );

                    if (updatedSlot.getStatus() != null) {
                        existingSlot.setStatus(
                                updatedSlot.getStatus()
                        );
                    }

                    return timetableSlotRepository.save(existingSlot);
                });
    }

    public boolean deleteSlot(Long id) {

        if (!timetableSlotRepository.existsById(id)) {
            return false;
        }

        timetableSlotRepository.deleteById(id);
        return true;
    }

    private Long getAdminCollegeId(UUID adminUserId) {

        User admin = userService
                .findById(adminUserId)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Admin user not found"
                        ));

        if (!"college_admin".equals(admin.getRole())) {
            throw new AccessDeniedException(
                    "User is not a college admin"
            );
        }

        if (!"active".equals(admin.getStatus())) {
            throw new AccessDeniedException(
                    "Admin user is not active"
            );
        }

        if (admin.getCollegeId() == null) {
            throw new AccessDeniedException(
                    "Admin user is not assigned to a college"
            );
        }

        return admin.getCollegeId();
    }

    private boolean isAssignmentInCollege(
            Long teacherAssignmentId,
            Long collegeId
    ) {
        return teacherAssignmentRepository
                .findById(teacherAssignmentId)
                .flatMap(assignment ->
                        teacherRepository.findById(
                                assignment.getTeacherId()
                        ))
                .flatMap(teacher ->
                        departmentRepository.findById(
                                teacher.getDepartmentId()
                        ))
                .map(department ->
                        collegeId.equals(
                                department.getCollegeId()
                        ))
                .orElse(false);
    }

    private boolean isTimetableSlotInCollege(
            TimetableSlot slot,
            Long collegeId
    ) {
        return isAssignmentInCollege(
                slot.getTeacherAssignmentId(),
                collegeId
        );
    }

    private void validateSlot(TimetableSlot slot) {

        if (slot.getTeacherAssignmentId() == null) {
            throw new IllegalArgumentException(
                    "teacherAssignmentId is required"
            );
        }

        if (slot.getDayOfWeek() == null) {
            throw new IllegalArgumentException(
                    "dayOfWeek is required"
            );
        }

        if (slot.getStartTime() == null
                || slot.getEndTime() == null) {
            throw new IllegalArgumentException(
                    "startTime and endTime are required"
            );
        }

        if (!slot.getEndTime().isAfter(
                slot.getStartTime()
        )) {
            throw new IllegalArgumentException(
                    "endTime must be after startTime"
            );
        }

        if (slot.getEffectiveFrom() == null
                || slot.getEffectiveTo() == null) {
            throw new IllegalArgumentException(
                    "effectiveFrom and effectiveTo are required"
            );
        }

        if (slot.getEffectiveTo().isBefore(
                slot.getEffectiveFrom()
        )) {
            throw new IllegalArgumentException(
                    "effectiveTo must be on or after effectiveFrom"
            );
        }
    }
}