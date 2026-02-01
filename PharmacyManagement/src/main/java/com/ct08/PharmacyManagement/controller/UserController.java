package com.ct08.PharmacyManagement.controller;

import com.ct08.PharmacyManagement.dto.ApiResponse;
import com.ct08.PharmacyManagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/{id}/lock")
    public ResponseEntity<ApiResponse<String>> lockAccount(@PathVariable Integer id) {
        try {
            userService.lockAccount(id);
            return ResponseEntity.ok(ApiResponse.success(null, "Account locked successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(String.valueOf(HttpStatus.FORBIDDEN.value()), e.getMessage()));
        } catch (Exception e) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), "An error occurred"));
        }
    }
}
