package com.ct08.PharmacyManagement.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpEmailEvent {
    private String email;
    private String otp;
    private String fullName;
}
