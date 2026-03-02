package com.ct08.PharmacyManagement.modules.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BonusToggleHistoryResponseDTO {
    private Integer bonusId;
    private Boolean isActive;
    private LocalDateTime toggledAt;
    private Integer toggledById;
    private String toggledByName;
    private String reason;
}
