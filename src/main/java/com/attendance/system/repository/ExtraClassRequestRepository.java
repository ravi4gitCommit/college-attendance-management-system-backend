package com.attendance.system.repository;

import com.attendance.system.entity.ExtraClassRequest;
import com.attendance.system.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExtraClassRequestRepository
        extends JpaRepository<ExtraClassRequest, Long> {

    List<ExtraClassRequest> findByRequestedBy(UUID requestedBy);

    List<ExtraClassRequest> findByStatus(RequestStatus status);

    List<ExtraClassRequest> findByTeacherAssignmentId(
            Long teacherAssignmentId
    );
}