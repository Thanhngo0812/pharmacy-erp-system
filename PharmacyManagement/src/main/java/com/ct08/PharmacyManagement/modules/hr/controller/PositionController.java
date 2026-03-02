package com.ct08.PharmacyManagement.modules.hr.controller;

import com.ct08.PharmacyManagement.common.dto.ApiResponse;
import com.ct08.PharmacyManagement.modules.hr.entity.Positions;
import com.ct08.PharmacyManagement.modules.hr.repository.PositionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.ct08.PharmacyManagement.modules.hr.dto.PositionResponse;
import com.ct08.PharmacyManagement.modules.hr.dto.PositionRequest;
import com.ct08.PharmacyManagement.modules.hr.dto.PositionStatusUpdateRequest;
import com.ct08.PharmacyManagement.modules.hr.service.PositionService;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/positions")
public class PositionController {

    @Autowired
    private PositionsRepository positionsRepository;

    @Autowired
    private PositionService positionService;

    // Returns only approved positions (for dropdowns, general use)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'HM')")
    public ResponseEntity<ApiResponse<List<PositionResponse>>> getApprovedPositions() {
        List<Positions> positions = positionsRepository.findByStatus(Positions.ApprovalStatus.Approved);
        List<PositionResponse> responses = positions.stream()
                .map(PositionResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses, "Positions retrieved successfully"));
    }

    // Returns all positions (including Pending, Rejected) for management
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'HM')")
    public ResponseEntity<ApiResponse<List<PositionResponse>>> getAllPositions(Authentication authentication) {
        List<PositionResponse> responses = positionService.getAllPositionsAdmin(authentication);
        return ResponseEntity.ok(ApiResponse.success(responses, "All positions retrieved successfully"));
    }

    // Search positions by keyword and status
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'HM')")
    public ResponseEntity<ApiResponse<List<PositionResponse>>> searchPositions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            Authentication authentication) {
        List<PositionResponse> responses = positionService.searchPositionsAdmin(keyword, status, authentication);
        return ResponseEntity.ok(ApiResponse.success(responses, "Positions searched successfully"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HM')")
    public ResponseEntity<ApiResponse<String>> createPosition(
            @Valid @RequestBody PositionRequest request,
            Authentication authentication) {
        positionService.createPosition(request, authentication);
        return ResponseEntity.ok(ApiResponse.success("Position created successfully", "Position created successfully"));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> updatePositionStatus(
            @PathVariable Integer id,
            @Valid @RequestBody PositionStatusUpdateRequest request,
            Authentication authentication) {
        positionService.updatePositionStatus(id, request, authentication);
        return ResponseEntity.ok(ApiResponse.success("Position status updated successfully", "Position status updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HM')")
    public ResponseEntity<ApiResponse<String>> deletePosition(
            @PathVariable Integer id,
            Authentication authentication) {
        positionService.deletePosition(id, authentication);
        return ResponseEntity.ok(ApiResponse.success("Position deleted successfully", "Position deleted successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> updatePositionName(
            @PathVariable Integer id,
            @Valid @RequestBody PositionRequest request,
            Authentication authentication) {
        positionService.updatePositionName(id, request, authentication);
        return ResponseEntity.ok(ApiResponse.success("Position updated successfully", "Position updated successfully"));
    }
}
