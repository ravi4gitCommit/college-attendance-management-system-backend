package com.attendance.system.service;

import com.attendance.system.entity.Department;
import com.attendance.system.entity.RecordStatus;
import com.attendance.system.entity.Student;
import com.attendance.system.entity.User;
import com.attendance.system.repository.DepartmentRepository;
import com.attendance.system.repository.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.attendance.system.dto.response.StudentListResponse;
import com.attendance.system.entity.StudentEnrollment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final UserService userService;

    public StudentService(
            StudentRepository studentRepository,
            DepartmentRepository departmentRepository,
            UserService userService
    ) {
        this.studentRepository = studentRepository;
        this.departmentRepository = departmentRepository;
        this.userService = userService;
    }

    // Get all students belonging to the logged-in College Admin's college
    public List<Student> getAllStudents(UUID adminUserId) {

        Long collegeId = getAdminCollegeId(adminUserId);

        return studentRepository.findAll()
                .stream()
                .filter(student -> isStudentInCollege(student, collegeId))
                .toList();
    }

    // Get complete student information for the College Admin Students page.
    public List<StudentListResponse> getAllStudentDetails(UUID adminUserId) {

        Long collegeId = getAdminCollegeId(adminUserId);

        return studentRepository.findAll()
                .stream()
                .filter(student -> isStudentInCollege(student, collegeId))
                .map(student -> {

                    User user = userService.findById(student.getUserId())
                            .orElseThrow(() ->
                                    new IllegalArgumentException("User not found"));

                    Department department = departmentRepository.findById(
                            student.getDepartmentId()
                    ).orElseThrow(() ->
                            new IllegalArgumentException("Department not found"));

                    return new StudentListResponse(
                            student.getId(),
                            user.getFullName(),
                            user.getEmail(),
                            user.getMobile(),
                            student.getRollNumber(),
                            student.getRegistrationNumber(),
                            department.getId(),
                            department.getName(),
                            department.getCode(),
                            null,
                            null,
                            null,
                            null,
                            student.getStatus().name()
                    );
                })
                .toList();
    }

    // Get one student only if the student belongs to the admin's college
    public Optional<Student> getStudentById(
            Long id,
            UUID adminUserId
    ) {

        Long collegeId = getAdminCollegeId(adminUserId);

        return studentRepository.findById(id)
                .filter(student -> isStudentInCollege(student, collegeId));
    }

    public Optional<Student> getStudentByUserId(UUID userId) {
        return studentRepository.findByUserId(userId);
    }

    // Get students of a department only if that department belongs to admin's college
    public List<Student> getStudentsByDepartment(
            Long departmentId,
            UUID adminUserId
    ) {

        Long collegeId = getAdminCollegeId(adminUserId);

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Department not found")
                );

        if (!collegeId.equals(department.getCollegeId())) {
            throw new AccessDeniedException(
                    "Department does not belong to your college"
            );
        }

        return studentRepository.findByDepartmentId(departmentId);
    }

    public Student createStudent(
            Student student,
            UUID adminUserId
    ) {

        Long collegeId = getAdminCollegeId(adminUserId);

        // Verify that the selected department belongs to the admin's college.
        Department department = departmentRepository.findById(
                student.getDepartmentId()
        ).orElseThrow(() ->
                new IllegalArgumentException("Department not found")
        );

        if (!collegeId.equals(department.getCollegeId())) {
            throw new AccessDeniedException(
                    "Department does not belong to your college"
            );
        }

        // Verify that the linked public.users record belongs
        // to the same college as the logged-in College Admin.
        User user = userService.findById(student.getUserId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );

        if (!collegeId.equals(user.getCollegeId())) {
            throw new AccessDeniedException(
                    "User does not belong to your college"
            );
        }

        // Verify that the linked user is an active student.
        if (!"student".equals(user.getRole())) {
            throw new IllegalArgumentException(
                    "User role must be student"
            );
        }

        if (!"active".equals(user.getStatus())) {
            throw new IllegalArgumentException(
                    "User is not active"
            );
        }

        // Duplicate user check
        if (studentRepository.existsByUserId(student.getUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User is already registered as a student"
            );
        }

        // Duplicate roll number check
        if (studentRepository.existsByRollNumberIgnoreCase(
                student.getRollNumber())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Roll number already exists"
            );
        }

        // Duplicate registration number check
        if (student.getRegistrationNumber() != null
                && !student.getRegistrationNumber().isBlank()
                && studentRepository.existsByRegistrationNumberIgnoreCase(
                student.getRegistrationNumber())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Registration number already exists"
            );
        }

        if (student.getStatus() == null) {
            student.setStatus(RecordStatus.active);
        }

        return studentRepository.save(student);
    }


    // Update student only if the existing student belongs to admin's college
    public Optional<Student> updateStudent(
            Long id,
            Student updatedStudent,
            UUID adminUserId
    ) {

        Long collegeId = getAdminCollegeId(adminUserId);

        return studentRepository.findById(id)
                .filter(existingStudent ->
                        isStudentInCollege(existingStudent, collegeId))
                .map(existingStudent -> {

                    // Verify that the new department belongs to
                    // the logged-in College Admin's college.
                    Department department = departmentRepository.findById(
                            updatedStudent.getDepartmentId()
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

                    // Check duplicate roll number.
                    // The current student's own record is excluded,
                    // so keeping the same roll number is allowed.
                    if (studentRepository.existsByRollNumberIgnoreCaseAndIdNot(
                            updatedStudent.getRollNumber(),
                            id
                    )) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Roll number already exists"
                        );
                    }

                    // Check duplicate registration number only when
                    // a registration number has been provided.
                    if (updatedStudent.getRegistrationNumber() != null
                            && !updatedStudent.getRegistrationNumber().isBlank()
                            && studentRepository
                            .existsByRegistrationNumberIgnoreCaseAndIdNot(
                                    updatedStudent.getRegistrationNumber(),
                                    id
                            )) {

                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Registration number already exists"
                        );
                    }

                    // Update only editable student fields.
                    // userId is intentionally NOT changed.
                    existingStudent.setDepartmentId(
                            updatedStudent.getDepartmentId()
                    );

                    existingStudent.setRollNumber(
                            updatedStudent.getRollNumber()
                    );

                    existingStudent.setRegistrationNumber(
                            updatedStudent.getRegistrationNumber()
                    );

                    if (updatedStudent.getStatus() != null) {
                        existingStudent.setStatus(
                                updatedStudent.getStatus()
                        );
                    }

                    return studentRepository.save(existingStudent);
                });
    }


    // Delete student only if the student belongs to admin's college
    public boolean deleteStudent(
            Long id,
            UUID adminUserId
    ) {

        Long collegeId = getAdminCollegeId(adminUserId);

        Optional<Student> student =
                studentRepository.findById(id);

        if (student.isEmpty()) {
            return false;
        }

        if (!isStudentInCollege(student.get(), collegeId)) {
            return false;
        }

        studentRepository.deleteById(id);
        return true;
    }

    // Get the college ID of the logged-in College Admin
    private Long getAdminCollegeId(UUID adminUserId) {

        return userService.findById(adminUserId)
                .map(User::getCollegeId)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Admin user not found"
                        )
                );
    }

    // Check whether the student's department belongs to the given college
    private boolean isStudentInCollege(
            Student student,
            Long collegeId
    ) {

        return departmentRepository.findById(
                        student.getDepartmentId()
                )
                .map(department ->
                        collegeId.equals(department.getCollegeId()))
                .orElse(false);
    }
}
