package com.ct08.PharmacyManagement.modules.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BonusEmployeeDTO {
    private Integer bonusId;
    private Integer employeeId;
    private String employeeName;
    private String positionName;
    private Boolean isActive;
    private String status;
}
