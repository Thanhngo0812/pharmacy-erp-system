package com.ct08.PharmacyManagement.modules.hr.dto;

import com.ct08.PharmacyManagement.modules.hr.entity.Bonus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BonusBulkActionRequestDTO {
    @NotEmpty(message = "List of bonus IDs cannot be empty")
    private List<Integer> bonusIds;

    @NotNull(message = "Status is required")
    private Bonus.ApprovalStatus status;

    private String approvalReason;
}
