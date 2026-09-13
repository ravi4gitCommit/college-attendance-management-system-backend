package com.attendance.system.controller;

import com.attendance.system.entity.TimetableSlot;
import com.attendance.system.service.TimetableSlotService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/college-admin/timetable-slots")
@PreAuthorize("@roleService.isCollegeAdmin(authentication)")
public class CollegeAdminTimetableController {

    private final TimetableSlotService timetableSlotService;

    public CollegeAdminTimetableController(
            TimetableSlotService timetableSlotService
    ) {
        this.timetableSlotService = timetableSlotService;
    }

    @GetMapping
    public ResponseEntity<List<TimetableSlot>> getAllSlots(
            Authentication authentication
    ) {
        UUID adminUserId =
                UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                timetableSlotService.getAllSlotsForCollege(
                        adminUserId
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimetableSlot> getSlotById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        UUID adminUserId =
                UUID.fromString(authentication.getName());

        return timetableSlotService
                .getSlotByIdForCollege(
                        id,
                        adminUserId
                )
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    @PostMapping
    public ResponseEntity<TimetableSlot> createSlot(
            @RequestBody TimetableSlot slot,
            Authentication authentication
    ) {
        try {
            UUID adminUserId =
                    UUID.fromString(authentication.getName());

            TimetableSlot createdSlot =
                    timetableSlotService.createSlotForCollege(
                            slot,
                            adminUserId
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(createdSlot);

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TimetableSlot> updateSlot(
            @PathVariable Long id,
            @RequestBody TimetableSlot slot,
            Authentication authentication
    ) {
        try {
            UUID adminUserId =
                    UUID.fromString(authentication.getName());

            return timetableSlotService
                    .updateSlotForCollege(
                            id,
                            slot,
                            adminUserId
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
        } catch (DataIntegrityViolationException
                 | JpaSystemException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid timetable slot data"
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSlot(
            @PathVariable Long id,
            Authentication authentication
    ) {
        UUID adminUserId =
                UUID.fromString(authentication.getName());

        boolean deleted =
                timetableSlotService.deleteSlotForCollege(
                        id,
                        adminUserId
                );

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}