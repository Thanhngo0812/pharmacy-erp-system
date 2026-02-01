package com.ct08.PharmacyManagement.controller;

import com.ct08.PharmacyManagement.dto.ApiResponse;
import com.ct08.PharmacyManagement.dto.UserProfileResponse;
import com.ct08.PharmacyManagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/{id}/lock")
    public ResponseEntity<ApiResponse<String>> lockAccount(@PathVariable Integer id) {
        userService.lockAccount(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Account locked successfully"));
    }

    
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(Authentication authentication) {
        String username = authentication.getName();
        UserProfileResponse userProfile = userService.getMyProfile(username);
        return ResponseEntity.ok(ApiResponse.success(userProfile, "Profile retrieved successfully"));

    }
}
