package com.ct08.PharmacyManagement.modules.hr.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PositionRequest {
    @NotBlank(message = "Position name is required")
    private String positionName;

    @NotBlank(message = "Lý do đề xuất là bắt buộc")
    private String reason;
}
