package com.ct08.PharmacyManagement.modules.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO chi tiết lương 1 NV theo tháng, bao gồm các giai đoạn lương,
 * chi tiết nghỉ phép và từng khoản bonus.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeePayrollDetailDTO {
    private Integer employeeId;
    private String fullName;
    private String positionName;
    private int month;
    private int year;

    private BigDecimal baseSalary;
    private List<SalaryPeriodDTO> salaryChanges;

    private int unpaidLeaveDays;
    private BigDecimal leaveDeduction;
    private List<LeaveDetailDTO> leaveDetails;

    private List<BonusDetailDTO> bonuses;
    private BigDecimal totalBonus;

    private BigDecimal totalSalary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalaryPeriodDTO {
        private LocalDate fromDate;
        private LocalDate toDate;
        private BigDecimal salary;
        private int days;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaveDetailDTO {
        private LocalDate startDate;
        private LocalDate endDate;
        private int days;
        private String type;
    }
}
