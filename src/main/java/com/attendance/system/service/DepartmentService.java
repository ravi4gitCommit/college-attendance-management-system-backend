package com.attendance.system.service;

import com.attendance.system.entity.Department;
import com.attendance.system.entity.RecordStatus;
import com.attendance.system.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public List<Department> getDepartmentsByCollege(Long collegeId) {
        return departmentRepository.findByCollegeId(collegeId);
    }

    public Optional<Department> getDepartmentById(Long id, Long collegeId) {
        return departmentRepository.findById(id)
                .filter(department -> department.getCollegeId().equals(collegeId));
    }

    public Department createDepartment(Department department, Long collegeId) {

        if (departmentRepository.existsByCollegeIdAndCodeIgnoreCase(
                collegeId,
                department.getCode()
        )) {
            throw new IllegalArgumentException(
                    "Department code already exists in this college"
            );
        }

        department.setCollegeId(collegeId);

        if (department.getStatus() == null) {
            department.setStatus(RecordStatus.active);
        }

        return departmentRepository.save(department);
    }

    public Optional<Department> updateDepartment(
            Long id,
            Department updatedDepartment,
            Long collegeId
    ) {
        return departmentRepository.findById(id)
                .filter(department -> department.getCollegeId().equals(collegeId))
                .map(existingDepartment -> {

                    if (departmentRepository.existsByCollegeIdAndCodeIgnoreCase(
                            collegeId,
                            updatedDepartment.getCode()
                    )
                            && !existingDepartment.getCode()
                            .equalsIgnoreCase(updatedDepartment.getCode())) {

                        throw new IllegalArgumentException(
                                "Department code already exists in this college"
                        );
                    }

                    existingDepartment.setName(updatedDepartment.getName());
                    existingDepartment.setCode(updatedDepartment.getCode());

                    if (updatedDepartment.getStatus() != null) {
                        existingDepartment.setStatus(updatedDepartment.getStatus());
                    }

                    return departmentRepository.save(existingDepartment);
                });
    }

    public boolean deleteDepartment(Long id, Long collegeId) {

        Optional<Department> department =
                departmentRepository.findById(id)
                        .filter(d -> d.getCollegeId().equals(collegeId));

        if (department.isEmpty()) {
            return false;
        }

        departmentRepository.delete(department.get());
        return true;
    }
}