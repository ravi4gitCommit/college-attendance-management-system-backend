package com.attendance.system.security;

import com.attendance.system.entity.User;
import com.attendance.system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoleServiceTest {

    @Test
    void isSuperAdmin_shouldReturnTrueForActiveSuperAdmin() {

        UUID userId = UUID.randomUUID();

        UserRepository userRepository = mock(UserRepository.class);
        Authentication authentication = mock(Authentication.class);

        Jwt jwt = Jwt.withTokenValue("test-token")
                .subject(userId.toString())
                .header("alg", "none")
                .build();

        when(authentication.getPrincipal()).thenReturn(jwt);

        User user = mock(User.class);
        when(user.getStatus()).thenReturn("active");
        when(user.getRole()).thenReturn("super_admin");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        RoleService roleService = new RoleService(userRepository);

        assertTrue(roleService.isSuperAdmin(authentication));
    }

    @Test
    void isSuperAdmin_shouldReturnFalseForInactiveSuperAdmin() {

        UUID userId = UUID.randomUUID();

        UserRepository userRepository = mock(UserRepository.class);
        Authentication authentication = mock(Authentication.class);

        Jwt jwt = Jwt.withTokenValue("test-token")
                .subject(userId.toString())
                .header("alg", "none")
                .build();

        when(authentication.getPrincipal()).thenReturn(jwt);

        User user = mock(User.class);
        when(user.getStatus()).thenReturn("inactive");
        when(user.getRole()).thenReturn("super_admin");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        RoleService roleService = new RoleService(userRepository);

        assertFalse(roleService.isSuperAdmin(authentication));
    }


    @Test
    void isSuperAdmin_shouldReturnFalseForWrongRole() {

        UUID userId = UUID.randomUUID();

        UserRepository userRepository = mock(UserRepository.class);
        Authentication authentication = mock(Authentication.class);

        Jwt jwt = Jwt.withTokenValue("test-token")
                .subject(userId.toString())
                .header("alg", "none")
                .build();

        when(authentication.getPrincipal()).thenReturn(jwt);

        User user = mock(User.class);
        when(user.getStatus()).thenReturn("active");
        when(user.getRole()).thenReturn("teacher");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        RoleService roleService = new RoleService(userRepository);

        assertFalse(roleService.isSuperAdmin(authentication));
    }


    @Test
    void isSuperAdmin_shouldReturnFalseWhenUserDoesNotExist() {

        UUID userId = UUID.randomUUID();

        UserRepository userRepository = mock(UserRepository.class);
        Authentication authentication = mock(Authentication.class);

        Jwt jwt = Jwt.withTokenValue("test-token")
                .subject(userId.toString())
                .header("alg", "none")
                .build();

        when(authentication.getPrincipal()).thenReturn(jwt);

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        RoleService roleService = new RoleService(userRepository);

        assertFalse(roleService.isSuperAdmin(authentication));
    }


    @Test
    void isSuperAdmin_shouldReturnFalseForNonJwtPrincipal() {

        UserRepository userRepository = mock(UserRepository.class);
        Authentication authentication = mock(Authentication.class);

        when(authentication.getPrincipal()).thenReturn("invalid-principal");

        RoleService roleService = new RoleService(userRepository);

        assertFalse(roleService.isSuperAdmin(authentication));
    }


}
