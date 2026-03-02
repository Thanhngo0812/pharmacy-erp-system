package com.ct08.PharmacyManagement.modules.hr.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BonusSingleEditRequestDTO {
    @NotBlank(message = "Bonus name cannot be blank")
    private String bonusName;

    private LocalDate endDate;
}
