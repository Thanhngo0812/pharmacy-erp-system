package com.ct08.PharmacyManagement.modules.hr.controller;

import com.ct08.PharmacyManagement.common.dto.ApiResponse;
import com.ct08.PharmacyManagement.modules.hr.dto.*;
import com.ct08.PharmacyManagement.modules.hr.service.BonusService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hr/bonuses")
public class BonusController {

    @Autowired
    private BonusService bonusService;

    /**
     * Lấy danh sách bonus gom nhóm theo (bonus_name, end_date, amount, status).
     * ADMIN: xem tất cả. HM: chỉ xem bonus của NV có role WS/SS.
     * Hỗ trợ tìm kiếm theo bonus_name và lọc theo status.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HM')")
    public ResponseEntity<ApiResponse<List<BonusGroupResponseDTO>>> getBonuses(
            @RequestParam(required = false) String bonusName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) java.math.BigDecimal minAmount,
            @RequestParam(required = false) java.math.BigDecimal maxAmount,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            Authentication authentication) {

        List<BonusGroupResponseDTO> result = bonusService.getBonusesGrouped(
                bonusName, status, minAmount, maxAmount, startDate, endDate, sortDirection, authentication);
        return ResponseEntity.ok(ApiResponse.success(result, "Fetched bonuses successfully"));
    }

    /**
     * Lấy danh sách nhân viên thỏa điều kiện hưởng trợ cấp (đang làm việc & vào làm
     * trước/trong khoảng thời gian trợ cấp).
     */
    @GetMapping("/eligible-employees")
    @PreAuthorize("hasAnyRole('ADMIN', 'HM')")
    public ResponseEntity<ApiResponse<List<com.ct08.PharmacyManagement.modules.hr.dto.EmployeeSalaryDTO>>> getEligibleEmployees(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate,
            Authentication authentication) {

        List<com.ct08.PharmacyManagement.modules.hr.dto.EmployeeSalaryDTO> result = bonusService
                .getEligibleEmployeesForBonus(startDate, endDate, authentication);
        return ResponseEntity.ok(ApiResponse.success(result, "Fetched eligible employees successfully"));
    }

    /**
     * Duyệt / Từ chối 1 khoản trợ cấp.
     */
    @PutMapping("/{id}/action")
    @PreAuthorize("hasAnyRole('ADMIN', 'HM')")
    public ResponseEntity<ApiResponse<Void>> approveRejectBonus(
            @PathVariable Integer id,
            @RequestBody @Valid BonusActionRequestDTO dto,
            Authentication authentication) {
        bonusService.approveRejectBonus(id, dto, authentication);
        return ResponseEntity.ok(ApiResponse.success(null, "Bonus action applied successfully"));
    }

    /**
     * Cập nhật thông tin (sửa lẻ) 1 khoản trợ cấp (tên, ngày kết thúc).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HM')")
    public ResponseEntity<ApiResponse<Void>> editSingleBonus(
            @PathVariable Integer id,
            @RequestBody @Valid BonusSingleEditRequestDTO dto,
            Authentication authentication) {
        bonusService.editSingleBonus(id, dto, authentication);
        return ResponseEntity.ok(ApiResponse.success(null, "Bonus updated successfully"));
    }

    /**
     * Duyệt / Từ chối nhiều khoản trợ cấp cùng lúc (Bulk Action).
     */
    @PutMapping("/bulk/action")
    @PreAuthorize("hasAnyRole('ADMIN', 'HM')")
    public ResponseEntity<ApiResponse<Void>> approveRejectBulkBonus(
            @RequestBody @Valid BonusBulkActionRequestDTO dto,
            Authentication authentication) {
        bonusService.approveRejectBulkBonus(dto, authentication);
        return ResponseEntity.ok(ApiResponse.success(null, "Bulk bonus action applied successfully"));
    }

    /**
     * Sửa chung nhiều khoản trợ cấp cùng lúc (Bulk Edit).
     */
    @PutMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'HM')")
    public ResponseEntity<ApiResponse<Void>> editBulkBonus(
            @RequestBody @Valid BonusBulkEditRequestDTO dto,
            Authentication authentication) {
        bonusService.editBulkBonus(dto, authentication);
        return ResponseEntity.ok(ApiResponse.success(null, "Bulk bonus updated successfully"));
    }

    /**
     * Xóa nhóm khoản trợ cấp bị từ chối (Bulk Delete).
     */
    @DeleteMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'HM')")
    public ResponseEntity<ApiResponse<Void>> deleteBulkBonus(
            @RequestParam List<Integer> ids,
            Authentication authentication) {
        bonusService.deleteBulkBonus(ids, authentication);
        return ResponseEntity.ok(ApiResponse.success(null, "Bulk bonus deleted successfully"));
    }

    /**
     * Tạo mới danh sách nhân viên cùng nhận 1 khoản trợ cấp.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HM')")
    public ResponseEntity<ApiResponse<Void>> createBonus(
            @Valid @RequestBody BonusCreateRequestDTO dto,
            Authentication authentication) {
        bonusService.createBonus(dto, authentication);
        return ResponseEntity.ok(ApiResponse.success(null, "Bonus created successfully"));
    }

    /**
     * Bật/Tắt hiển thị (Active/Inactive) nhóm trợ cấp.
     */
    @PutMapping("/bulk/toggle-active")
    @PreAuthorize("hasAnyRole('ADMIN', 'HM')")
    public ResponseEntity<ApiResponse<Void>> toggleBonusActive(
            @Valid @RequestBody BonusToggleRequestDTO dto,
            Authentication authentication) {
        bonusService.toggleBonusActive(dto, authentication);
        return ResponseEntity.ok(ApiResponse.success(null, "Bonus active status toggled successfully"));
    }

    /**
     * Lấy danh sách lịch sử bật/tắt của một nhóm phần thưởng cụ thể.
     */
    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'HM')")
    public ResponseEntity<ApiResponse<List<BonusToggleHistoryResponseDTO>>> getBonusHistory(
            @PathVariable Integer id,
            Authentication authentication) {
        List<BonusToggleHistoryResponseDTO> history = bonusService.getBonusToggleHistory(id, authentication);
        return ResponseEntity.ok(ApiResponse.success(history, "Fetched bonus history successfully"));
    }
}
