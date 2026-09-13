
package com.attendance.system.security;

import com.attendance.system.entity.User;
import com.attendance.system.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("roleService")
public class RoleService {

    private final UserRepository userRepository;

    public RoleService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isSuperAdmin(Authentication authentication) {

        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            return false;
        }

        try {
            UUID userId = UUID.fromString(jwt.getSubject());

            User user = userRepository.findById(userId).orElse(null);

            if (user == null) {
                return false;
            }

            if (!"active".equals(user.getStatus())) {
                return false;
            }

            return "super_admin".equals(user.getRole());

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isCollegeAdmin(Authentication authentication) {

        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            return false;
        }

        try {
            UUID userId = UUID.fromString(jwt.getSubject());

            User user = userRepository.findById(userId).orElse(null);

            if (user == null) {
                return false;
            }

            if (!"active".equals(user.getStatus())) {
                return false;
            }

            return "college_admin".equals(user.getRole());

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isTeacher(Authentication authentication) {

        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            return false;
        }

        try {
            UUID userId = UUID.fromString(jwt.getSubject());

            User user = userRepository.findById(userId).orElse(null);

            if (user == null) {
                return false;
            }

            if (!"active".equals(user.getStatus())) {
                return false;
            }

            return "teacher".equals(user.getRole());

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isStudent(Authentication authentication) {

        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            return false;
        }

        try {
            UUID userId = UUID.fromString(jwt.getSubject());

            User user = userRepository.findById(userId).orElse(null);

            if (user == null) {
                return false;
            }

            if (!"active".equals(user.getStatus())) {
                return false;
            }

            return "student".equals(user.getRole());

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // Check whether the logged-in user is allowed to access the requested user.
    public boolean canAccessUser(Authentication authentication, UUID targetUserId) {

        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            return false;
        }

        try {
            UUID currentUserId = UUID.fromString(jwt.getSubject());

            User currentUser =
                    userRepository.findById(currentUserId).orElse(null);

            if (currentUser == null) {
                return false;
            }

            if (!"active".equals(currentUser.getStatus())) {
                return false;
            }

            // Super Admin can access any user.
            if ("super_admin".equals(currentUser.getRole())) {
                return true;
            }

            // Every user can access their own user record.
            if (currentUserId.equals(targetUserId)) {
                return true;
            }

            // College Admin can access users belonging to the same college.
            if ("college_admin".equals(currentUser.getRole())) {

                User targetUser =
                        userRepository.findById(targetUserId).orElse(null);

                if (targetUser == null) {
                    return false;
                }

                return currentUser.getCollegeId() != null
                        && currentUser.getCollegeId().equals(targetUser.getCollegeId());
            }

            return false;

        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}