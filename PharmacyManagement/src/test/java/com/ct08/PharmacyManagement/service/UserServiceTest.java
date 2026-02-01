package com.ct08.PharmacyManagement.service;

import com.ct08.PharmacyManagement.entity.Roles;
import com.ct08.PharmacyManagement.entity.Users;
import com.ct08.PharmacyManagement.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    private void mockCurrentUserRoles(String... roles) {
        // SimpleGrantedAuthority vs String authorities - The service uses .getAuthority() which returns String.
        // But getAuthorities returns collection of GrantedAuthority.
        // My service implementation:
        // authentication.getAuthorities().stream().map(ga -> ga.getAuthority())...
        
        List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        
        // Need to use doReturn because of generics
        doReturn(authorities).when(authentication).getAuthorities();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
    }

    private Users createTargetUser(int id, String... roleNames) {
        Users user = new Users();
        user.setId(id);
        user.setIsActive(true);
        Set<Roles> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Roles r = new Roles();
            r.setRoleName(roleName);
            roles.add(r);
        }
        user.setRoles(roles);
        return user;
    }

    @Test
    void testManagerCanLockAnyone() {
        mockCurrentUserRoles("ROLE_MANAGER");
        Users targetUser = createTargetUser(2, "ROLE_MANAGER"); // Manager locking manager
        when(usersRepository.findById(2)).thenReturn(Optional.of(targetUser));

        userService.lockAccount(2);

        verify(usersRepository, times(1)).save(targetUser);
        assert(!targetUser.getIsActive());
    }

    @Test
    void testHRCanLockNormalUser() {
        mockCurrentUserRoles("ROLE_HR");
        Users targetUser = createTargetUser(3, "ROLE_EMPLOYEE");
        when(usersRepository.findById(3)).thenReturn(Optional.of(targetUser));

        userService.lockAccount(3);

        verify(usersRepository, times(1)).save(targetUser);
        assert(!targetUser.getIsActive());
    }

    @Test
    void testHRCannotLockManager() {
        mockCurrentUserRoles("ROLE_HR");
        Users targetUser = createTargetUser(4, "ROLE_MANAGER");
        when(usersRepository.findById(4)).thenReturn(Optional.of(targetUser));

        assertThrows(RuntimeException.class, () -> userService.lockAccount(4));
        verify(usersRepository, never()).save(targetUser);
    }

    @Test
    void testHRCannotLockWM() {
        mockCurrentUserRoles("ROLE_HR");
        Users targetUser = createTargetUser(5, "ROLE_WM");
        when(usersRepository.findById(5)).thenReturn(Optional.of(targetUser));

        assertThrows(RuntimeException.class, () -> userService.lockAccount(5));
        verify(usersRepository, never()).save(targetUser);
    }

    @Test
    void testOtherRoleCannotLock() {
        mockCurrentUserRoles("ROLE_EMPLOYEE");
        Users targetUser = createTargetUser(6, "ROLE_EMPLOYEE");
        when(usersRepository.findById(6)).thenReturn(Optional.of(targetUser));

        assertThrows(RuntimeException.class, () -> userService.lockAccount(6));
        verify(usersRepository, never()).save(targetUser);
    }
}
