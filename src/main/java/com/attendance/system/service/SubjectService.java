package com.attendance.system.service;

import com.attendance.system.entity.Department;
import com.attendance.system.entity.RecordStatus;
import com.attendance.system.entity.Subject;
import com.attendance.system.repository.DepartmentRepository;
import com.attendance.system.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final DepartmentRepository departmentRepository;

    public SubjectService(
            SubjectRepository subjectRepository,
            DepartmentRepository departmentRepository
    ) {
        this.subjectRepository = subjectRepository;
        this.departmentRepository = departmentRepository;
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