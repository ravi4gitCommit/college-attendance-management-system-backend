package com.attendance.system.controller;

import com.attendance.system.entity.Semester;
import com.attendance.system.service.SemesterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/college-admin/semesters")
@PreAuthorize("@roleService.isCollegeAdmin(authentication)")
public class SemesterController {

    private final SemesterService semesterService;

    public SemesterController(SemesterService semesterService) {
        this.semesterService = semesterService;
    }

    @GetMapping
    public ResponseEntity<List<Semester>> getAllSemesters() {
        return ResponseEntity.ok(
                semesterService.getAllSemesters()
        );
    }
}
