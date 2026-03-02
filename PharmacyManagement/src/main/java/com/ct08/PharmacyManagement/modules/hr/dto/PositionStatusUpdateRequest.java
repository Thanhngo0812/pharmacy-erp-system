package com.ct08.PharmacyManagement.modules.hr.dto;

import com.ct08.PharmacyManagement.modules.hr.entity.Positions;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PositionStatusUpdateRequest {
    @NotNull(message = "Status is required")
    private Positions.ApprovalStatus status;

    @NotBlank(message = "Lý do duyệt/từ chối là bắt buộc")
    private String approvalReason;
}
