package com.ct08.PharmacyManagement.modules.hr.controller;

import com.ct08.PharmacyManagement.common.dto.ApiResponse;
import com.ct08.PharmacyManagement.modules.hr.dto.EmployeeResponse;
import com.ct08.PharmacyManagement.modules.hr.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import org.springframework.web.multipart.MultipartFile;
import com.ct08.PharmacyManagement.modules.hr.dto.EmployeeUpdateRequest;
import java.io.IOException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

        @Autowired
        private EmployeeService employeeService;

        @GetMapping("/salary")
        @PreAuthorize("hasAnyRole('ADMIN', 'HM')")
        public ResponseEntity<ApiResponse<Map<String, Object>>> getEmployeeSalaryList(
                        Authentication authentication,
                        @RequestParam(defaultValue = "id") String sortBy,
                        @RequestParam(defaultValue = "asc") String order,
                        @RequestParam(required = false) Integer id,
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) BigDecimal minSalary,
                        @RequestParam(required = false) BigDecimal maxSalary) {
                Map<String, Object> result = employeeService.getEmployeeSalaryList(authentication, sortBy, order, id,
                                name,
                                status, minSalary, maxSalary);
                return ResponseEntity.ok(ApiResponse.success(result, "Employee salary list retrieved successfully"));
        }

        @GetMapping
        @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'HM')")
        public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getEmployees(
                        Authentication authentication,
                        @RequestParam(defaultValue = "id") String sortBy,
                        @RequestParam(defaultValue = "asc") String order,
                        @RequestParam(required = false) Integer id,
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) String phone,
                        @RequestParam(required = false) String email,
                        @RequestParam(required = false) String role,
                        @RequestParam(required = false) String status) {
                List<EmployeeResponse> employees = employeeService.getEmployees(authentication, sortBy, order, id, name,
                                phone,
                                email, role, status);
                return ResponseEntity.ok(ApiResponse.success(employees, "Employee list retrieved successfully"));
        }

        @GetMapping("/me/career-history")
        public ResponseEntity<ApiResponse<List<com.ct08.PharmacyManagement.modules.hr.dto.CareerHistoryDTO>>> getMyCareerHistory() {
                List<com.ct08.PharmacyManagement.modules.hr.dto.CareerHistoryDTO> history = employeeService
                                .getMyCareerHistory();
                return ResponseEntity.ok(ApiResponse.success(history, "Career history retrieved successfully"));
        }

        @GetMapping("/{id}/career-history")
        @PreAuthorize("hasAnyRole('ADMIN', 'HM')")
        public ResponseEntity<ApiResponse<List<com.ct08.PharmacyManagement.modules.hr.dto.CareerHistoryDTO>>> getEmployeeCareerHistory(
                        @PathVariable Integer id, Authentication authentication) {
                List<com.ct08.PharmacyManagement.modules.hr.dto.CareerHistoryDTO> history = employeeService
                                .getCareerHistoryByEmployeeId(id, authentication);
                return ResponseEntity.ok(ApiResponse.success(history, "Career history retrieved successfully"));
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN', 'HM')")
        public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeDetail(@PathVariable Integer id,
                        Authentication authentication) {
                EmployeeResponse employee = employeeService.getEmployeeDetail(id, authentication);
                return ResponseEntity.ok(ApiResponse.success(employee, "Employee details retrieved successfully"));
        }

        @PutMapping(value = "/{id}", consumes = { "multipart/form-data" })
        @PreAuthorize("hasAnyRole('ADMIN', 'HM')")
        public ResponseEntity<ApiResponse<String>> updateEmployee(
                        @PathVariable Integer id,
                        @Valid @RequestPart("data") EmployeeUpdateRequest request,
                        @RequestPart(value = "image", required = false) MultipartFile image,
                        Authentication authentication) throws IOException {
                employeeService.updateEmployee(id, request, image, authentication);
                return ResponseEntity.ok(
                                ApiResponse.success("Employee updated successfully", "Employee updated successfully"));
        }

        @PostMapping(consumes = { "multipart/form-data" })
        @PreAuthorize("hasAnyRole('ADMIN', 'HM')")
        public ResponseEntity<ApiResponse<String>> createEmployee(
                        @Valid @RequestPart("data") com.ct08.PharmacyManagement.modules.hr.dto.EmployeeCreationRequest request,
                        @RequestPart(value = "image", required = false) MultipartFile image,
                        Authentication authentication) throws IOException {
                employeeService.createEmployee(request, image, authentication);
                return ResponseEntity.ok(
                                ApiResponse.success("Employee created successfully", "Employee created successfully"));
        }

        @PutMapping(value = "/me", consumes = { "multipart/form-data" })
        public ResponseEntity<ApiResponse<String>> updateMyProfile(
                        @Valid @RequestPart("data") com.ct08.PharmacyManagement.modules.hr.dto.EmployeeProfileUpdateRequest request,
                        @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
                Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                                .getContext()
                                .getAuthentication();
                employeeService.updateMyProfile(request, image, authentication);
                return ResponseEntity.ok(
                                ApiResponse.success("Profile updated successfully", "Profile updated successfully"));
        }

        @PostMapping("/{id}/resign")
        @PreAuthorize("hasAnyRole('ADMIN', 'HM')")
        public ResponseEntity<ApiResponse<String>> resignEmployee(
                        @PathVariable Integer id,
                        @RequestBody @Valid com.ct08.PharmacyManagement.modules.hr.dto.ResignationRequest request) {
                Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                                .getContext()
                                .getAuthentication();
                employeeService.resignEmployee(id, request, authentication);
                return ResponseEntity
                                .ok(ApiResponse.success("Employee resigned successfully",
                                                "Employee resigned successfully"));
        }

        @PostMapping("/{id}/rehire")
        @PreAuthorize("hasAnyRole('ADMIN', 'HM')")
        public ResponseEntity<ApiResponse<String>> rehireEmployee(
                        @PathVariable Integer id,
                        @RequestBody @Valid com.ct08.PharmacyManagement.modules.hr.dto.RehireRequest request) {
                Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                                .getContext()
                                .getAuthentication();
                employeeService.rehireEmployee(id, request, authentication);
                return ResponseEntity
                                .ok(ApiResponse.success("Employee re-hired successfully",
                                                "Employee re-hired successfully"));
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN', 'HM')")
        public ResponseEntity<ApiResponse<String>> deleteWaitingEmployee(@PathVariable Integer id) {
                Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                                .getContext()
                                .getAuthentication();
                employeeService.deleteWaitingEmployee(id, authentication);
                return ResponseEntity.ok(
                                ApiResponse.success("Employee deleted successfully", "Employee deleted successfully"));
        }
}
