package com.ct08.PharmacyManagement.modules.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BonusGroupResponseDTO {
    private String bonusName;
    private BigDecimal amount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String status;
    private String approvalReason;
    private Integer proposedById;
    private String proposedByName;
    private Integer approvedById;
    private String approvedByName;
    private int employeeCount;
    private int activeCount;
    private List<BonusEmployeeDTO> employees;
}
