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
@RequestMapping("/api/v1/hod/timetable-slots")
@PreAuthorize("@roleService.isHOD(authentication)")
public class HodTimetableController {

    private final TimetableSlotService timetableSlotService;

    public HodTimetableController(
            TimetableSlotService timetableSlotService
    ) {
        this.timetableSlotService = timetableSlotService;
    }

    @GetMapping
    public ResponseEntity<List<TimetableSlot>> getAllSlots(
            Authentication authentication
    ) {
        UUID hodUserId =
                UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                timetableSlotService.getAllSlotsForHod(hodUserId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimetableSlot> getSlotById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        UUID hodUserId =
                UUID.fromString(authentication.getName());

        return timetableSlotService
                .getSlotByIdForHod(id, hodUserId)
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
            UUID hodUserId =
                    UUID.fromString(authentication.getName());

            TimetableSlot createdSlot =
                    timetableSlotService.createSlotForHod(
                            slot,
                            hodUserId
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
            UUID hodUserId =
                    UUID.fromString(authentication.getName());

            return timetableSlotService
                    .updateSlotForHod(
                            id,
                            slot,
                            hodUserId
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
        UUID hodUserId =
                UUID.fromString(authentication.getName());

        boolean deleted =
                timetableSlotService.deleteSlotForHod(
                        id,
                        hodUserId
                );

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
