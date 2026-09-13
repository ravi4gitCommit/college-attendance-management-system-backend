package com.attendance.system.controller;

import org.springframework.web.server.ResponseStatusException;
import com.attendance.system.entity.AttendanceRecord;
import com.attendance.system.service.AttendanceRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/v1/college-admin/attendance")
@PreAuthorize("@roleService.isCollegeAdmin(authentication)")
public class AttendanceRecordController {

    private final AttendanceRecordService attendanceRecordService;

    public AttendanceRecordController(
            AttendanceRecordService attendanceRecordService) {
        this.attendanceRecordService = attendanceRecordService;
    }

    @GetMapping
    public ResponseEntity<List<AttendanceRecord>> getAllRecords(
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                attendanceRecordService.getAllRecords(userId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttendanceRecord> getRecordById(
            @PathVariable Long id,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        try {
            return attendanceRecordService
                    .getRecordById(id, userId)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());

        } catch (AccessDeniedException e) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    e.getMessage()
            );

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }
    }

    @GetMapping("/session/{classSessionId}")
    public ResponseEntity<List<AttendanceRecord>> getByClassSession(
            @PathVariable Long classSessionId,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        try {
            return ResponseEntity.ok(
                    attendanceRecordService
                            .getRecordsByClassSession(classSessionId, userId)
            );

        } catch (AccessDeniedException e) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    e.getMessage()
            );

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<AttendanceRecord>> getByStudent(
            @PathVariable Long studentId,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        try {
            return ResponseEntity.ok(
                    attendanceRecordService
                            .getRecordsByStudent(studentId, userId)
            );

        } catch (AccessDeniedException e) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    e.getMessage()
            );

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }
    }
}