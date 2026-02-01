package com.ct08.PharmacyManagement.controller;

import com.ct08.PharmacyManagement.dto.ApiResponse;
import com.ct08.PharmacyManagement.dto.UserProfileResponse;
import com.ct08.PharmacyManagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;



    
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(Authentication authentication) {
        String username = authentication.getName();
        UserProfileResponse userProfile = userService.getMyProfile(username);
        return ResponseEntity.ok(ApiResponse.success(userProfile, "Profile retrieved successfully"));
    }
}
