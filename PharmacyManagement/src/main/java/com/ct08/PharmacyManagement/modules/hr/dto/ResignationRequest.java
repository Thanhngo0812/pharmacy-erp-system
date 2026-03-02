package com.ct08.PharmacyManagement.modules.hr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class ResignationRequest {
    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotBlank(message = "Reason is required")
    private String reason;

    public ResignationRequest() {
    }

    public ResignationRequest(LocalDate date, String reason) {
        this.date = date;
        this.reason = reason;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
