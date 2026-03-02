package com.ct08.PharmacyManagement.modules.auth.dto;

import java.util.List;

public class LoginResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private Integer employeeId;
    private String fullName;
    private List<String> roles;
    private String email;
    private Long expiresIn;
    private String imgUrl;
    public LoginResponse() {
    }

    public LoginResponse(String accessToken, String tokenType, Integer employeeId, String fullName, List<String> roles,String email, Long expiresIn, String imgUrl) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.roles = roles;
        this.expiresIn = expiresIn;
        this.imgUrl = imgUrl;
        this.email = email;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
     public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}
}
