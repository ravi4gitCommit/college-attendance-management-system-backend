package com.attendance.system.dto.response;

public class StudentListResponse {

    private Long id;
    private String fullName;
    private String email;
    private String mobile;

    private String rollNumber;
    private String registrationNumber;

    private Long departmentId;
    private String departmentName;
    private String departmentCode;

    private Short semesterNumber;
    private String semesterName;

    private String sectionName;
    private String academicSessionName;

    private String status;

    public StudentListResponse(
            Long id,
            String fullName,
            String email,
            String mobile,
            String rollNumber,
            String registrationNumber,
            Long departmentId,
            String departmentName,
            String departmentCode,
            Short semesterNumber,
            String semesterName,
            String sectionName,
            String academicSessionName,
            String status) {

        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.mobile = mobile;
        this.rollNumber = rollNumber;
        this.registrationNumber = registrationNumber;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.departmentCode = departmentCode;
        this.semesterNumber = semesterNumber;
        this.semesterName = semesterName;
        this.sectionName = sectionName;
        this.academicSessionName = academicSessionName;
        this.status = status;
    }

    public Long getId() {
        return id;
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

    public String getRollNumber() {
        return rollNumber;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public Short getSemesterNumber() {
        return semesterNumber;
    }

    public String getSemesterName() {
        return semesterName;
    }

    public String getSectionName() {
        return sectionName;
    }

    public String getAcademicSessionName() {
        return academicSessionName;
    }

    public String getStatus() {
        return status;
    }
}
