package com.ct08.PharmacyManagement.modules.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MySalaryResponseDTO {
    private Integer employeeId;
    private String fullName;
    private String positionName;
    private int month;
    private int year;

    private BigDecimal baseSalary;

    private int unpaidLeaveDays;
    private BigDecimal leaveDeduction;

    private List<BonusDetailDTO> bonuses;
    private BigDecimal totalBonus;

    private BigDecimal totalSalary;
}
