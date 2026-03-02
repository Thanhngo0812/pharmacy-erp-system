package com.ct08.PharmacyManagement.modules.hr.controller;

import com.ct08.PharmacyManagement.common.dto.ApiResponse;
import com.ct08.PharmacyManagement.modules.hr.dto.*;
import com.ct08.PharmacyManagement.modules.hr.service.PayrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hr/payroll")
public class PayrollController {

        @Autowired
        private PayrollService payrollService;

        // =====================================================
        // API 0: NV tự tra cứu lương (existing)
        // =====================================================

        /**
         * Nhân viên tra cứu lương của chính mình theo tháng/năm.
         * GET /api/v1/hr/payroll/my-salary?month=3&year=2026
         */
        @GetMapping("/my-salary")
        public ResponseEntity<ApiResponse<MySalaryResponseDTO>> getMySalary(
                        @RequestParam int month,
                        @RequestParam int year,
                        Authentication authentication) {
                MySalaryResponseDTO result = payrollService.calculateMySalary(month, year, authentication);
                return ResponseEntity.ok(ApiResponse.success(result, "Salary calculated successfully"));
        }

        // =====================================================
        // API 1: Bảng lương tháng toàn bộ NV
        // =====================================================

        /**
         * ADMIN/HM: Xem bảng lương tháng của tất cả NV.
         * HM chỉ xem NV có role WS/SS.
         * GET /api/v1/hr/payroll/monthly?month=3&year=2026
         */
        @GetMapping("/monthly")
        @PreAuthorize("hasAnyRole('ADMIN', 'HM')")
        public ResponseEntity<ApiResponse<MonthlyPayrollResponseDTO>> getMonthlyPayroll(
                        @RequestParam int month,
                        @RequestParam int year,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) Integer employeeId,
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false, defaultValue = "id") String sortBy,
                        @RequestParam(required = false, defaultValue = "asc") String order,
                        Authentication authentication) {

                MonthlyPayrollResponseDTO result = payrollService.calculateMonthlyPayroll(
                                month, year, status, employeeId, name, sortBy, order, authentication);
                return ResponseEntity.ok(ApiResponse.success(result, "Monthly payroll calculated successfully"));
        }

        // =====================================================
        // API 2: Chi tiết lương 1 NV
        // =====================================================

        /**
         * ADMIN/HM: Xem chi tiết lương 1 NV theo tháng.
         * GET /api/v1/hr/payroll/monthly/{employeeId}?month=3&year=2026
         */
        @GetMapping("/monthly/{employeeId}")
        @PreAuthorize("hasAnyRole('ADMIN', 'HM')")
        public ResponseEntity<ApiResponse<EmployeePayrollDetailDTO>> getEmployeePayrollDetail(
                        @PathVariable Integer employeeId,
                        @RequestParam int month,
                        @RequestParam int year,
                        Authentication authentication) {

                EmployeePayrollDetailDTO result = payrollService.calculateEmployeePayrollDetail(
                                employeeId, month, year, authentication);
                return ResponseEntity
                                .ok(ApiResponse.success(result, "Employee payroll detail calculated successfully"));
        }

        // =====================================================
        // API 3: Thống kê quỹ lương
        // =====================================================

        /**
         * ADMIN: Thống kê quỹ lương qua khoảng thời gian.
         * GET
         * /api/v1/hr/payroll/summary?fromMonth=1&fromYear=2026&toMonth=3&toYear=2026
         */
        @GetMapping("/summary")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<List<PayrollSummaryDTO>>> getPayrollSummary(
                        @RequestParam int fromMonth,
                        @RequestParam int fromYear,
                        @RequestParam int toMonth,
                        @RequestParam int toYear,
                        Authentication authentication) {

                List<PayrollSummaryDTO> result = payrollService.getPayrollSummary(
                                fromMonth, fromYear, toMonth, toYear, authentication);
                return ResponseEntity.ok(ApiResponse.success(result, "Payroll summary retrieved successfully"));
        }

        // =====================================================
        // API 4: Export bảng lương CSV
        // =====================================================

        /**
         * ADMIN: Xuất bảng lương tháng ra CSV.
         * GET /api/v1/hr/payroll/monthly/export?month=3&year=2026
         */
        @GetMapping("/monthly/export")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<byte[]> exportMonthlyPayroll(
                        @RequestParam int month,
                        @RequestParam int year,
                        Authentication authentication) {

                String csv = payrollService.exportMonthlyPayrollCsv(month, year, authentication);
                byte[] csvBytes = csv.getBytes(java.nio.charset.StandardCharsets.UTF_8);

                // Thêm UTF-8 BOM để Excel nhận diện tiếng Việt
                byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
                byte[] result = new byte[bom.length + csvBytes.length];
                System.arraycopy(bom, 0, result, 0, bom.length);
                System.arraycopy(csvBytes, 0, result, bom.length, csvBytes.length);

                String filename = "payroll_" + year + "_" + month + ".csv";

                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                                .body(result);
        }

        // =====================================================
        // API 5: Export bảng lương PDF
        // =====================================================

        /**
         * ADMIN: Xuất bảng lương tháng ra PDF (sử dụng Jasper Reports).
         * GET /api/v1/hr/payroll/monthly/export/pdf?month=3&year=2026
         */
        @GetMapping("/monthly/export/pdf")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<byte[]> exportMonthlyPayrollPdf(
                        @RequestParam int month,
                        @RequestParam int year,
                        Authentication authentication) {

                byte[] pdfBytes = payrollService.exportMonthlyPayrollPdf(month, year, authentication);

                String filename = "bang_luong_thang_" + month + "_" + year + ".pdf";

                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                                .contentType(MediaType.APPLICATION_PDF)
                                .body(pdfBytes);
        }
}
