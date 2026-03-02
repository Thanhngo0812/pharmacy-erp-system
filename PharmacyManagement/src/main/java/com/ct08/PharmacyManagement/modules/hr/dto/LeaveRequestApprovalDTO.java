package com.ct08.PharmacyManagement.modules.hr.dto;

import com.ct08.PharmacyManagement.modules.hr.entity.LeaveRequests.ApprovalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class LeaveRequestApprovalDTO {
    @NotNull(message = "Status is required")
    private ApprovalStatus status;

    @NotBlank(message = "Lý do duyệt/từ chối là bắt buộc")
    private String approvalReason;
}
