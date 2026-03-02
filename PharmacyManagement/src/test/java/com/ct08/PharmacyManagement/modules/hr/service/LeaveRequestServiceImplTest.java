package com.ct08.PharmacyManagement.modules.hr.service;

import com.ct08.PharmacyManagement.common.exception.BadRequestException;
import com.ct08.PharmacyManagement.common.exception.ConflictException;
import com.ct08.PharmacyManagement.modules.auth.entity.Roles;
import com.ct08.PharmacyManagement.modules.auth.entity.Users;
import com.ct08.PharmacyManagement.modules.auth.repository.UsersRepository;
import com.ct08.PharmacyManagement.modules.hr.dto.LeaveRequestCreationDTO;
import com.ct08.PharmacyManagement.modules.hr.entity.Employees;
import com.ct08.PharmacyManagement.modules.hr.entity.LeaveRequests;
import com.ct08.PharmacyManagement.modules.hr.repository.LeaveRequestsRepository;
import com.ct08.PharmacyManagement.modules.hr.service.impl.LeaveRequestServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LeaveRequestServiceImplTest {

    @Mock
    private LeaveRequestsRepository leaveRequestsRepository;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private LeaveRequestServiceImpl leaveRequestService;

    private Users currentUser;
    private Employees currentEmployee;

    @BeforeEach
    void setUp() {
        currentEmployee = new Employees();
        currentEmployee.setId(1);

        currentUser = new Users();
        currentUser.setId(1);
        currentUser.setUsername("testuser");
        currentUser.setEmployee(currentEmployee);

        Roles roleSS = new Roles(1, "ROLE_SS");
        currentUser.setRoles(Set.of(roleSS));
    }

    private void mockSecurityContext() {
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("testuser");
        lenient().when(usersRepository.findByUsername("testuser")).thenReturn(Optional.of(currentUser));
    }

    @Test
    void createLeaveRequest_Success() {
        mockSecurityContext();

        LeaveRequestCreationDTO request = new LeaveRequestCreationDTO();
        request.setLeaveType("Annual Leave");
        request.setStartDate(LocalDateTime.now().plusDays(2));
        request.setEndDate(LocalDateTime.now().plusDays(3));
        request.setReason("Vacation");

        when(leaveRequestsRepository.findByEmployeeIdAndStatusIn(anyInt(), anyList()))
                .thenReturn(Collections.emptyList());

        leaveRequestService.createLeaveRequest(request);

        verify(leaveRequestsRepository, times(1)).save(any(LeaveRequests.class));
    }

    @Test
    void createLeaveRequest_SuccessForAdminAndAutoApprovedWithSalary() {
        mockSecurityContext();

        Roles roleAdmin = new Roles(1, "ROLE_ADMIN");
        currentUser.setRoles(Set.of(roleAdmin)); // User is now an ADMIN

        LeaveRequestCreationDTO request = new LeaveRequestCreationDTO();
        request.setLeaveType("Annual Leave");
        request.setStartDate(LocalDateTime.now().plusDays(2));
        request.setEndDate(LocalDateTime.now().plusDays(3));
        request.setReason("Admin Vacation");
        request.setIsPaidLeave(true); // Admin selected Paid Leave

        when(leaveRequestsRepository.findByEmployeeIdAndStatusIn(anyInt(), anyList()))
                .thenReturn(Collections.emptyList());

        leaveRequestService.createLeaveRequest(request);

        verify(leaveRequestsRepository, times(1))
                .save(argThat(
                        leaveRequest -> leaveRequest.getStatus() == LeaveRequests.ApprovalStatus.Approved_Salary &&
                                leaveRequest.getApprovedBy().getId().equals(currentUser.getId())));
    }

    @Test
    void createLeaveRequest_SuccessForAdminAndAutoApprovedWithoutSalary() {
        mockSecurityContext();

        Roles roleAdmin = new Roles(1, "ROLE_ADMIN");
        currentUser.setRoles(Set.of(roleAdmin)); // User is now an ADMIN

        LeaveRequestCreationDTO request = new LeaveRequestCreationDTO();
        request.setLeaveType("Annual Leave");
        request.setStartDate(LocalDateTime.now().plusDays(2));
        request.setEndDate(LocalDateTime.now().plusDays(3));
        request.setReason("Admin Vacation");
        request.setIsPaidLeave(false); // Admin selected Unpaid Leave

        when(leaveRequestsRepository.findByEmployeeIdAndStatusIn(anyInt(), anyList()))
                .thenReturn(Collections.emptyList());

        leaveRequestService.createLeaveRequest(request);

        verify(leaveRequestsRepository, times(1))
                .save(argThat(leaveRequest -> leaveRequest.getStatus() == LeaveRequests.ApprovalStatus.Approved &&
                        leaveRequest.getApprovedBy().getId().equals(currentUser.getId())));
    }

    @Test
    void createLeaveRequest_FailsWhenStartDateIsTodayOrPast() {
        mockSecurityContext();

        LeaveRequestCreationDTO request = new LeaveRequestCreationDTO();
        request.setLeaveType("Sick Leave");
        request.setStartDate(LocalDateTime.now().minusDays(1)); // Past date
        request.setEndDate(LocalDateTime.now().plusDays(1));

        assertThrows(BadRequestException.class, () -> leaveRequestService.createLeaveRequest(request));
    }

    @Test
    void createLeaveRequest_FailsWhenOverlap() {
        mockSecurityContext();

        LocalDateTime newStart = LocalDateTime.now().plusDays(5);
        LocalDateTime newEnd = LocalDateTime.now().plusDays(10);

        LeaveRequestCreationDTO request = new LeaveRequestCreationDTO();
        request.setLeaveType("Annual Leave");
        request.setStartDate(newStart);
        request.setEndDate(newEnd);

        // Mock existing request that overlaps (e.g., from day 4 to day 8)
        LeaveRequests existingRequest = new LeaveRequests();
        existingRequest.setId(99);
        existingRequest.setStartDate(LocalDateTime.now().plusDays(4));
        existingRequest.setEndDate(LocalDateTime.now().plusDays(8));

        when(leaveRequestsRepository.findByEmployeeIdAndStatusIn(anyInt(), anyList()))
                .thenReturn(List.of(existingRequest));

        assertThrows(ConflictException.class, () -> leaveRequestService.createLeaveRequest(request));
    }
}
