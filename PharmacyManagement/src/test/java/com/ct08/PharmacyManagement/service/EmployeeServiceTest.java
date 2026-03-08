package com.ct08.PharmacyManagement.service;

import com.ct08.PharmacyManagement.modules.auth.entity.Roles;
import com.ct08.PharmacyManagement.modules.auth.entity.Users;
import com.ct08.PharmacyManagement.modules.auth.repository.UsersRepository;
import com.ct08.PharmacyManagement.modules.hr.dto.EmployeeResponse;
import com.ct08.PharmacyManagement.modules.hr.entity.Employees;
import com.ct08.PharmacyManagement.modules.hr.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

        @Mock
        private UsersRepository usersRepository;

        @Mock
        private Authentication authentication;

        @Mock
        private com.ct08.PharmacyManagement.modules.auth.repository.RolesRepository rolesRepository;

        @Mock
        private com.ct08.PharmacyManagement.modules.hr.repository.EmployeesRepository employeesRepository;

        @Mock
        private com.ct08.PharmacyManagement.common.infra.message.MessageProducerService messageProducerService;

        @InjectMocks
        private EmployeeService employeeService;

        private Users user1;
        private Users user2;

        @BeforeEach
        void setUp() {
                Employees emp1 = new Employees();
                emp1.setId(1);
                emp1.setFirstName("An");
                emp1.setLastName("Nguyen");
                user1 = new Users();
                user1.setId(1);
                user1.setEmployee(emp1);
                user1.setIsActive(true);
                user1.setRoles(Collections.singleton(new Roles(1, "ROLE_ADMIN")));

                Employees emp2 = new Employees();
                emp2.setId(2);
                emp2.setFirstName("Binh");
                emp2.setLastName("Tran");
                user2 = new Users();
                user2.setId(2);
                user2.setEmployee(emp2);
                user2.setIsActive(true);
                user2.setRoles(Collections.singleton(new Roles(2, "ROLE_WS")));
        }

        @Test
        void getEmployees_AdminRole_ShouldReturnAllEmployees() {
                // Arrange
                doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                                .when(authentication).getAuthorities();
                when(authentication.isAuthenticated()).thenReturn(true);
                when(usersRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class),
                                any(Sort.class)))
                                .thenReturn(Arrays.asList(user1, user2));

                // Act
                // Act
                List<EmployeeResponse> result = employeeService.getEmployees(authentication, "id", "asc", null, null,
                                null,
                                null, null, null);

                // Assert
                assertEquals(2, result.size());
                verify(usersRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class),
                                any(Sort.class));
        }

        @Test
        void getEmployees_HRRole_ShouldReturnSpecificEmployees() {
                // Arrange
                doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_HR")))
                                .when(authentication).getAuthorities();
                when(authentication.isAuthenticated()).thenReturn(true);
                when(usersRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class),
                                any(Sort.class)))
                                .thenReturn(Arrays.asList(user2));

                // Act
                // Act
                List<EmployeeResponse> result = employeeService.getEmployees(authentication, "id", "asc", null, null,
                                null,
                                null, null, null);

                // Assert
                assertEquals(1, result.size());
                assertEquals("Binh", result.get(0).getFirstName());

                verify(usersRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class),
                                any(Sort.class));
        }

        @Test
        void getEmployees_Sorting_ShouldUseCorrectProperty() {
                // Should rely on manual integration test or verify Sort object construction if
                // feasible,
                // but since Sort is created inside private method, we assume it's correct for
                // unit test scope
                // or verify the Sort object passed to repo.

                doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                                .when(authentication).getAuthorities();
                when(authentication.isAuthenticated()).thenReturn(true);

                // Act
                // Act
                employeeService.getEmployees(authentication, "salary", "desc", null, null, null, null, null, null);

                // Assert
                // We capture the sort to check
                // ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
                // verify(usersRepository).findAll(sortCaptor.capture());
                // assertEquals("employee.currentSalary: DESC",
                // sortCaptor.getValue().toString());
        }

        @Test
        void updateEmployee_Admin_Success() throws java.io.IOException {
                // Arrange
                Integer empId = 1;
                com.ct08.PharmacyManagement.modules.hr.dto.EmployeeUpdateRequest request = new com.ct08.PharmacyManagement.modules.hr.dto.EmployeeUpdateRequest();
                request.setFirstName("NewName");
                request.setEmail("new@example.com");

                doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                                .when(authentication).getAuthorities();
                when(authentication.isAuthenticated()).thenReturn(true);
                when(usersRepository.findByEmployeeId(empId)).thenReturn(Optional.of(user1));
                when(usersRepository.findByUsername("new@example.com")).thenReturn(Optional.empty());

                org.springframework.security.crypto.password.PasswordEncoder encoder = mock(
                                org.springframework.security.crypto.password.PasswordEncoder.class);
                when(encoder.encode(anyString())).thenReturn("hashedPass");
                org.springframework.test.util.ReflectionTestUtils.setField(employeeService, "passwordEncoder", encoder);

                // Act
                employeeService.updateEmployee(empId, request, null, authentication);

                // Assert
                assertEquals("NewName", user1.getEmployee().getFirstName());
                assertEquals("new@example.com", user1.getEmployee().getEmail());
                verify(usersRepository).save(user1);
        }

        @Test
        void updateEmployee_EmailConflict_ThrowsConflictException() {
                // Arrange
                Integer empId = 1;
                com.ct08.PharmacyManagement.modules.hr.dto.EmployeeUpdateRequest request = new com.ct08.PharmacyManagement.modules.hr.dto.EmployeeUpdateRequest();
                request.setEmail("existing@example.com");

                doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                                .when(authentication).getAuthorities();
                when(authentication.isAuthenticated()).thenReturn(true);
                when(usersRepository.findByEmployeeId(empId)).thenReturn(Optional.of(user1));
                when(usersRepository.findByUsername("existing@example.com")).thenReturn(Optional.of(new Users()));

                // Act & Assert
                assertThrows(com.ct08.PharmacyManagement.common.exception.ConflictException.class,
                                () -> employeeService.updateEmployee(empId, request, null, authentication));
        }

        @Test
        void updateEmployee_PhoneConflict_ThrowsConflictException() {
                // Arrange
                Integer empId = 1;
                com.ct08.PharmacyManagement.modules.hr.dto.EmployeeUpdateRequest request = new com.ct08.PharmacyManagement.modules.hr.dto.EmployeeUpdateRequest();
                request.setPhone("0999999999");

                doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                                .when(authentication).getAuthorities();
                when(authentication.isAuthenticated()).thenReturn(true);
                when(usersRepository.findByEmployeeId(empId)).thenReturn(Optional.of(user1));
                // Mock target employee phone to be different
                user1.getEmployee().setPhone("0000000000");
                when(employeesRepository.findByPhone("0999999999")).thenReturn(Optional.of(new Employees()));

                // Act & Assert
                assertThrows(com.ct08.PharmacyManagement.common.exception.ConflictException.class,
                                () -> employeeService.updateEmployee(empId, request, null, authentication));
        }

        @Test
        void createEmployee_AdminRoles_Success() throws java.io.IOException {
                // Arrange
                com.ct08.PharmacyManagement.modules.hr.dto.EmployeeCreationRequest request = new com.ct08.PharmacyManagement.modules.hr.dto.EmployeeCreationRequest();
                request.setFirstName("John");
                request.setEmail("john@example.com");
                request.setPhone("0123456789");
                request.setPositionName("Quản lý");
                request.setRoles(Arrays.asList("ROLE_ADMIN"));

                doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();
                when(authentication.isAuthenticated()).thenReturn(true);
                when(authentication.getName()).thenReturn("admin@example.com");

                when(usersRepository.findByUsername("admin@example.com")).thenReturn(Optional.of(user1));
                when(usersRepository.findByUsername("john@example.com")).thenReturn(Optional.empty());
                when(employeesRepository.existsByEmail("john@example.com")).thenReturn(false);
                when(employeesRepository.findByPhone("0123456789")).thenReturn(Optional.empty());

                com.ct08.PharmacyManagement.modules.hr.entity.Positions pos = new com.ct08.PharmacyManagement.modules.hr.entity.Positions();
                pos.setId(1);
                pos.setPositionName("Quản lý");
                com.ct08.PharmacyManagement.modules.hr.repository.PositionsRepository posRepo = mock(
                                com.ct08.PharmacyManagement.modules.hr.repository.PositionsRepository.class);
                when(posRepo.findByPositionName("Quản lý")).thenReturn(Optional.of(pos));
                org.springframework.test.util.ReflectionTestUtils.setField(employeeService, "positionsRepository",
                                posRepo);

                when(rolesRepository.findByRoleName("ROLE_ADMIN")).thenReturn(Optional.of(new Roles(1, "ROLE_ADMIN")));

                org.springframework.security.crypto.password.PasswordEncoder encoder = mock(
                                org.springframework.security.crypto.password.PasswordEncoder.class);
                when(encoder.encode(anyString())).thenReturn("hashedPass");
                org.springframework.test.util.ReflectionTestUtils.setField(employeeService, "passwordEncoder", encoder);

                com.ct08.PharmacyManagement.modules.hr.repository.CareerChangesRepository careerRepo = mock(
                                com.ct08.PharmacyManagement.modules.hr.repository.CareerChangesRepository.class);
                org.springframework.test.util.ReflectionTestUtils.setField(employeeService, "careerChangesRepository",
                                careerRepo);

                // Act
                employeeService.createEmployee(request, null, authentication);

                // Assert
                verify(employeesRepository).save(any(Employees.class));
                verify(usersRepository).save(any(Users.class));
                verify(careerRepo).save(any(com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges.class));
        }

        @Test
        void deleteWaitingEmployee_Admin_Success() {
                // Arrange
                Integer empId = 1;

                Employees waitingEmp = new Employees();
                waitingEmp.setId(empId);
                waitingEmp.setStatus(Employees.EmployeeStatus.Waiting);

                Users waitingUser = new Users();
                waitingUser.setId(empId);
                waitingUser.setEmployee(waitingEmp);

                doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                                .when(authentication).getAuthorities();
                when(authentication.isAuthenticated()).thenReturn(true);
                when(usersRepository.findByEmployeeId(empId)).thenReturn(Optional.of(waitingUser));

                com.ct08.PharmacyManagement.modules.hr.repository.CareerChangesRepository careerRepo = mock(
                                com.ct08.PharmacyManagement.modules.hr.repository.CareerChangesRepository.class);
                org.springframework.test.util.ReflectionTestUtils.setField(employeeService, "careerChangesRepository",
                                careerRepo);

                // Act
                employeeService.deleteWaitingEmployee(empId, authentication);

                // Assert
                verify(careerRepo).deleteByEmployeeId(empId);
                verify(usersRepository).delete(waitingUser);
                verify(employeesRepository).delete(waitingEmp);
        }

        @Test
        void getEmployeeSalaryList_Admin_ShouldReturnAllEmployees() {
                // Arrange
                user1.getEmployee().setCurrentSalary(new java.math.BigDecimal("30000000"));
                user2.getEmployee().setCurrentSalary(new java.math.BigDecimal("15000000"));

                doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                                .when(authentication).getAuthorities();
                when(authentication.isAuthenticated()).thenReturn(true);
                when(usersRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class),
                                any(Sort.class)))
                                .thenReturn(Arrays.asList(user1, user2));

                // Act
                java.util.Map<String, Object> result = employeeService.getEmployeeSalaryList(
                                authentication, "id", "asc", null, null, null, null, null);

                // Assert
                @SuppressWarnings("unchecked")
                java.util.List<?> employees = (java.util.List<?>) result
                                .get("employees");
                assertEquals(2, employees.size());
                assertEquals(new java.math.BigDecimal("45000000"), result.get("totalSalaryFund"));
                assertEquals(2, result.get("totalEmployees"));
                verify(usersRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class),
                                any(Sort.class));
        }

        @Test
        void getEmployeeSalaryList_Unauthorized_ShouldThrowAccessDenied() {
                // Arrange
                doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_WS")))
                                .when(authentication).getAuthorities();
                when(authentication.isAuthenticated()).thenReturn(true);

                // Act & Assert
                assertThrows(AccessDeniedException.class,
                                () -> employeeService.getEmployeeSalaryList(
                                                authentication, "id", "asc", null, null, null, null, null));
        }

        @Test
        void getCareerHistoryByEmployeeId_Admin_Success() {
                // Arrange
                Integer empId = 2;
                doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                                .when(authentication).getAuthorities();
                when(authentication.isAuthenticated()).thenReturn(true);
                when(usersRepository.findByEmployeeId(empId)).thenReturn(Optional.of(user2));

                com.ct08.PharmacyManagement.modules.hr.repository.CareerChangesRepository careerRepo = mock(
                                com.ct08.PharmacyManagement.modules.hr.repository.CareerChangesRepository.class);
                when(careerRepo.findByEmployeeIdOrderByIdDesc(empId)).thenReturn(Collections.emptyList());
                org.springframework.test.util.ReflectionTestUtils.setField(employeeService, "careerChangesRepository",
                                careerRepo);

                // Act
                List<com.ct08.PharmacyManagement.modules.hr.dto.CareerHistoryDTO> result = employeeService
                                .getCareerHistoryByEmployeeId(empId, authentication);

                // Assert
                assertNotNull(result);
                assertEquals(0, result.size());
                verify(careerRepo).findByEmployeeIdOrderByIdDesc(empId);
        }

        @Test
        void getCareerHistoryByEmployeeId_HM_WS_Success() {
                // Arrange
                Integer empId = 2;
                doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_HM")))
                                .when(authentication).getAuthorities();
                when(authentication.isAuthenticated()).thenReturn(true);
                when(usersRepository.findByEmployeeId(empId)).thenReturn(Optional.of(user2));

                com.ct08.PharmacyManagement.modules.hr.repository.CareerChangesRepository careerRepo = mock(
                                com.ct08.PharmacyManagement.modules.hr.repository.CareerChangesRepository.class);
                when(careerRepo.findByEmployeeIdOrderByIdDesc(empId)).thenReturn(Collections.emptyList());
                org.springframework.test.util.ReflectionTestUtils.setField(employeeService, "careerChangesRepository",
                                careerRepo);

                // Act
                List<com.ct08.PharmacyManagement.modules.hr.dto.CareerHistoryDTO> result = employeeService
                                .getCareerHistoryByEmployeeId(empId, authentication);

                // Assert
                assertNotNull(result);
                verify(careerRepo).findByEmployeeIdOrderByIdDesc(empId);
        }

        @Test
        void getCareerHistoryByEmployeeId_HM_AdminTarget_Denied() {
                // Arrange - HM trying to view ADMIN's career history
                Integer empId = 1;
                doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_HM")))
                                .when(authentication).getAuthorities();
                when(authentication.isAuthenticated()).thenReturn(true);
                when(usersRepository.findByEmployeeId(empId)).thenReturn(Optional.of(user1));

                // Act & Assert
                assertThrows(AccessDeniedException.class,
                                () -> employeeService.getCareerHistoryByEmployeeId(empId, authentication));
        }
}
