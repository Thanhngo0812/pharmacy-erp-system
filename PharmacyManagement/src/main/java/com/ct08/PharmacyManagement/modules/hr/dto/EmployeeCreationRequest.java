package com.ct08.PharmacyManagement.modules.hr.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class EmployeeCreationRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotBlank(message = "Position is required")
    private String positionName;

    private BigDecimal currentSalary = BigDecimal.ZERO;

    @NotNull(message = "Hire date is required")
    private LocalDate hireDate;

    @NotNull(message = "At least one role is required")
    private List<String> roles;
}
