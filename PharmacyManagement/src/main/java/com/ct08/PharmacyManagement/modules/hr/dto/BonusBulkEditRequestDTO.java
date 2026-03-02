package com.ct08.PharmacyManagement.modules.hr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BonusBulkEditRequestDTO {
    @NotEmpty(message = "List of bonus IDs cannot be empty")
    private List<Integer> bonusIds;

    @NotBlank(message = "Bonus name cannot be blank")
    private String bonusName;

    private LocalDate endDate;
}
