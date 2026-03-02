package com.ct08.PharmacyManagement.modules.hr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LeaveRequestCreationDTO {
    @NotBlank(message = "Leave type is required")
    private String leaveType;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    private String reason;

    // Optional field for ADMINs to specify if the leave is paid when auto-approving
    private Boolean isPaidLeave = false;
}
