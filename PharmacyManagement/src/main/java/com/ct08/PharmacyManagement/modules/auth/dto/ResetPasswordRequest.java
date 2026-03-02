package com.ct08.PharmacyManagement.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email format")
    private String email;

    @NotBlank(message = "OTP is required")
    @Size(min = 6, max = 6, message = "OTP must be exactly 6 characters")
    private String otp;

    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "New password must be at least 6 characters")
    private String newPassword;
}
