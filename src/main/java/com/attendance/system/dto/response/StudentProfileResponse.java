package com.attendance.system.dto.response;

public class StudentProfileResponse {

    private String fullName;
    private String email;
    private String mobile;

    private String rollNumber;
    private String registrationNumber;

    private String departmentName;
    private String departmentCode;

    private Short semesterNumber;
    private String semesterName;

    private String sectionName;

    private String programName;
    private String programCode;

    private String academicSessionName;

    private String collegeName;
    private String collegeCode;
    private String university;

    public StudentProfileResponse(
            String fullName,
            String email,
            String mobile,
            String rollNumber,
            String registrationNumber,
            String departmentName,
            String departmentCode,
            Short semesterNumber,
            String semesterName,
            String sectionName,
            String programName,
            String programCode,
            String academicSessionName,
            String collegeName,
            String collegeCode,
            String university) {

        this.fullName = fullName;
        this.email = email;
        this.mobile = mobile;
        this.rollNumber = rollNumber;
        this.registrationNumber = registrationNumber;
        this.departmentName = departmentName;
        this.departmentCode = departmentCode;
        this.semesterNumber = semesterNumber;
        this.semesterName = semesterName;
        this.sectionName = sectionName;
        this.programName = programName;
        this.programCode = programCode;
        this.academicSessionName = academicSessionName;
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

    public String getRollNumber() {
        return rollNumber;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
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

    public String getProgramName() {
        return programName;
    }

    public String getProgramCode() {
        return programCode;
    }

    public String getAcademicSessionName() {
        return academicSessionName;
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