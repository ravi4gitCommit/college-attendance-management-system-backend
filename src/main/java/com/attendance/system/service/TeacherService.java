package com.attendance.system.service;

import com.attendance.system.entity.Department;
import com.attendance.system.entity.RecordStatus;
import com.attendance.system.entity.Teacher;
import com.attendance.system.entity.User;
import com.attendance.system.repository.DepartmentRepository;
import com.attendance.system.repository.TeacherRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final DepartmentRepository departmentRepository;
    private final UserService userService;

    public TeacherService(
            TeacherRepository teacherRepository,
            DepartmentRepository departmentRepository,
            UserService userService
    ) {
        this.teacherRepository = teacherRepository;
        this.departmentRepository = departmentRepository;
        this.userService = userService;
    }

    // Get all teachers belonging to the logged-in College Admin's college.
    public List<Teacher> getAllTeachers(UUID adminUserId) {

        Long collegeId = getAdminCollegeId(adminUserId);

        return teacherRepository.findAll()
                .stream()
                .filter(teacher -> isTeacherInCollege(teacher, collegeId))
                .toList();
    }

    // Get one teacher only if the teacher belongs to the admin's college.
    public Optional<Teacher> getTeacherById(
            Long id,
            UUID adminUserId
    ) {

        Long collegeId = getAdminCollegeId(adminUserId);

        return teacherRepository.findById(id)
                .filter(teacher ->
                        isTeacherInCollege(teacher, collegeId)
                );
    }

    // Existing lookup by linked Supabase Auth user ID.
    public Optional<Teacher> getTeacherByUserId(UUID userId) {
        return teacherRepository.findByUserId(userId);
    }

    // Get teachers of a department only if that department
    // belongs to the logged-in College Admin's college.
    public List<Teacher> getTeachersByDepartment(
            Long departmentId,
            UUID adminUserId
    ) {

        Long collegeId = getAdminCollegeId(adminUserId);

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Department not found"
                        )
                );

        if (!collegeId.equals(department.getCollegeId())) {
            throw new AccessDeniedException(
                    "Department does not belong to your college"
            );
        }

        return teacherRepository.findByDepartmentId(departmentId);
    }

    // Create a teacher only inside the admin's college.
    public Teacher createTeacher(
            Teacher teacher,
            UUID adminUserId
    ) {

        Long collegeId = getAdminCollegeId(adminUserId);

        // Verify that the selected department belongs
        // to the logged-in College Admin's college.
        Department department = departmentRepository.findById(
                teacher.getDepartmentId()
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Department not found"
                )
        );

        if (!collegeId.equals(department.getCollegeId())) {
            throw new AccessDeniedException(
                    "Department does not belong to your college"
            );
        }

        // Verify that the linked users record exists.
        User user = userService.findById(teacher.getUserId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );

        // Verify that the linked user belongs
        // to the logged-in College Admin's college.
        if (!collegeId.equals(user.getCollegeId())) {
            throw new AccessDeniedException(
                    "User does not belong to your college"
            );
        }

        // A teacher profile must be linked to a teacher-role user.
        if (!"teacher".equals(user.getRole())) {
            throw new IllegalArgumentException(
                    "User role must be teacher"
            );
        }

        // Only active users can be registered as teachers.
        if (!"active".equals(user.getStatus())) {
            throw new IllegalArgumentException(
                    "User is not active"
            );
        }

        // A user can have only one teacher profile.
        if (teacherRepository.existsByUserId(teacher.getUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User is already registered as a teacher"
            );
        }

        // Employee ID is optional, but if provided it must be unique.
        if (teacher.getEmployeeId() != null
                && !teacher.getEmployeeId().isBlank()
                && teacherRepository.existsByEmployeeIdIgnoreCase(
                teacher.getEmployeeId()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Employee ID already exists"
            );
        }

        if (teacher.getStatus() == null) {
            teacher.setStatus(RecordStatus.active);
        }

        return teacherRepository.save(teacher);
    }

    // Update teacher only if the existing teacher belongs
    // to the logged-in College Admin's college.
    public Optional<Teacher> updateTeacher(
            Long id,
            Teacher updatedTeacher,
            UUID adminUserId
    ) {

        Long collegeId = getAdminCollegeId(adminUserId);

        return teacherRepository.findById(id)
                .filter(existingTeacher ->
                        isTeacherInCollege(existingTeacher, collegeId)
                )
                .map(existingTeacher -> {

                    // Verify that the new department belongs
                    // to the admin's college.
                    Department department =
                            departmentRepository.findById(
                                    updatedTeacher.getDepartmentId()
                            ).orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Department not found"
                                    )
                            );

                    if (!collegeId.equals(department.getCollegeId())) {
                        throw new AccessDeniedException(
                                "Department does not belong to your college"
                        );
                    }

                    // Employee ID is optional.
                    // When provided, exclude the current teacher
                    // from the duplicate check.
                    if (updatedTeacher.getEmployeeId() != null
                            && !updatedTeacher.getEmployeeId().isBlank()
                            && teacherRepository
                            .existsByEmployeeIdIgnoreCaseAndIdNot(
                                    updatedTeacher.getEmployeeId(),
                                    id
                            )) {

                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Employee ID already exists"
                        );
                    }

                    // Update only editable teacher fields.
                    // userId is intentionally NOT changed.
                    existingTeacher.setDepartmentId(
                            updatedTeacher.getDepartmentId()
                    );

                    existingTeacher.setEmployeeId(
                            updatedTeacher.getEmployeeId()
                    );

                    if (updatedTeacher.getStatus() != null) {
                        existingTeacher.setStatus(
                                updatedTeacher.getStatus()
                        );
                    }

                    return teacherRepository.save(existingTeacher);
                });
    }

    // Delete teacher only if the teacher belongs
    // to the logged-in College Admin's college.
    public boolean deleteTeacher(
            Long id,
            UUID adminUserId
    ) {

        Long collegeId = getAdminCollegeId(adminUserId);

        Optional<Teacher> teacher =
                teacherRepository.findById(id);

        if (teacher.isEmpty()) {
            return false;
        }

        if (!isTeacherInCollege(teacher.get(), collegeId)) {
            return false;
        }

        teacherRepository.deleteById(id);
        return true;
    }

    // Get the college ID of the logged-in College Admin.
    private Long getAdminCollegeId(UUID adminUserId) {

        return userService.findById(adminUserId)
                .map(User::getCollegeId)
                .filter(collegeId -> collegeId != null)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Admin user is not assigned to a college"
                        )
                );
    }

    // Check whether the teacher's department belongs
    // to the given college.
    private boolean isTeacherInCollege(
            Teacher teacher,
            Long collegeId
    ) {

        return departmentRepository.findById(
                        teacher.getDepartmentId()
                )
                .map(department ->
                        collegeId.equals(department.getCollegeId())
                )
                .orElse(false);
    }
}
