package com.ct08.PharmacyManagement.modules.hr.controller;

import com.ct08.PharmacyManagement.common.dto.ApiResponse;
import com.ct08.PharmacyManagement.modules.hr.dto.LeaveRequestApprovalDTO;
import com.ct08.PharmacyManagement.modules.hr.dto.LeaveRequestCreationDTO;
import com.ct08.PharmacyManagement.modules.hr.dto.LeaveRequestResponseDTO;
import com.ct08.PharmacyManagement.modules.hr.service.LeaveRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/hr/leave-requests")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> createLeaveRequest(@Valid @RequestBody LeaveRequestCreationDTO request) {
        leaveRequestService.createLeaveRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "Leave request created successfully"));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'HM')")
    public ResponseEntity<ApiResponse<String>> approveLeaveRequest(
            @PathVariable Integer id,
            @Valid @RequestBody LeaveRequestApprovalDTO request) {
        leaveRequestService.approveLeaveRequest(id, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Leave request status updated successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HM')")
    public ResponseEntity<ApiResponse<List<LeaveRequestResponseDTO>>> getAllLeaveRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<LeaveRequestResponseDTO> requests = leaveRequestService.getAllLeaveRequests(status, employeeId, startDate,
                endDate);
        return ResponseEntity.ok(ApiResponse.success(requests, "Retrieved leave requests successfully"));
    }

    @GetMapping("/my-requests")
    public ResponseEntity<ApiResponse<List<LeaveRequestResponseDTO>>> getMyLeaveRequests(
            @RequestParam(required = false) String status) {
        List<LeaveRequestResponseDTO> requests = leaveRequestService.getMyLeaveRequests(status);
        return ResponseEntity.ok(ApiResponse.success(requests, "Retrieved your leave requests successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteLeaveRequest(@PathVariable Integer id) {
        leaveRequestService.deleteLeaveRequest(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Leave request deleted successfully"));
    }
}
