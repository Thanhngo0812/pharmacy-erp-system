package com.ct08.PharmacyManagement.common.event;

public class PasswordEmailEvent {
    private Integer userId;
    private String email;
    private String fullName;
    private String newPassword;

    public PasswordEmailEvent() {
    }

    public PasswordEmailEvent(Integer userId, String email, String fullName, String newPassword) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.newPassword = newPassword;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
