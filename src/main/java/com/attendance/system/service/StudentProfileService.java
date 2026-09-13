package com.attendance.system.service;

import com.attendance.system.dto.response.StudentProfileResponse;
import com.attendance.system.entity.*;
import com.attendance.system.repository.*;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StudentProfileService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;
    private final SemesterRepository semesterRepository;
    private final SectionRepository sectionRepository;
    private final ProgramRepository programRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final CollegeRepository collegeRepository;

    public StudentProfileService(
            StudentRepository studentRepository,
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            StudentEnrollmentRepository studentEnrollmentRepository,
            SemesterRepository semesterRepository,
            SectionRepository sectionRepository,
            ProgramRepository programRepository,
            AcademicSessionRepository academicSessionRepository,
            CollegeRepository collegeRepository) {

        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.studentEnrollmentRepository = studentEnrollmentRepository;
        this.semesterRepository = semesterRepository;
        this.sectionRepository = sectionRepository;
        this.programRepository = programRepository;
        this.academicSessionRepository = academicSessionRepository;
        this.collegeRepository = collegeRepository;
    }

    public StudentProfileResponse getMyProfile(UUID userId) {

        // 1. Find student
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Student not found"));

        // 2. Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        // 3. Find department
        Department department = departmentRepository.findById(
                student.getDepartmentId()
        ).orElseThrow(() ->
                new IllegalArgumentException("Department not found"));

        // 4. Find active enrollment
        StudentEnrollment enrollment =
                studentEnrollmentRepository.findByStudentIdAndStatus(
                        student.getId(),
                        EnrollmentStatus.active
                ).orElseThrow(() ->
                        new IllegalArgumentException("Active enrollment not found"));

        // 5. Find semester
        Semester semester = semesterRepository.findById(
                enrollment.getSemesterId()
        ).orElseThrow(() ->
                new IllegalArgumentException("Semester not found"));

        // 6. Find section
        Section section = sectionRepository.findById(
                enrollment.getSectionId()
        ).orElseThrow(() ->
                new IllegalArgumentException("Section not found"));

        // 7. Find program
        Program program = programRepository.findByIdAndStatus(
                enrollment.getProgramId(),
                RecordStatus.active
        ).orElseThrow(() ->
                new IllegalArgumentException("Program not found"));

        // 8. Find academic session
        AcademicSession academicSession =
                academicSessionRepository.findById(
                        enrollment.getAcademicSessionId()
                ).orElseThrow(() ->
                        new IllegalArgumentException("Academic session not found"));

        // 9. Find college
        College college = collegeRepository.findById(
                user.getCollegeId()
        ).orElseThrow(() ->
                new IllegalArgumentException("College not found"));

        // 10. Build profile response
        return new StudentProfileResponse(
                user.getFullName(),
                user.getEmail(),
                user.getMobile(),

                student.getRollNumber(),
                student.getRegistrationNumber(),

                department.getName(),
                department.getCode(),

                semester.getNumber(),
                semester.getName(),

                section.getName(),

                program.getName(),
                program.getCode(),

                academicSession.getName(),

                college.getName(),
                college.getCode(),
                college.getUniversity()
        );
    }
}