package com.ct08.PharmacyManagement.modules.hr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class CareerChangeRequest {
    @NotNull(message = "employeeId is required")
    private Integer employeeId;

    @NotNull(message = "changeType is required")
    private String changeType;

    private BigDecimal newSalary;

    private String newPositionName;

    @NotNull(message = "effectiveDate is required")
    private LocalDate effectiveDate;

    @NotNull(message = "reason is required")
    private String reason;
}
