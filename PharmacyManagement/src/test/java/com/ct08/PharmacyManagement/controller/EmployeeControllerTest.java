package com.ct08.PharmacyManagement.controller;

import com.ct08.PharmacyManagement.common.dto.ApiResponse;
import com.ct08.PharmacyManagement.modules.hr.controller.EmployeeController;
import com.ct08.PharmacyManagement.modules.hr.dto.EmployeeResponse;
import com.ct08.PharmacyManagement.modules.hr.entity.Employees;
import com.ct08.PharmacyManagement.modules.hr.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private EmployeeController employeeController;

    private Employees employee1;

    @BeforeEach
    void setUp() {
        employee1 = new Employees();
        employee1.setId(1);
    }

    @Test
    void getEmployees_ShouldReturnListOfEmployees() {
        // Arrange
        List<com.ct08.PharmacyManagement.modules.hr.dto.EmployeeResponse> responses = Arrays.asList(new com.ct08.PharmacyManagement.modules.hr.dto.EmployeeResponse());
        when(employeeService.getEmployees(authentication, "id", "asc", null, null, null, null, null, null)).thenReturn(responses);

        // Act
        ResponseEntity<ApiResponse<List<EmployeeResponse>>> response = employeeController.getEmployees(authentication, "id", "asc", null, null, null, null, null, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("Employee list retrieved successfully", response.getBody().getMessage());
        verify(employeeService).getEmployees(authentication, "id", "asc", null, null, null, null, null, null);
    }
}
