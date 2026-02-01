package com.ct08.PharmacyManagement.controller;

import com.ct08.PharmacyManagement.dto.ApiResponse;
import com.ct08.PharmacyManagement.dto.LoginRequest;
import com.ct08.PharmacyManagement.dto.LoginResponse;
import com.ct08.PharmacyManagement.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(loginResponse, "Login successful!"));
    }
}
