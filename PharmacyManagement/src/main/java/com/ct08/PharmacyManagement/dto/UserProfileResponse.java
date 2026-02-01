package com.ct08.PharmacyManagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class UserProfileResponse {
    private String fullName;
    private String email;
    private String phone;
    private String imageUrl;
    private String positionName;
    private BigDecimal currentSalary;
    private LocalDate hireDate;

    public UserProfileResponse() {
    }

    public UserProfileResponse(String fullName, String email, String phone, String imageUrl, 
                               String positionName, BigDecimal currentSalary, LocalDate hireDate) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.imageUrl = imageUrl;
        this.positionName = positionName;
        this.currentSalary = currentSalary;
        this.hireDate = hireDate;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public BigDecimal getCurrentSalary() {
        return currentSalary;
    }

    public void setCurrentSalary(BigDecimal currentSalary) {
        this.currentSalary = currentSalary;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }
}
