package com.ct08.PharmacyManagement.modules.hr.dto;

import com.ct08.PharmacyManagement.modules.hr.entity.LeaveRequests.ApprovalStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LeaveRequestResponseDTO {
    private Integer id;
    private Integer employeeId;
    private String employeeName;
    private String leaveType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String reason;
    private String approvalReason;
    private ApprovalStatus status;
    private String approvedByUsername;
}
