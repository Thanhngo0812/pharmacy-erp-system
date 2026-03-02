package com.ct08.PharmacyManagement.modules.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO tóm tắt lương tháng cho từng NV trong bảng lương tổng.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeePayrollDTO {
    private Integer employeeId;
    private String fullName;
    private String positionName;
    private BigDecimal baseSalary;
    private String salaryNote; // Ghi chú nếu lương thay đổi trong tháng
    private int workingDays; // Số ngày công thực tế (bao gồm cả nghỉ có lương)
    private int paidLeaveDays; // Nghỉ có lương
    private int unpaidLeaveDays; // Nghỉ không lương
    private BigDecimal leaveDeduction;
    private List<BonusDetailDTO> bonuses; // Danh sách chi tiết thưởng/phạt
    private BigDecimal totalAllowance;
    private BigDecimal totalPenalty;
    private BigDecimal totalBonus;
    private BigDecimal totalSalary;
}
