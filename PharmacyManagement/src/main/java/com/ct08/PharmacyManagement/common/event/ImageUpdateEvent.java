package com.ct08.PharmacyManagement.common.event;

public class ImageUpdateEvent {
    private Integer employeeId;
    private String localFilePath;
    private String oldImageUrl;

    public ImageUpdateEvent() {
    }

    public ImageUpdateEvent(Integer employeeId, String localFilePath, String oldImageUrl) {
        this.employeeId = employeeId;
        this.localFilePath = localFilePath;
        this.oldImageUrl = oldImageUrl;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getLocalFilePath() {
        return localFilePath;
    }

    public void setLocalFilePath(String localFilePath) {
        this.localFilePath = localFilePath;
    }

    public String getOldImageUrl() {
        return oldImageUrl;
    }

    public void setOldImageUrl(String oldImageUrl) {
        this.oldImageUrl = oldImageUrl;
    }
}
