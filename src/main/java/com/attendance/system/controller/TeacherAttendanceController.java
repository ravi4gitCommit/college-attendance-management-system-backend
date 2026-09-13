package com.attendance.system.controller;

import com.attendance.system.entity.AttendanceRecord;
import com.attendance.system.entity.AttendanceStatus;
import com.attendance.system.service.AttendanceRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import com.attendance.system.dto.response.TeacherClassStudentResponse;
import java.util.UUID;
import java.util.List;
import com.attendance.system.dto.request.AttendanceSubmitRequest;
@RestController
@RequestMapping("/api/v1/teacher/classes")
@PreAuthorize("@roleService.isTeacher(authentication)")
public class TeacherAttendanceController {

    private final AttendanceRecordService attendanceRecordService;

    public TeacherAttendanceController(
            AttendanceRecordService attendanceRecordService
    ) {
        this.attendanceRecordService = attendanceRecordService;
    }


    @PostMapping("/{classSessionId}/attendance/submit")
    public ResponseEntity<java.util.List<AttendanceRecord>> submitAttendance(
            @PathVariable Long classSessionId,
            @RequestBody AttendanceSubmitRequest request,
            Authentication authentication
    ) {

        UUID submittedBy =
                UUID.fromString(authentication.getName());

        try {

            java.util.List<AttendanceRecord> records =
                    attendanceRecordService.submitAttendance(
                            classSessionId,
                            request,
                            submittedBy
                    );

            return ResponseEntity.ok(records);

        } catch (org.springframework.security.access.AccessDeniedException e) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );

        } catch (IllegalStateException e) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    e.getMessage()
            );
        }
    }
    @GetMapping("/{classSessionId}/attendance")
    public ResponseEntity<List<TeacherClassStudentResponse>> getStudentsByClassSession(
            @PathVariable Long classSessionId,
            Authentication authentication
    ) {

        UUID userId =
                UUID.fromString(authentication.getName());

        try {

            List<TeacherClassStudentResponse> students =
                    attendanceRecordService.getStudentsByClassSession(
                            classSessionId,
                            userId
                    );

            return ResponseEntity.ok(students);

        } catch (org.springframework.security.access.AccessDeniedException e) {

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