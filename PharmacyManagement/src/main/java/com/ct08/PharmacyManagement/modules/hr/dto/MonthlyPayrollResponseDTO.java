package com.ct08.PharmacyManagement.modules.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO wrapper cho bảng lương tháng: summary + danh sách NV.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyPayrollResponseDTO {
    private int month;
    private int year;
    private PayrollSummarySectionDTO summary;
    private List<EmployeePayrollDTO> employees;
    private List<String> allBonusNames; // Danh sách tất cả các loại thưởng/phạt xuất hiện trong tháng

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PayrollSummarySectionDTO {
        private int totalEmployees;
        private BigDecimal totalPayroll;
        private BigDecimal totalAllowance;
        private BigDecimal totalPenalty;
        private BigDecimal totalBonus;
        private BigDecimal totalDeduction;
    }
}
