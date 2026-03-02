package com.ct08.PharmacyManagement.modules.hr.dto;

import com.ct08.PharmacyManagement.modules.hr.entity.Employees;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class EmployeeSalaryDTO {
    private Integer employeeId;
    private String fullName;
    private String positionName;
    private BigDecimal currentSalary;
    private String status;
    private LocalDate hireDate;

    public EmployeeSalaryDTO(Employees employee) {
        this.employeeId = employee.getId();
        this.fullName = (employee.getLastName() != null ? employee.getLastName() : "")
                + " " + (employee.getFirstName() != null ? employee.getFirstName() : "");
        this.fullName = this.fullName.trim();
        this.positionName = employee.getCurrentPosition() != null
                ? employee.getCurrentPosition().getPositionName()
                : null;
        this.currentSalary = employee.getCurrentSalary();
        this.status = employee.getStatus() != null ? employee.getStatus().name() : null;
        this.hireDate = employee.getHireDate();
    }
}
