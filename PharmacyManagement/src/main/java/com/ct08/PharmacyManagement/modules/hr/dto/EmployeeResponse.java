package com.ct08.PharmacyManagement.modules.hr.dto;

import com.ct08.PharmacyManagement.modules.auth.entity.Roles;
import com.ct08.PharmacyManagement.modules.hr.entity.Employees;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class EmployeeResponse {
    private Integer id;
    private String lastName;
    private String firstName;
    private String email;
    private String phone;
    private String imageUrl;
    private String positionName;
    private BigDecimal currentSalary;
    private String status;
    private LocalDate hireDate;
    private Set<String> roles;
    private Boolean isActive;

    private String mailStatus;

    private Integer proposedById;
    private String proposedByName;
    private Integer approvedById;
    private String approvedByName;

    public EmployeeResponse(Employees employee, Set<Roles> roles, boolean isActive, String mailStatus) {
        this.id = employee.getId();
        this.lastName = employee.getLastName();
        this.firstName = employee.getFirstName();
        this.email = employee.getEmail();
        this.phone = employee.getPhone();
        this.imageUrl = employee.getImageUrl();
        this.positionName = employee.getCurrentPosition() != null ? employee.getCurrentPosition().getPositionName()
                : null;
        this.currentSalary = employee.getCurrentSalary();
        this.status = employee.getStatus() != null ? employee.getStatus().name() : null;
        this.hireDate = employee.getHireDate();
        this.roles = roles.stream().map(Roles::getRoleName).collect(Collectors.toSet());
        this.isActive = isActive;
        this.mailStatus = mailStatus;
    }

    // Explicit getter for currentPosition manually because lombok might miss the
    // logic inside constructor if field name differs
    // Actually, let's keep field name same as DTO property to avoid confusion.
    // Adjusted constructor to map positionName.

    // Re-writing field to match constructor assignment
    // private String positionName; // Up there
}
