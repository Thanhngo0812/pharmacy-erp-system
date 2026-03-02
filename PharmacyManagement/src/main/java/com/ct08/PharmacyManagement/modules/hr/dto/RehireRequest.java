package com.ct08.PharmacyManagement.modules.hr.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class RehireRequest {
    @NotNull(message = "Date is required")
    private LocalDate date;
    
    // Optional: if provided, use this salary. If null, try to restore old salary.
    private BigDecimal newSalary;

    private String reason;

    public RehireRequest() {
    }

    public RehireRequest(LocalDate date, BigDecimal newSalary, String reason) {
        this.date = date;
        this.newSalary = newSalary;
        this.reason = reason;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getNewSalary() {
        return newSalary;
    }

    public void setNewSalary(BigDecimal newSalary) {
        this.newSalary = newSalary;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
