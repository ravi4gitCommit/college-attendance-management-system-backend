package com.attendance.system.controller;

import com.attendance.system.entity.ClassSession;
import com.attendance.system.entity.ClassSessionStatus;
import com.attendance.system.service.ClassSessionService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/college-admin/class-sessions")
public class ClassSessionController {

    private final ClassSessionService classSessionService;

    public ClassSessionController(
            ClassSessionService classSessionService
    ) {
        this.classSessionService = classSessionService;
    }


    // Get all class sessions
    @PreAuthorize("@roleService.isCollegeAdmin(authentication)")
    @GetMapping
    public ResponseEntity<List<ClassSession>> getAllSessions(
            Authentication authentication
    ) {

        UUID adminUserId =
                UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                classSessionService
                        .getAllSessionsForCollege(adminUserId)
        );
    }

    // Get class session by ID
    @PreAuthorize("@roleService.isCollegeAdmin(authentication)")
    @GetMapping("/{id}")
    public ResponseEntity<ClassSession> getSessionById(
            @PathVariable Long id,
            Authentication authentication
    ) {

        UUID adminUserId =
                UUID.fromString(authentication.getName());

        return classSessionService
                .getSessionByIdForCollege(
                        id,
                        adminUserId
                )
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    // Get sessions by teacher assignment
    @PreAuthorize("@roleService.isCollegeAdmin(authentication)")
    @GetMapping("/assignment/{teacherAssignmentId}")
    public ResponseEntity<List<ClassSession>>
    getSessionsByTeacherAssignment(
            @PathVariable Long teacherAssignmentId,
            Authentication authentication
    ) {

        UUID adminUserId =
                UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                classSessionService
                        .getSessionsByTeacherAssignmentForCollege(
                                teacherAssignmentId,
                                adminUserId
                        )
        );
    }

    // Get sessions by date
    @PreAuthorize("@roleService.isCollegeAdmin(authentication)")
    @GetMapping("/date/{date}")
    public ResponseEntity<List<ClassSession>>
    getSessionsByDate(
            @PathVariable LocalDate date,
            Authentication authentication
    ) {

        UUID adminUserId =
                UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                classSessionService
                        .getSessionsByDateForCollege(
                                date,
                                adminUserId
                        )
        );
    }

    // Get sessions by status
    @PreAuthorize("@roleService.isCollegeAdmin(authentication)")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ClassSession>>
    getSessionsByStatus(
            @PathVariable ClassSessionStatus status,
            Authentication authentication
    ) {

        UUID adminUserId =
                UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                classSessionService
                        .getSessionsByStatusForCollege(
                                status,
                                adminUserId
                        )
        );
    }


    // Create class session
    @PreAuthorize("@roleService.isCollegeAdmin(authentication)")
    @PostMapping
    public ResponseEntity<ClassSession> createSession(
            @RequestBody ClassSession session,
            Authentication authentication
    ) {

        try {

            ClassSession createdSession =
                    classSessionService.createSessionForCollege(
                            session,
                            UUID.fromString(authentication.getName())
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(createdSession);

        } catch (IllegalArgumentException e) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );

        } catch (IllegalStateException e) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }
    }

    // Update class session
    @PreAuthorize("@roleService.isCollegeAdmin(authentication)")
    @PutMapping("/{id}")
    public ResponseEntity<ClassSession> updateSession(
            @PathVariable Long id,
            @RequestBody ClassSession session,
            Authentication authentication
    ){

        try {

            return classSessionService
                    .updateSessionForCollege(
                            id,
                            session,
                            UUID.fromString(authentication.getName())
                    )
                    .map(ResponseEntity::ok)
                    .orElseGet(() ->
                            ResponseEntity.notFound().build()
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

        } catch (DataIntegrityViolationException | JpaSystemException e) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid class session source: timetable slot or extra class request does not match the teacher assignment"
            );
        }
    }

    // Delete class session
    @PreAuthorize("@roleService.isCollegeAdmin(authentication)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(
            @PathVariable Long id,
            Authentication authentication
    ) {

        try {

            boolean deleted =
                    classSessionService.deleteSessionForCollege(
                            id,
                            UUID.fromString(authentication.getName())
                    );

            if (!deleted) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.noContent().build();

        } catch (IllegalStateException e) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    e.getMessage()
            );
        }
    }

    // Start class session
    @PreAuthorize("@roleService.isTeacher(authentication)")
    @PostMapping("/{id}/start")
    public ResponseEntity<ClassSession> startSession(
            @PathVariable Long id,
            Authentication authentication
    ) {

        try {

            UUID teacherUserId =
                    UUID.fromString(authentication.getName());

            ClassSession startedSession =
                    classSessionService.startSession(
                            id,
                            teacherUserId
                    );

            return ResponseEntity.ok(startedSession);

        } catch (IllegalArgumentException e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    e.getMessage()
            );

        } catch (IllegalStateException e) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    e.getMessage()
            );
        }
    }

    // Submit class session
    @PreAuthorize("@roleService.isTeacher(authentication)")
    @PostMapping("/{id}/submit")
    public ResponseEntity<ClassSession> submitSession(
            @PathVariable Long id,
            Authentication authentication
    ) {

        try {

            UUID teacherUserId =
                    UUID.fromString(authentication.getName());

            ClassSession submittedSession =
                    classSessionService.submitSession(
                            id,
                            teacherUserId
                    );

            return ResponseEntity.ok(submittedSession);

        } catch (IllegalArgumentException e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    e.getMessage()
            );

        } catch (IllegalStateException e) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    e.getMessage()
            );
        }
    }
}
