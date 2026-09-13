package com.attendance.system.controller;

import com.attendance.system.entity.College;
import com.attendance.system.entity.CollegeStatus;
import com.attendance.system.service.CollegeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import org.springframework.web.server.ResponseStatusException;
@RestController
@RequestMapping("/api/v1/admin/colleges")
@PreAuthorize("@roleService.isSuperAdmin(authentication)")
public class CollegeController {

    private final CollegeService collegeService;

    public CollegeController(CollegeService collegeService) {
        this.collegeService = collegeService;
    }

    // GET /api/v1/admin/colleges
    @GetMapping
    public ResponseEntity<List<College>> getAllColleges() {
        return ResponseEntity.ok(collegeService.getAllColleges());
    }

    // GET /api/v1/admin/colleges/{id}
    @GetMapping("/{id}")
    public ResponseEntity<College> getCollegeById(@PathVariable Long id) {

        return collegeService.getCollegeById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST /api/v1/admin/colleges
    // POST /api/v1/admin/colleges
    @PostMapping
    public ResponseEntity<College> createCollege(
            @RequestBody College college
    ) {
        try {
            College createdCollege = collegeService.createCollege(college);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(createdCollege);

        } catch (IllegalStateException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    e.getMessage()
            );
        }
    }

    // PUT /api/v1/admin/colleges/{id}
    @PutMapping("/{id}")
    public ResponseEntity<College> updateCollege(
            @PathVariable Long id,
            @RequestBody College college
    ) {

        try {
            return collegeService.updateCollege(id, college)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());

        } catch (IllegalStateException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    e.getMessage()
            );
        }
    }

    // PATCH /api/v1/admin/colleges/{id}/status
    @PatchMapping("/{id}/status")
    public ResponseEntity<College> updateStatus(
            @PathVariable Long id,
            @RequestParam CollegeStatus status
    ) {

        return collegeService.updateStatus(id, status)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}