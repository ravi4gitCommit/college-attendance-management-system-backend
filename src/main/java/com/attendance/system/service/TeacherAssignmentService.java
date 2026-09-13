package com.attendance.system.service;

import com.attendance.system.entity.AssignmentStatus;
import com.attendance.system.entity.Department;
import com.attendance.system.entity.Program;
import com.attendance.system.entity.SubjectOffering;
import com.attendance.system.entity.Teacher;
import com.attendance.system.entity.TeacherAssignment;
import com.attendance.system.entity.User;
import com.attendance.system.repository.ClassSessionRepository;
import com.attendance.system.repository.DepartmentRepository;
import com.attendance.system.repository.ExtraClassRequestRepository;
import com.attendance.system.repository.ProgramRepository;
import com.attendance.system.repository.SubjectOfferingRepository;
import com.attendance.system.repository.TeacherAssignmentRepository;
import com.attendance.system.repository.TeacherRepository;
import com.attendance.system.repository.TimetableSlotRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TeacherAssignmentService {

    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final TeacherRepository teacherRepository;
    private final DepartmentRepository departmentRepository;
    private final SubjectOfferingRepository subjectOfferingRepository;
    private final ProgramRepository programRepository;
    private final UserService userService;

    private final ClassSessionRepository classSessionRepository;
    private final ExtraClassRequestRepository extraClassRequestRepository;
    private final TimetableSlotRepository timetableSlotRepository;

    public TeacherAssignmentService(
            TeacherAssignmentRepository teacherAssignmentRepository,
            TeacherRepository teacherRepository,
            DepartmentRepository departmentRepository,
            SubjectOfferingRepository subjectOfferingRepository,
            ProgramRepository programRepository,
            UserService userService,
            ClassSessionRepository classSessionRepository,
            ExtraClassRequestRepository extraClassRequestRepository,
            TimetableSlotRepository timetableSlotRepository
    ) {
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.teacherRepository = teacherRepository;
        this.departmentRepository = departmentRepository;
        this.subjectOfferingRepository = subjectOfferingRepository;
        this.programRepository = programRepository;
        this.userService = userService;
        this.classSessionRepository = classSessionRepository;
        this.extraClassRequestRepository = extraClassRequestRepository;
        this.timetableSlotRepository = timetableSlotRepository;
    }

    // Existing generic method preserved.
    public List<TeacherAssignment> getAllAssignments() {
        return teacherAssignmentRepository.findAll();
    }

    // Existing generic method preserved.
    public Optional<TeacherAssignment> getAssignmentById(Long id) {
        return teacherAssignmentRepository.findById(id);
    }

    // Existing generic method preserved.
    public List<TeacherAssignment> getAssignmentsByTeacher(
            Long teacherId
    ) {
        return teacherAssignmentRepository.findByTeacherId(teacherId);
    }

    // Existing teacher self-service method preserved.
    public List<TeacherAssignment> getMyAssignments(Long teacherId) {
        return teacherAssignmentRepository.findByTeacherId(teacherId);
    }

    // Existing generic method preserved.
    public List<TeacherAssignment> getAssignmentsBySubjectOffering(
            Long subjectOfferingId
    ) {
        return teacherAssignmentRepository
                .findBySubjectOfferingId(subjectOfferingId);
    }

    /*
     * College-scoped methods for College Admin APIs.
     *
     * These methods do not remove the existing generic methods.
     * They only expose assignments belonging to the authenticated
     * College Admin's college.
     */

    public List<TeacherAssignment> getAllAssignmentsForCollege(
            UUID adminUserId
    ) {
        Long collegeId = getAdminCollegeId(adminUserId);

        return teacherAssignmentRepository.findAll()
                .stream()
                .filter(assignment -> isAssignmentInCollege(
                        assignment,
                        collegeId
                ))
                .toList();
    }

    public Optional<TeacherAssignment> getAssignmentByIdForCollege(
            Long id,
            UUID adminUserId
    ) {
        Long collegeId = getAdminCollegeId(adminUserId);

        return teacherAssignmentRepository.findById(id)
                .filter(assignment ->
                        isAssignmentInCollege(
                                assignment,
                                collegeId
                        ));
    }

    public List<TeacherAssignment> getAssignmentsByTeacherForCollege(
            Long teacherId,
            UUID adminUserId
    ) {
        Long collegeId = getAdminCollegeId(adminUserId);

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Teacher not found"
                        ));

        if (!isTeacherInCollege(teacher, collegeId)) {
            throw new AccessDeniedException(
                    "Teacher does not belong to your college"
            );
        }

        return teacherAssignmentRepository
                .findByTeacherId(teacherId);
    }

    public List<TeacherAssignment> getAssignmentsBySubjectOfferingForCollege(
            Long subjectOfferingId,
            UUID adminUserId
    ) {
        Long collegeId = getAdminCollegeId(adminUserId);

        if (!isSubjectOfferingInCollege(
                subjectOfferingId,
                collegeId
        )) {
            throw new AccessDeniedException(
                    "Subject offering does not belong to your college"
            );
        }

        return teacherAssignmentRepository
                .findBySubjectOfferingId(subjectOfferingId)
                .stream()
                .filter(assignment ->
                        isAssignmentInCollege(
                                assignment,
                                collegeId
                        ))
                .toList();
    }

    public TeacherAssignment createAssignmentForCollege(
            TeacherAssignment assignment,
            UUID adminUserId
    ) {
        Long collegeId = getAdminCollegeId(adminUserId);

        validateTeacherBelongsToCollege(
                assignment.getTeacherId(),
                collegeId
        );

        validateSubjectOfferingBelongsToCollege(
                assignment.getSubjectOfferingId(),
                collegeId
        );

        return createAssignment(assignment);
    }

    public Optional<TeacherAssignment> updateAssignmentForCollege(
            Long id,
            TeacherAssignment updatedAssignment,
            UUID adminUserId
    ) {
        Long collegeId = getAdminCollegeId(adminUserId);

        Optional<TeacherAssignment> existing =
                teacherAssignmentRepository.findById(id);

        if (existing.isEmpty()) {
            return Optional.empty();
        }

        if (!isAssignmentInCollege(
                existing.get(),
                collegeId
        )) {
            return Optional.empty();
        }

        validateTeacherBelongsToCollege(
                updatedAssignment.getTeacherId(),
                collegeId
        );

        validateSubjectOfferingBelongsToCollege(
                updatedAssignment.getSubjectOfferingId(),
                collegeId
        );

        return updateAssignment(
                id,
                updatedAssignment
        );
    }

    public boolean deleteAssignmentForCollege(
            Long id,
            UUID adminUserId
    ) {
        Long collegeId = getAdminCollegeId(adminUserId);

        Optional<TeacherAssignment> assignment =
                teacherAssignmentRepository.findById(id);

        if (assignment.isEmpty()) {
            return false;
        }

        if (!isAssignmentInCollege(
                assignment.get(),
                collegeId
        )) {
            return false;
        }

        return deleteAssignment(id);
    }

    // Existing create method preserved.
    public TeacherAssignment createAssignment(
            TeacherAssignment assignment
    ) {

        if (assignment.getStatus() == null) {
            assignment.setStatus(AssignmentStatus.active);
        }

        if (assignment.getAssignedAt() == null) {
            assignment.setAssignedAt(OffsetDateTime.now());
        }

        return teacherAssignmentRepository.save(assignment);
    }

    // Existing update method preserved.
    public Optional<TeacherAssignment> updateAssignment(
            Long id,
            TeacherAssignment updatedAssignment
    ) {

        return teacherAssignmentRepository.findById(id)
                .map(existingAssignment -> {

                    existingAssignment.setTeacherId(
                            updatedAssignment.getTeacherId()
                    );

                    existingAssignment.setSubjectOfferingId(
                            updatedAssignment.getSubjectOfferingId()
                    );

                    if (updatedAssignment.getStatus() != null) {
                        existingAssignment.setStatus(
                                updatedAssignment.getStatus()
                        );
                    }

                    if (updatedAssignment.getAssignedAt() != null) {
                        existingAssignment.setAssignedAt(
                                updatedAssignment.getAssignedAt()
                        );
                    }

                    existingAssignment.setEndedAt(
                            updatedAssignment.getEndedAt()
                    );

                    return teacherAssignmentRepository.save(
                            existingAssignment
                    );
                });
    }

    // Existing delete method preserved.
    public boolean deleteAssignment(Long id) {

        if (!teacherAssignmentRepository.existsById(id)) {
            return false;
        }

        if (classSessionRepository.findByTeacherAssignmentId(id)
                .stream()
                .findAny()
                .isPresent()) {
            throw new IllegalStateException(
                    "Cannot delete teacher assignment because class sessions exist"
            );
        }

        if (extraClassRequestRepository.findByTeacherAssignmentId(id)
                .stream()
                .findAny()
                .isPresent()) {
            throw new IllegalStateException(
                    "Cannot delete teacher assignment because extra class requests exist"
            );
        }

        if (timetableSlotRepository.findByTeacherAssignmentId(id)
                .stream()
                .findAny()
                .isPresent()) {
            throw new IllegalStateException(
                    "Cannot delete teacher assignment because timetable slots exist"
            );
        }

        teacherAssignmentRepository.deleteById(id);
        return true;
    }

    private Long getAdminCollegeId(UUID adminUserId) {

        User admin = userService.findById(adminUserId)
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
            TeacherAssignment assignment,
            Long collegeId
    ) {
        return teacherRepository.findById(
                        assignment.getTeacherId()
                )
                .map(teacher ->
                        isTeacherInCollege(
                                teacher,
                                collegeId
                        ))
                .orElse(false);
    }

    private boolean isTeacherInCollege(
            Teacher teacher,
            Long collegeId
    ) {
        return departmentRepository.findById(
                        teacher.getDepartmentId()
                )
                .map(department ->
                        collegeId.equals(
                                department.getCollegeId()
                        ))
                .orElse(false);
    }

    private boolean isSubjectOfferingInCollege(
            Long subjectOfferingId,
            Long collegeId
    ) {
        SubjectOffering offering =
                subjectOfferingRepository.findById(
                        subjectOfferingId
                ).orElse(null);

        if (offering == null) {
            return false;
        }

        Program program =
                programRepository.findById(
                        offering.getProgramId()
                ).orElse(null);

        if (program == null) {
            return false;
        }

        Department department =
                departmentRepository.findById(
                        program.getDepartmentId()
                ).orElse(null);

        return department != null
                && collegeId.equals(
                department.getCollegeId()
        );
    }

    private void validateTeacherBelongsToCollege(
            Long teacherId,
            Long collegeId
    ) {
        Teacher teacher = teacherRepository.findById(
                teacherId
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Teacher not found"
                ));

        if (!isTeacherInCollege(
                teacher,
                collegeId
        )) {
            throw new AccessDeniedException(
                    "Teacher does not belong to your college"
            );
        }
    }

    private void validateSubjectOfferingBelongsToCollege(
            Long subjectOfferingId,
            Long collegeId
    ) {
        if (!isSubjectOfferingInCollege(
                subjectOfferingId,
                collegeId
        )) {
            throw new AccessDeniedException(
                    "Subject offering does not belong to your college"
            );
        }
    }
}
