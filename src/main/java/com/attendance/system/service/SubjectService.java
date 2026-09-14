package com.attendance.system.service;

import com.attendance.system.entity.Department;
import com.attendance.system.entity.RecordStatus;
import com.attendance.system.entity.Subject;
import com.attendance.system.entity.User;
import com.attendance.system.repository.DepartmentHodAssignmentRepository;
import com.attendance.system.repository.DepartmentRepository;
import com.attendance.system.repository.SubjectRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentHodAssignmentRepository departmentHodAssignmentRepository;
    private final UserService userService;

    public SubjectService(
            SubjectRepository subjectRepository,
            DepartmentRepository departmentRepository,
            DepartmentHodAssignmentRepository departmentHodAssignmentRepository,
            UserService userService
    ) {
        this.subjectRepository = subjectRepository;
        this.departmentRepository = departmentRepository;
        this.departmentHodAssignmentRepository =
                departmentHodAssignmentRepository;
        this.userService = userService;
    }

    // Get all subjects belonging only to the logged-in admin's college.
    public List<Subject> getAllSubjects(Long collegeId) {

        List<Department> departments =
                departmentRepository.findByCollegeId(collegeId);

        List<Subject> subjects = new ArrayList<>();

        for (Department department : departments) {
            subjects.addAll(
                    subjectRepository.findByDepartmentId(department.getId())
            );
        }

        return subjects;
    }

    // Get a subject only when its department belongs to the admin's college.
    public Optional<Subject> getSubjectById(
            Long id,
            Long collegeId
    ) {
        return subjectRepository.findById(id)
                .filter(subject ->
                        isDepartmentOwnedByCollege(
                                subject.getDepartmentId(),
                                collegeId
                        )
                );
    }

    // Get subjects of a department only when that department
    // belongs to the logged-in admin's college.
    public List<Subject> getSubjectsByDepartment(
            Long departmentId,
            Long collegeId
    ) {

        if (!isDepartmentOwnedByCollege(departmentId, collegeId)) {
            return List.of();
        }

        return subjectRepository.findByDepartmentId(departmentId);
    }

    // Create a subject only under a department belonging
    // to the logged-in admin's college.
    public Subject createSubject(
            Subject subject,
            Long collegeId
    ) {

        if (!isDepartmentOwnedByCollege(
                subject.getDepartmentId(),
                collegeId
        )) {
            throw new IllegalArgumentException(
                    "Department does not belong to your college"
            );
        }

        if (subjectRepository.existsByDepartmentIdAndCodeIgnoreCase(
                subject.getDepartmentId(),
                subject.getCode()
        )) {
            throw new IllegalArgumentException(
                    "Subject code already exists in this department"
            );
        }

        if (subject.getStatus() == null) {
            subject.setStatus(RecordStatus.active);
        }

        return subjectRepository.save(subject);
    }

    // Update a subject only when:
    // 1. Existing subject belongs to the admin's college.
    // 2. New department also belongs to the admin's college.
    // 3. Duplicate subject code is not created.
    public Optional<Subject> updateSubject(
            Long id,
            Subject updatedSubject,
            Long collegeId
    ) {

        return subjectRepository.findById(id)
                .filter(existingSubject ->
                        isDepartmentOwnedByCollege(
                                existingSubject.getDepartmentId(),
                                collegeId
                        )
                )
                .map(existingSubject -> {

                    Long newDepartmentId =
                            updatedSubject.getDepartmentId();

                    if (!isDepartmentOwnedByCollege(
                            newDepartmentId,
                            collegeId
                    )) {
                        throw new IllegalArgumentException(
                                "Department does not belong to your college"
                        );
                    }

                    boolean departmentChanged =
                            !existingSubject.getDepartmentId()
                                    .equals(newDepartmentId);

                    boolean codeChanged =
                            !existingSubject.getCode()
                                    .equalsIgnoreCase(
                                            updatedSubject.getCode()
                                    );

                    if (departmentChanged || codeChanged) {

                        if (subjectRepository
                                .existsByDepartmentIdAndCodeIgnoreCase(
                                        newDepartmentId,
                                        updatedSubject.getCode()
                                )) {

                            throw new IllegalArgumentException(
                                    "Subject code already exists in this department"
                            );
                        }
                    }

                    existingSubject.setDepartmentId(newDepartmentId);
                    existingSubject.setName(updatedSubject.getName());
                    existingSubject.setCode(updatedSubject.getCode());
                    existingSubject.setCredits(updatedSubject.getCredits());
                    existingSubject.setType(updatedSubject.getType());

                    if (updatedSubject.getStatus() != null) {
                        existingSubject.setStatus(
                                updatedSubject.getStatus()
                        );
                    }

                    return subjectRepository.save(existingSubject);
                });
    }

    // Delete only a subject belonging to the admin's college.
    public boolean deleteSubject(
            Long id,
            Long collegeId
    ) {

        Optional<Subject> subject =
                subjectRepository.findById(id)
                        .filter(existingSubject ->
                                isDepartmentOwnedByCollege(
                                        existingSubject.getDepartmentId(),
                                        collegeId
                                )
                        );

        if (subject.isEmpty()) {
            return false;
        }

        subjectRepository.delete(subject.get());

        return true;
    }

    // =========================================================
    // HOD SUBJECT METHODS
    // =========================================================

    // Get all subjects belonging to the HOD's currently assigned department.
    public List<Subject> getAllSubjectsForHod(UUID hodUserId) {

        Long departmentId = getHodDepartmentId(hodUserId);

        return subjectRepository.findByDepartmentId(departmentId);
    }

    // Get one subject only when it belongs to the HOD's department.
    public Optional<Subject> getSubjectByIdForHod(
            Long id,
            UUID hodUserId
    ) {

        Long departmentId = getHodDepartmentId(hodUserId);

        return subjectRepository.findById(id)
                .filter(subject ->
                        departmentId.equals(subject.getDepartmentId())
                );
    }

    // HOD can create a subject only inside their own department.
    public Subject createSubjectForHod(
            Subject subject,
            UUID hodUserId
    ) {

        Long hodDepartmentId = getHodDepartmentId(hodUserId);

        if (subject.getDepartmentId() == null) {
            throw new IllegalArgumentException(
                    "Department is required"
            );
        }

        if (!hodDepartmentId.equals(subject.getDepartmentId())) {
            throw new AccessDeniedException(
                    "Subject must belong to your department"
            );
        }

        if (subjectRepository.existsByDepartmentIdAndCodeIgnoreCase(
                hodDepartmentId,
                subject.getCode()
        )) {
            throw new IllegalArgumentException(
                    "Subject code already exists in this department"
            );
        }

        if (subject.getStatus() == null) {
            subject.setStatus(RecordStatus.active);
        }

        return subjectRepository.save(subject);
    }

    // HOD can update only a subject belonging to their department.
    public Optional<Subject> updateSubjectForHod(
            Long id,
            Subject updatedSubject,
            UUID hodUserId
    ) {

        Long hodDepartmentId = getHodDepartmentId(hodUserId);

        return subjectRepository.findById(id)
                .filter(existingSubject ->
                        hodDepartmentId.equals(
                                existingSubject.getDepartmentId()
                        )
                )
                .map(existingSubject -> {

                    if (updatedSubject.getDepartmentId() == null) {
                        throw new IllegalArgumentException(
                                "Department is required"
                        );
                    }

                    if (!hodDepartmentId.equals(
                            updatedSubject.getDepartmentId()
                    )) {
                        throw new AccessDeniedException(
                                "Subject must belong to your department"
                        );
                    }

                    boolean codeChanged =
                            !existingSubject.getCode()
                                    .equalsIgnoreCase(
                                            updatedSubject.getCode()
                                    );

                    if (codeChanged &&
                            subjectRepository
                                    .existsByDepartmentIdAndCodeIgnoreCase(
                                            hodDepartmentId,
                                            updatedSubject.getCode()
                                    )) {

                        throw new IllegalArgumentException(
                                "Subject code already exists in this department"
                        );
                    }

                    existingSubject.setDepartmentId(hodDepartmentId);
                    existingSubject.setName(updatedSubject.getName());
                    existingSubject.setCode(updatedSubject.getCode());
                    existingSubject.setCredits(updatedSubject.getCredits());
                    existingSubject.setType(updatedSubject.getType());

                    if (updatedSubject.getStatus() != null) {
                        existingSubject.setStatus(
                                updatedSubject.getStatus()
                        );
                    }

                    return subjectRepository.save(existingSubject);
                });
    }

    // HOD can delete only a subject belonging to their department.
    public boolean deleteSubjectForHod(
            Long id,
            UUID hodUserId
    ) {

        Long hodDepartmentId = getHodDepartmentId(hodUserId);

        Optional<Subject> subject =
                subjectRepository.findById(id)
                        .filter(existingSubject ->
                                hodDepartmentId.equals(
                                        existingSubject.getDepartmentId()
                                )
                        );

        if (subject.isEmpty()) {
            return false;
        }

        subjectRepository.delete(subject.get());

        return true;
    }

    // Resolve the HOD's currently active department assignment.
    private Long getHodDepartmentId(UUID hodUserId) {

        User hod = userService.findById(hodUserId)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "HOD user not found"
                        )
                );

        if (!"hod".equals(hod.getRole())) {
            throw new AccessDeniedException(
                    "User is not an HOD"
            );
        }

        if (!"active".equals(hod.getStatus())) {
            throw new AccessDeniedException(
                    "HOD user is not active"
            );
        }

        return departmentHodAssignmentRepository
                .findActiveDepartmentIdByHodUserId(
                        hodUserId,
                        LocalDate.now()
                )
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "HOD has no active department assignment"
                        )
                );
    }

    // Common ownership check.
    private boolean isDepartmentOwnedByCollege(
            Long departmentId,
            Long collegeId
    ) {

        return departmentRepository.findById(departmentId)
                .map(department ->
                        department.getCollegeId().equals(collegeId)
                )
                .orElse(false);
    }
}