package com.ct08.PharmacyManagement.modules.hr.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BonusToggleRequestDTO {
    @NotEmpty(message = "Bonus list cannot be empty")
    private List<Integer> bonusIds;

    @NotNull(message = "Active status cannot be null")
    private Boolean isActive;

    private String reason;
}
