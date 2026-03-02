package com.ct08.PharmacyManagement.modules.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO thống kê quỹ lương theo từng tháng (dùng cho API summary).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollSummaryDTO {
    private int month;
    private int year;
    private int totalEmployees;
    private BigDecimal totalPayroll;
    private BigDecimal totalAllowance;
    private BigDecimal totalPenalty;
    private BigDecimal totalBonus;
    private BigDecimal totalDeduction;
}
