package com.attendance.system.service;

import com.attendance.system.dto.response.TeacherProfileResponse;
import com.attendance.system.entity.College;
import com.attendance.system.entity.Department;
import com.attendance.system.entity.Teacher;
import com.attendance.system.entity.User;
import com.attendance.system.repository.CollegeRepository;
import com.attendance.system.repository.DepartmentRepository;
import com.attendance.system.repository.TeacherRepository;
import com.attendance.system.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TeacherProfileService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final CollegeRepository collegeRepository;

    public TeacherProfileService(
            TeacherRepository teacherRepository,
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            CollegeRepository collegeRepository) {

        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.collegeRepository = collegeRepository;
    }

    public TeacherProfileResponse getMyProfile(UUID userId) {

        Teacher teacher = teacherRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Teacher profile not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Department department = departmentRepository
                .findById(teacher.getDepartmentId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Department not found"));

        College college = collegeRepository
                .findById(department.getCollegeId())
                .orElseThrow(() ->
                        new IllegalArgumentException("College not found"));

        return new TeacherProfileResponse(
                user.getFullName(),
                user.getEmail(),
                user.getMobile(),
                teacher.getEmployeeId(),
                department.getName(),
                department.getCode(),
                college.getName(),
                college.getCode(),
                college.getUniversity()
        );
    }
}