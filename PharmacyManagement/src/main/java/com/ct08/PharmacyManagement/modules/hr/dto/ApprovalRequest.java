package com.ct08.PharmacyManagement.modules.hr.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class ApprovalRequest {
    @NotNull(message = "isApproved is required")
    private Boolean isApproved;

    @NotNull(message = "reason is required")
    private String reason;
}
