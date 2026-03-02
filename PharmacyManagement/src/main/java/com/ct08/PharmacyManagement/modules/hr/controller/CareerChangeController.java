package com.ct08.PharmacyManagement.modules.hr.controller;

import com.ct08.PharmacyManagement.common.dto.ApiResponse;
import com.ct08.PharmacyManagement.modules.hr.dto.ApprovalRequest;
import com.ct08.PharmacyManagement.modules.hr.dto.CareerChangeRequest;
import com.ct08.PharmacyManagement.modules.hr.dto.HiredCareerChangeResponse;
import com.ct08.PharmacyManagement.modules.hr.service.CareerChangesService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CareerChangeController {

    @Autowired
    private CareerChangesService careerChangesService;

    // =====================================================
    // General Career Change endpoints
    // =====================================================

    @GetMapping("/api/career-changes")
    @PreAuthorize("hasAnyRole('ADMIN', 'HM')")
    public ResponseEntity<ApiResponse<java.util.List<com.ct08.PharmacyManagement.modules.hr.dto.CareerHistoryDTO>>> getCareerChanges(
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String order,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) Integer employeeId,
            @RequestParam(required = false) String changeType,
            @RequestParam(required = false) Integer proposedById,
            Authentication authentication) {

        java.util.List<com.ct08.PharmacyManagement.modules.hr.dto.CareerHistoryDTO> result = careerChangesService
                .getCareerChanges(sortBy, order, status, id, employeeId, changeType, proposedById, authentication);
        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched career changes", result));
    }

    @GetMapping("/api/career-changes/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'HM')")
    public ResponseEntity<ApiResponse<java.util.List<com.ct08.PharmacyManagement.modules.hr.dto.CareerHistoryDTO>>> getMyCareerChanges(
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String order,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) Integer employeeId,
            @RequestParam(required = false) String changeType,
            Authentication authentication) {

        java.util.List<com.ct08.PharmacyManagement.modules.hr.dto.CareerHistoryDTO> result = careerChangesService
                .getMyCareerChanges(sortBy, order, status, id, employeeId, changeType, authentication);
        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched my career changes", result));
    }

    @PostMapping("/api/career-changes")
    @PreAuthorize("hasAnyRole('ADMIN', 'HM')")
    public ResponseEntity<ApiResponse<Void>> createCareerChange(
            @Valid @RequestBody CareerChangeRequest request,
            Authentication authentication) {

        careerChangesService.createCareerChange(request, authentication);
        return ResponseEntity.ok(new ApiResponse<>(true, "Career change created successfully", null));
    }

    @PutMapping("/api/career-changes/{id}/action")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> approveOrRejectCareerChange(
            @PathVariable Integer id,
            @Valid @RequestBody ApprovalRequest request,
            Authentication authentication) {

        careerChangesService.approveOrRejectCareerChange(id, request, authentication);
        String actionStr = request.getIsApproved() ? "approved" : "rejected";
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Career change " + actionStr + " successfully", null));
    }

    // =====================================================
    // Hired Career Change endpoints (existing)
    // =====================================================

    @GetMapping("/api/v1/career-changes/hired")
    public ResponseEntity<ApiResponse<List<HiredCareerChangeResponse>>> getHiredCareerChanges(
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String order,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) Integer proposedById,
            Authentication authentication) {

        List<HiredCareerChangeResponse> result = careerChangesService.getHiredCareerChanges(sortBy, order, status,
                id, employeeName, proposedById, authentication);
        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched hired career changes", result));
    }

    @PutMapping("/api/v1/career-changes/hired/{id}/action")
    public ResponseEntity<ApiResponse<Void>> approveOrRejectHiredCareerChange(
            @PathVariable Integer id,
            @Valid @RequestBody ApprovalRequest request,
            Authentication authentication) {

        careerChangesService.approveOrRejectHiredCareerChange(id, request, authentication);
        String actionStr = request.getIsApproved() ? "approved" : "rejected";
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Hired career change " + actionStr + " successfully", null));
    }
}
