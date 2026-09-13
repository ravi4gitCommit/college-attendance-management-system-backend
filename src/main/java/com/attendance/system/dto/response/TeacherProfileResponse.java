package com.attendance.system.dto.response;

public class TeacherProfileResponse {

    private String fullName;
    private String email;
    private String mobile;

    private String employeeId;

    private String departmentName;
    private String departmentCode;

    private String collegeName;
    private String collegeCode;
    private String university;

    public TeacherProfileResponse(
            String fullName,
            String email,
            String mobile,
            String employeeId,
            String departmentName,
            String departmentCode,
            String collegeName,
            String collegeCode,
            String university
    ) {
        this.fullName = fullName;
        this.email = email;
        this.mobile = mobile;
        this.employeeId = employeeId;
        this.departmentName = departmentName;
        this.departmentCode = departmentCode;
        this.collegeName = collegeName;
        this.collegeCode = collegeCode;
        this.university = university;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public String getCollegeCode() {
        return collegeCode;
    }

    public String getUniversity() {
        return university;
    }
}