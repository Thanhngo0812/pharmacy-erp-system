package com.ct08.PharmacyManagement.service;

import com.ct08.PharmacyManagement.common.event.PasswordEmailEvent;
import com.ct08.PharmacyManagement.common.exception.ConflictException;
import com.ct08.PharmacyManagement.common.exception.ResourceNotFoundException;
import com.ct08.PharmacyManagement.common.infra.message.MessageProducerService;
import com.ct08.PharmacyManagement.modules.auth.entity.Users;
import com.ct08.PharmacyManagement.modules.auth.repository.UsersRepository;
import com.ct08.PharmacyManagement.modules.hr.dto.ApprovalRequest;
import com.ct08.PharmacyManagement.modules.hr.dto.HiredCareerChangeResponse;
import com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges;
import com.ct08.PharmacyManagement.modules.hr.entity.Employees;
import com.ct08.PharmacyManagement.modules.hr.repository.CareerChangesRepository;
import com.ct08.PharmacyManagement.modules.hr.repository.EmployeesRepository;
import com.ct08.PharmacyManagement.modules.hr.service.CareerChangesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CareerChangesServiceTest {

        @Mock
        private CareerChangesRepository careerChangesRepository;

        @Mock
        private EmployeesRepository employeesRepository;

        @Mock
        private UsersRepository usersRepository;

        @Mock
        private MessageProducerService messageProducerService;

        @Mock
        private PasswordEncoder passwordEncoder;

        @Mock
        private Authentication authentication;

        @InjectMocks
        private CareerChangesService careerChangesService;

        private Users adminUser;
        private Employees employee;
        private Users targetUser;
        private CareerChanges hiredChange;

        @BeforeEach
        void setUp() {
                adminUser = new Users();
                adminUser.setId(1);
                adminUser.setUsername("admin");

                employee = new Employees();
                employee.setId(2);
                employee.setFirstName("Khoa");
                employee.setLastName("Ngo");
                employee.setEmail("khoa@example.com");
                employee.setStatus(Employees.EmployeeStatus.Waiting);

                targetUser = new Users();
                targetUser.setId(2);
                targetUser.setEmployee(employee);
                targetUser.setIsActive(false);

                hiredChange = new CareerChanges();
                hiredChange.setId(10);
                hiredChange.setEmployee(employee);
                hiredChange.setChangeType(CareerChanges.ChangeType.Hired);
                hiredChange.setStatus(CareerChanges.ApprovalStatus.Pending);
        }

        @Test
        void getHiredCareerChanges_AsAdmin_Success() {
                doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication)
                                .getAuthorities();
                when(authentication.isAuthenticated()).thenReturn(true);
                when(careerChangesRepository.findAll(any(Specification.class), any(Sort.class)))
                                .thenReturn(Arrays.asList(hiredChange));

                List<HiredCareerChangeResponse> responses = careerChangesService.getHiredCareerChanges("id", "desc",
                                "Pending", null, null, null, authentication);

                assertEquals(1, responses.size());
                assertEquals(10, responses.get(0).getId());
        }

        @Test
        void getHiredCareerChanges_AsStandardUser_ThrowsAccessDenied() {
                doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_WH"))).when(authentication)
                                .getAuthorities();
                when(authentication.isAuthenticated()).thenReturn(true);

                assertThrows(AccessDeniedException.class,
                                () -> careerChangesService.getHiredCareerChanges("id", "asc", null, null, null, null,
                                                authentication));
        }

        @Test
        void approveHiredCareerChange_AsAdmin_Success() {
                doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication)
                                .getAuthorities();
                when(authentication.isAuthenticated()).thenReturn(true);
                when(authentication.getName()).thenReturn("admin");

                when(usersRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
                when(careerChangesRepository.findById(10)).thenReturn(Optional.of(hiredChange));
                when(usersRepository.findByEmployeeId(employee.getId())).thenReturn(Optional.of(targetUser));
                when(passwordEncoder.encode(anyString())).thenReturn("hashed-pwd");

                ApprovalRequest req = new ApprovalRequest();
                req.setIsApproved(true);

                careerChangesService.approveOrRejectHiredCareerChange(10, req, authentication);

                assertEquals(CareerChanges.ApprovalStatus.Approved, hiredChange.getStatus());
                assertEquals(Employees.EmployeeStatus.Active, employee.getStatus());
                assertTrue(targetUser.getIsActive());
                assertEquals("sending", targetUser.getMailStatus());

                verify(careerChangesRepository).save(hiredChange);
                verify(employeesRepository).save(employee);
                verify(usersRepository).save(targetUser);
                verify(messageProducerService).sendMessage(eq("user-password-email"), any(PasswordEmailEvent.class));
        }

        @Test
        void rejectHiredCareerChange_AsAdmin_Success() {
                doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication)
                                .getAuthorities();
                when(authentication.isAuthenticated()).thenReturn(true);
                when(authentication.getName()).thenReturn("admin");

                when(usersRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
                when(careerChangesRepository.findById(10)).thenReturn(Optional.of(hiredChange));
                when(usersRepository.findByEmployeeId(employee.getId())).thenReturn(Optional.of(targetUser));

                ApprovalRequest req = new ApprovalRequest();
                req.setIsApproved(false);

                careerChangesService.approveOrRejectHiredCareerChange(10, req, authentication);

                assertEquals(CareerChanges.ApprovalStatus.Rejected, hiredChange.getStatus());
                assertEquals(Employees.EmployeeStatus.Rejected, employee.getStatus());
                assertFalse(targetUser.getIsActive()); // Still false
                assertNull(targetUser.getMailStatus()); // Not sending email

                verify(careerChangesRepository).save(hiredChange);
                verify(employeesRepository).save(employee);
                verify(usersRepository).save(targetUser);
                verify(messageProducerService, never()).sendMessage(eq("user-password-email"), any());
        }

        @Test
        void approveCareerChange_NonHiredType_ThrowsConflict() {
                hiredChange.setChangeType(CareerChanges.ChangeType.Promotion);

                doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication)
                                .getAuthorities();
                when(authentication.isAuthenticated()).thenReturn(true);
                when(authentication.getName()).thenReturn("admin");
                when(usersRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
                when(careerChangesRepository.findById(10)).thenReturn(Optional.of(hiredChange));

                ApprovalRequest req = new ApprovalRequest();
                req.setIsApproved(true);

                assertThrows(ConflictException.class,
                                () -> careerChangesService.approveOrRejectHiredCareerChange(10, req, authentication));
        }
}
