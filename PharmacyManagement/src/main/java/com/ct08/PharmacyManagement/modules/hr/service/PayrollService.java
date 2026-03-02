package com.ct08.PharmacyManagement.modules.hr.service;

import com.ct08.PharmacyManagement.common.exception.BadRequestException;
import com.ct08.PharmacyManagement.common.exception.ResourceNotFoundException;
import com.ct08.PharmacyManagement.modules.auth.entity.Users;
import com.ct08.PharmacyManagement.modules.auth.repository.UsersRepository;
import com.ct08.PharmacyManagement.modules.hr.dto.*;
import com.ct08.PharmacyManagement.modules.hr.entity.Bonus;
import com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges;
import com.ct08.PharmacyManagement.modules.hr.entity.Employees;
import com.ct08.PharmacyManagement.modules.hr.entity.LeaveRequests;
import com.ct08.PharmacyManagement.modules.hr.repository.BonusRepository;
import com.ct08.PharmacyManagement.modules.hr.repository.BonusToggleHistoryRepository;
import com.ct08.PharmacyManagement.modules.hr.repository.CareerChangesRepository;
import com.ct08.PharmacyManagement.modules.hr.repository.EmployeesRepository;
import com.ct08.PharmacyManagement.modules.hr.repository.LeaveRequestsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final UsersRepository usersRepository;
    private final EmployeesRepository employeesRepository;
    private final CareerChangesRepository careerChangesRepository;
    private final BonusRepository bonusRepository;
    private final BonusToggleHistoryRepository bonusToggleHistoryRepository;
    private final LeaveRequestsRepository leaveRequestsRepository;

    // =====================================================
    // API 0: NV tự tra cứu lương (existing)
    // =====================================================

    /**
     * Tính lương cho chính nhân viên đang đăng nhập, theo tháng/năm.
     */
    public MySalaryResponseDTO calculateMySalary(int month, int year, Authentication authentication) {
        validateMonthYear(month, year);

        String username = authentication.getName();
        Users currentUser = usersRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Employees employee = currentUser.getEmployee();
        if (employee == null) {
            throw new BadRequestException("No employee record linked to this account");
        }

        Integer employeeId = employee.getId();
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate firstDay = yearMonth.atDay(1);
        LocalDate lastDay = yearMonth.atEndOfMonth();
        int daysInMonth = yearMonth.lengthOfMonth();

        BigDecimal baseSalary = calculateBaseSalary(employeeId, firstDay, lastDay, daysInMonth);

        int unpaidLeaveDays = calculateUnpaidLeaveDays(employeeId, firstDay, lastDay);
        BigDecimal dailyRate = baseSalary.divide(BigDecimal.valueOf(daysInMonth), 0, RoundingMode.HALF_UP);
        BigDecimal leaveDeduction = dailyRate.multiply(BigDecimal.valueOf(unpaidLeaveDays));

        List<BonusDetailDTO> bonusDetails = calculateBonuses(employeeId, firstDay, lastDay);
        BigDecimal totalBonus = bonusDetails.stream()
                .map(BonusDetailDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSalary = baseSalary.subtract(leaveDeduction).add(totalBonus);

        String fullName = buildFullName(employee);

        return MySalaryResponseDTO.builder()
                .employeeId(employeeId)
                .fullName(fullName)
                .positionName(employee.getCurrentPosition() != null
                        ? employee.getCurrentPosition().getPositionName()
                        : null)
                .month(month)
                .year(year)
                .baseSalary(baseSalary)
                .unpaidLeaveDays(unpaidLeaveDays)
                .leaveDeduction(leaveDeduction)
                .bonuses(bonusDetails)
                .totalBonus(totalBonus)
                .totalSalary(totalSalary)
                .build();
    }

    // =====================================================
    // API 1: Bảng lương tháng toàn bộ NV
    // =====================================================

    /**
     * Tính bảng lương tháng cho tất cả NV.
     * ADMIN: xem tất cả. HM: chỉ NV có role WS/SS.
     */
    public MonthlyPayrollResponseDTO calculateMonthlyPayroll(int month, int year,
            String status, Integer employeeId, String name,
            String sortBy, String order,
            Authentication authentication) {

        validateMonthYear(month, year);

        // Phân quyền
        Set<String> currentRoles = getRoles(authentication);
        boolean isAdmin = currentRoles.contains("ROLE_ADMIN");
        boolean isHM = currentRoles.contains("ROLE_HM");
        if (!isAdmin && !isHM) {
            throw new AccessDeniedException("You do not have permission to view payroll");
        }

        // Lấy danh sách nhân viên
        List<Employees> employees = getEmployeesForRole(isAdmin);

        // Lọc
        if (status != null && !status.isEmpty()) {
            Employees.EmployeeStatus filterStatus = Employees.EmployeeStatus.valueOf(status);
            employees = employees.stream()
                    .filter(e -> e.getStatus() == filterStatus)
                    .collect(Collectors.toList());
        }
        if (employeeId != null) {
            employees = employees.stream()
                    .filter(e -> e.getId().equals(employeeId))
                    .collect(Collectors.toList());
        }
        if (name != null && !name.isEmpty()) {
            String lowerName = name.toLowerCase();
            employees = employees.stream()
                    .filter(e -> buildFullName(e).toLowerCase().contains(lowerName))
                    .collect(Collectors.toList());
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate firstDay = yearMonth.atDay(1);
        LocalDate lastDay = yearMonth.atEndOfMonth();
        int daysInMonth = yearMonth.lengthOfMonth();

        // Tính lương cho từng NV
        List<EmployeePayrollDTO> payrollList = employees.stream()
                .map(emp -> buildEmployeePayroll(emp, firstDay, lastDay, daysInMonth))
                .filter(dto -> dto.getBaseSalary().compareTo(BigDecimal.ZERO) > 0
                        || dto.getTotalBonus().compareTo(BigDecimal.ZERO) != 0)
                .collect(Collectors.toList());

        // Sắp xếp
        Comparator<EmployeePayrollDTO> comparator = getPayrollComparator(sortBy);
        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }
        payrollList.sort(comparator);

        // Tạo summary
        BigDecimal totalPayroll = payrollList.stream()
                .map(EmployeePayrollDTO::getTotalSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAllowance = payrollList.stream()
                .map(EmployeePayrollDTO::getTotalAllowance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPenalty = payrollList.stream()
                .map(EmployeePayrollDTO::getTotalPenalty)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalBonus = payrollList.stream()
                .map(EmployeePayrollDTO::getTotalBonus)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDeduction = payrollList.stream()
                .map(EmployeePayrollDTO::getLeaveDeduction)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Thu thập tất cả các loại bonus name unique trong tháng để làm cột động
        List<String> allBonusNames = payrollList.stream()
                .flatMap(dto -> dto.getBonuses().stream())
                .map(BonusDetailDTO::getBonusName)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        return MonthlyPayrollResponseDTO.builder()
                .month(month)
                .year(year)
                .summary(MonthlyPayrollResponseDTO.PayrollSummarySectionDTO.builder()
                        .totalEmployees(payrollList.size())
                        .totalPayroll(totalPayroll)
                        .totalAllowance(totalAllowance)
                        .totalPenalty(totalPenalty)
                        .totalBonus(totalBonus)
                        .totalDeduction(totalDeduction)
                        .build())
                .employees(payrollList)
                .allBonusNames(allBonusNames)
                .build();
    }

    // =====================================================
    // API 2: Chi tiết lương 1 NV
    // =====================================================

    /**
     * Tính chi tiết lương 1 NV theo tháng, bao gồm salary changes, leave details,
     * bonus details.
     */
    public EmployeePayrollDetailDTO calculateEmployeePayrollDetail(Integer empId, int month, int year,
            Authentication authentication) {

        validateMonthYear(month, year);

        // Phân quyền
        Set<String> currentRoles = getRoles(authentication);
        boolean isAdmin = currentRoles.contains("ROLE_ADMIN");
        boolean isHM = currentRoles.contains("ROLE_HM");
        if (!isAdmin && !isHM) {
            throw new AccessDeniedException("You do not have permission to view payroll detail");
        }

        Employees employee = employeesRepository.findById(empId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + empId));

        // HM chỉ xem NV có role WS/SS
        if (!isAdmin && isHM) {
            validateHMAccess(empId);
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate firstDay = yearMonth.atDay(1);
        LocalDate lastDay = yearMonth.atEndOfMonth();
        int daysInMonth = yearMonth.lengthOfMonth();

        // Salary changes detail
        List<EmployeePayrollDetailDTO.SalaryPeriodDTO> salaryPeriods = buildSalaryPeriods(
                empId, firstDay, lastDay, daysInMonth);
        BigDecimal baseSalary = calculateBaseSalary(empId, firstDay, lastDay, daysInMonth);

        // Leave details
        List<EmployeePayrollDetailDTO.LeaveDetailDTO> leaveDetails = buildLeaveDetails(
                empId, firstDay, lastDay);
        int unpaidLeaveDays = calculateUnpaidLeaveDays(empId, firstDay, lastDay);
        BigDecimal leaveDeduction = calculatePreciseLeaveDeduction(empId, firstDay, lastDay, daysInMonth);

        // Bonus details
        List<BonusDetailDTO> bonusDetails = calculateBonuses(empId, firstDay, lastDay);
        BigDecimal totalBonus = bonusDetails.stream()
                .map(BonusDetailDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSalary = baseSalary.subtract(leaveDeduction).add(totalBonus);

        return EmployeePayrollDetailDTO.builder()
                .employeeId(empId)
                .fullName(buildFullName(employee))
                .positionName(employee.getCurrentPosition() != null
                        ? employee.getCurrentPosition().getPositionName()
                        : null)
                .month(month)
                .year(year)
                .baseSalary(baseSalary)
                .salaryChanges(salaryPeriods)
                .unpaidLeaveDays(unpaidLeaveDays)
                .leaveDeduction(leaveDeduction)
                .leaveDetails(leaveDetails)
                .bonuses(bonusDetails)
                .totalBonus(totalBonus)
                .totalSalary(totalSalary)
                .build();
    }

    // =====================================================
    // API 3: Thống kê quỹ lương
    // =====================================================

    /**
     * Thống kê quỹ lương qua khoảng thời gian (nhiều tháng).
     * Chỉ ADMIN.
     */
    public List<PayrollSummaryDTO> getPayrollSummary(int fromMonth, int fromYear,
            int toMonth, int toYear,
            Authentication authentication) {

        Set<String> currentRoles = getRoles(authentication);
        if (!currentRoles.contains("ROLE_ADMIN")) {
            throw new AccessDeniedException("Only ADMIN can view payroll summary");
        }

        List<PayrollSummaryDTO> summaries = new ArrayList<>();

        YearMonth start = YearMonth.of(fromYear, fromMonth);
        YearMonth end = YearMonth.of(toYear, toMonth);

        if (start.isAfter(end)) {
            throw new BadRequestException("Start date must be before or equal to end date");
        }

        // Lấy tất cả NV (ADMIN xem tất cả)
        List<Employees> allEmployees = employeesRepository.findAll();

        YearMonth current = start;
        while (!current.isAfter(end)) {
            LocalDate firstDay = current.atDay(1);
            LocalDate lastDay = current.atEndOfMonth();
            int daysInMonth = current.lengthOfMonth();

            BigDecimal monthTotalPayroll = BigDecimal.ZERO;
            BigDecimal monthTotalAllowance = BigDecimal.ZERO;
            BigDecimal monthTotalPenalty = BigDecimal.ZERO;
            BigDecimal monthTotalBonus = BigDecimal.ZERO;
            BigDecimal monthTotalDeduction = BigDecimal.ZERO;
            int monthTotalEmployees = 0;

            for (Employees emp : allEmployees) {
                BigDecimal baseSalary = calculateBaseSalary(emp.getId(), firstDay, lastDay, daysInMonth);
                if (baseSalary.compareTo(BigDecimal.ZERO) == 0)
                    continue;

                int unpaidLeaveDays = calculateUnpaidLeaveDays(emp.getId(), firstDay, lastDay);
                BigDecimal dailyRate = baseSalary.divide(BigDecimal.valueOf(daysInMonth), 0, RoundingMode.HALF_UP);
                BigDecimal leaveDeduction = dailyRate.multiply(BigDecimal.valueOf(unpaidLeaveDays));

                List<BonusDetailDTO> bonuses = calculateBonuses(emp.getId(), firstDay, lastDay);
                BigDecimal totalAllowance = bonuses.stream()
                        .map(BonusDetailDTO::getAmount)
                        .filter(a -> a.compareTo(BigDecimal.ZERO) >= 0)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalPenalty = bonuses.stream()
                        .map(BonusDetailDTO::getAmount)
                        .filter(a -> a.compareTo(BigDecimal.ZERO) < 0)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalBonus = totalAllowance.add(totalPenalty);

                BigDecimal totalSalary = baseSalary.subtract(leaveDeduction).add(totalBonus);

                monthTotalPayroll = monthTotalPayroll.add(totalSalary);
                monthTotalAllowance = monthTotalAllowance.add(totalAllowance);
                monthTotalPenalty = monthTotalPenalty.add(totalPenalty);
                monthTotalBonus = monthTotalBonus.add(totalBonus);
                monthTotalDeduction = monthTotalDeduction.add(leaveDeduction);
                monthTotalEmployees++;
            }

            summaries.add(PayrollSummaryDTO.builder()
                    .month(current.getMonthValue())
                    .year(current.getYear())
                    .totalEmployees(monthTotalEmployees)
                    .totalPayroll(monthTotalPayroll)
                    .totalAllowance(monthTotalAllowance)
                    .totalPenalty(monthTotalPenalty)
                    .totalBonus(monthTotalBonus)
                    .totalDeduction(monthTotalDeduction)
                    .build());

            current = current.plusMonths(1);
        }

        return summaries;
    }

    // =====================================================
    // API 4: Export CSV
    // =====================================================

    /**
     * Xuất bảng lương tháng ra CSV string.
     * Chỉ ADMIN.
     */
    public String exportMonthlyPayrollCsv(int month, int year, Authentication authentication) {
        // Kiểm tra quyền ADMIN
        Set<String> currentRoles = getRoles(authentication);
        if (!currentRoles.contains("ROLE_ADMIN")) {
            throw new AccessDeniedException("Only ADMIN can export payroll");
        }

        // Lấy danh sách NV
        List<Employees> employees = employeesRepository.findAll().stream()
                .sorted(Comparator.comparing(Employees::getId))
                .collect(Collectors.toList());

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate firstDay = yearMonth.atDay(1);
        LocalDate lastDay = yearMonth.atEndOfMonth();
        int daysInMonth = yearMonth.lengthOfMonth();

        StringBuilder sb = new StringBuilder();
        // Header
        sb.append("Mã NV,Họ tên,Chức vụ,Lương cơ bản,Ngày nghỉ KL,Khấu trừ nghỉ,");
        sb.append("Tên khoản thưởng/phạt,Số tiền thưởng/phạt,");
        sb.append("Tổng trợ cấp,Lương thực nhận\n");

        BigDecimal grandTotalPayroll = BigDecimal.ZERO;
        BigDecimal grandTotalBonus = BigDecimal.ZERO;
        BigDecimal grandTotalDeduction = BigDecimal.ZERO;
        int totalEmpCount = 0;

        for (Employees emp : employees) {
            BigDecimal baseSalary = calculateBaseSalary(emp.getId(), firstDay, lastDay, daysInMonth);
            if (baseSalary.compareTo(BigDecimal.ZERO) == 0)
                continue;

            int unpaidLeaveDays = calculateUnpaidLeaveDays(emp.getId(), firstDay, lastDay);
            BigDecimal leaveDeduction = calculatePreciseLeaveDeduction(emp.getId(), firstDay, lastDay, daysInMonth);

            List<BonusDetailDTO> bonuses = calculateBonuses(emp.getId(), firstDay, lastDay);
            BigDecimal totalBonus = bonuses.stream()
                    .map(BonusDetailDTO::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalSalary = baseSalary.subtract(leaveDeduction).add(totalBonus);

            String fullName = buildFullName(emp);
            String positionName = emp.getCurrentPosition() != null
                    ? emp.getCurrentPosition().getPositionName()
                    : "";

            if (bonuses.isEmpty()) {
                // NV không có bonus → 1 dòng duy nhất
                sb.append(emp.getId()).append(",");
                sb.append("\"").append(fullName).append("\",");
                sb.append("\"").append(positionName).append("\",");
                sb.append(baseSalary).append(",");
                sb.append(unpaidLeaveDays).append(",");
                sb.append(leaveDeduction).append(",");
                sb.append(","); // Tên khoản (trống)
                sb.append(","); // Số tiền khoản (trống)
                sb.append(totalBonus).append(",");
                sb.append(totalSalary).append("\n");
            } else {
                // Dòng đầu tiên — kèm bonus đầu tiên
                BonusDetailDTO firstBonus = bonuses.get(0);
                sb.append(emp.getId()).append(",");
                sb.append("\"").append(fullName).append("\",");
                sb.append("\"").append(positionName).append("\",");
                sb.append(baseSalary).append(",");
                sb.append(unpaidLeaveDays).append(",");
                sb.append(leaveDeduction).append(",");
                sb.append("\"").append(firstBonus.getBonusName()).append("\",");
                sb.append(firstBonus.getAmount()).append(",");
                sb.append(totalBonus).append(",");
                sb.append(totalSalary).append("\n");

                // Các dòng bonus tiếp theo
                for (int i = 1; i < bonuses.size(); i++) {
                    BonusDetailDTO bonus = bonuses.get(i);
                    sb.append(",,,,,"); // Các cột chính để trống
                    sb.append(","); // Khấu trừ nghỉ để trống
                    sb.append("\"").append(bonus.getBonusName()).append("\",");
                    sb.append(bonus.getAmount()).append(",");
                    sb.append(",\n"); // Tổng + Lương thực nhận để trống
                }
            }

            grandTotalPayroll = grandTotalPayroll.add(totalSalary);
            grandTotalBonus = grandTotalBonus.add(totalBonus);
            grandTotalDeduction = grandTotalDeduction.add(leaveDeduction);
            totalEmpCount++;
        }

        // Footer summary
        sb.append("\n");
        sb.append("Tổng nhân viên:,").append(totalEmpCount).append("\n");
        sb.append("Tổng quỹ lương:,").append(grandTotalPayroll).append("\n");
        sb.append("Tổng trợ cấp:,").append(grandTotalBonus).append("\n");
        sb.append("Tổng khấu trừ:,").append(grandTotalDeduction).append("\n");

        return sb.toString();
    }

    /**
     * Xuất bảng lương tháng ra PDF (sử dụng Jasper Reports).
     * Yêu cầu file template: src/main/resources/reports/monthly_payroll.jrxml
     */
    public byte[] exportMonthlyPayrollPdf(int month, int year, Authentication authentication) {
        // 1. Kiểm tra phân quyền (tương tự CSV/tính lương) và lấy dữ liệu
        Set<String> currentRoles = getRoles(authentication);
        if (!currentRoles.contains("ROLE_ADMIN")) {
            throw new AccessDeniedException("Only ADMIN can export payroll PDF");
        }

        MonthlyPayrollResponseDTO payrollData = calculateMonthlyPayroll(month, year, null, null, null, "id", "asc",
                authentication);

        try {
            // 2. Load template từ resources
            java.io.InputStream reportStream = getClass().getResourceAsStream("/reports/monthly_payroll.jrxml");
            if (reportStream == null) {
                throw new RuntimeException("Không tìm thấy file template mẫu: /reports/monthly_payroll.jrxml");
            }

            // Biên dịch JRXML thành file Jasper
            net.sf.jasperreports.engine.JasperReport jasperReport = net.sf.jasperreports.engine.JasperCompileManager
                    .compileReport(reportStream);

            // 3. Chuẩn bị Parameters
            java.util.Map<String, Object> parameters = new java.util.HashMap<>();
            parameters.put("month", month);
            parameters.put("year", year);
            if (payrollData.getSummary() != null) {
                parameters.put("totalEmployees", payrollData.getSummary().getTotalEmployees());
                parameters.put("totalPayroll", payrollData.getSummary().getTotalPayroll());
                parameters.put("totalBonus", payrollData.getSummary().getTotalBonus());
                parameters.put("totalDeduction", payrollData.getSummary().getTotalDeduction());
            }

            // 4. Cung cấp Dữ liệu (datasource) cho các Detail band (Danh sách nhân viên)
            net.sf.jasperreports.engine.data.JRBeanCollectionDataSource dataSource = new net.sf.jasperreports.engine.data.JRBeanCollectionDataSource(
                    payrollData.getEmployees());

            // 5. Đổ dữ liệu vào report
            net.sf.jasperreports.engine.JasperPrint jasperPrint = net.sf.jasperreports.engine.JasperFillManager
                    .fillReport(jasperReport, parameters, dataSource);

            // 6. Xuất PDF thành mảng byte
            return net.sf.jasperreports.engine.JasperExportManager.exportReportToPdf(jasperPrint);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi trong quá trình tạo PDF: " + e.getMessage(), e);
        }
    }

    // =====================================================
    // Private helper methods — Tính toán lương
    // =====================================================

    /**
     * Lấy mức lương cơ bản áp dụng cho tháng từ Career_Changes.
     * Nếu giữa tháng có thay đổi lương → tính tỷ lệ theo ngày.
     */
    private BigDecimal calculateBaseSalary(Integer employeeId, LocalDate firstDay, LocalDate lastDay,
            int daysInMonth) {
        List<CareerChanges> changes = careerChangesRepository.findByEmployeeIdOrderByIdDesc(employeeId);

        List<CareerChanges> appliedChanges = changes.stream()
                .filter(c -> c.getStatus() == CareerChanges.ApprovalStatus.Approved)
                .filter(c -> Boolean.TRUE.equals(c.getIsApplied()))
                .filter(c -> c.getEffectiveDate() != null)
                .sorted((a, b) -> a.getEffectiveDate().compareTo(b.getEffectiveDate()))
                .collect(Collectors.toList());

        if (appliedChanges.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<CareerChanges> changesInMonth = appliedChanges.stream()
                .filter(c -> !c.getEffectiveDate().isBefore(firstDay) && !c.getEffectiveDate().isAfter(lastDay))
                .collect(Collectors.toList());

        BigDecimal salaryAtStartOfMonth = BigDecimal.ZERO;
        for (CareerChanges c : appliedChanges) {
            if (c.getEffectiveDate().isBefore(firstDay) || c.getEffectiveDate().isEqual(firstDay)) {
                salaryAtStartOfMonth = c.getNewSalary() != null ? c.getNewSalary() : BigDecimal.ZERO;
            }
        }

        if (changesInMonth.isEmpty()) {
            return salaryAtStartOfMonth;
        }

        BigDecimal totalSalary = BigDecimal.ZERO;
        BigDecimal currentRate = salaryAtStartOfMonth;
        LocalDate currentStart = firstDay;

        for (CareerChanges c : changesInMonth) {
            long daysBefore = ChronoUnit.DAYS.between(currentStart, c.getEffectiveDate());
            if (daysBefore > 0) {
                BigDecimal portion = currentRate.multiply(BigDecimal.valueOf(daysBefore))
                        .divide(BigDecimal.valueOf(daysInMonth), 0, RoundingMode.HALF_UP);
                totalSalary = totalSalary.add(portion);
            }
            currentRate = c.getNewSalary() != null ? c.getNewSalary() : currentRate;
            currentStart = c.getEffectiveDate();
        }

        long daysRemaining = ChronoUnit.DAYS.between(currentStart, lastDay) + 1;
        BigDecimal remainingPortion = currentRate.multiply(BigDecimal.valueOf(daysRemaining))
                .divide(BigDecimal.valueOf(daysInMonth), 0, RoundingMode.HALF_UP);
        totalSalary = totalSalary.add(remainingPortion);

        return totalSalary;
    }

    /**
     * Tính số ngày nghỉ không lương (status = Approved, NOT Approved_Salary).
     */
    private int calculateUnpaidLeaveDays(Integer employeeId, LocalDate firstDay, LocalDate lastDay) {
        List<LeaveRequests> unpaidLeaves = leaveRequestsRepository
                .findByEmployeeIdAndStatus(employeeId, LeaveRequests.ApprovalStatus.Approved);

        int totalDays = 0;
        LocalDateTime monthStart = firstDay.atStartOfDay();
        LocalDateTime monthEnd = lastDay.atTime(23, 59, 59);

        for (LeaveRequests leave : unpaidLeaves) {
            LocalDateTime overlapStart = leave.getStartDate().isBefore(monthStart) ? monthStart
                    : leave.getStartDate();
            LocalDateTime overlapEnd = leave.getEndDate().isAfter(monthEnd) ? monthEnd : leave.getEndDate();

            if (!overlapStart.isAfter(overlapEnd)) {
                long days = ChronoUnit.DAYS.between(overlapStart.toLocalDate(), overlapEnd.toLocalDate()) + 1;
                totalDays += (int) days;
            }
        }
        return totalDays;
    }

    /**
     * Tính số tiền khấu trừ nghỉ không lương chính xác theo ngày.
     * Nếu lương thay đổi giữa tháng, ngày nghỉ ở mức lương nào sẽ trừ theo mức
     * lương đó.
     */
    private BigDecimal calculatePreciseLeaveDeduction(Integer employeeId, LocalDate firstDay, LocalDate lastDay,
            int daysInMonth) {
        if (daysInMonth <= 0)
            return BigDecimal.ZERO;

        List<LeaveRequests> unpaidLeaves = leaveRequestsRepository
                .findByEmployeeIdAndStatus(employeeId, LeaveRequests.ApprovalStatus.Approved);
        if (unpaidLeaves.isEmpty())
            return BigDecimal.ZERO;

        List<CareerChanges> changes = careerChangesRepository.findByEmployeeIdOrderByIdDesc(employeeId);
        List<CareerChanges> appliedChanges = changes.stream()
                .filter(c -> c.getStatus() == CareerChanges.ApprovalStatus.Approved)
                .filter(c -> Boolean.TRUE.equals(c.getIsApplied()))
                .filter(c -> c.getEffectiveDate() != null)
                .sorted(Comparator.comparing(CareerChanges::getEffectiveDate))
                .collect(Collectors.toList());

        BigDecimal totalDeduction = BigDecimal.ZERO;

        for (LeaveRequests leave : unpaidLeaves) {
            LocalDate start = leave.getStartDate().toLocalDate().isBefore(firstDay) ? firstDay
                    : leave.getStartDate().toLocalDate();
            LocalDate end = leave.getEndDate().toLocalDate().isAfter(lastDay) ? lastDay
                    : leave.getEndDate().toLocalDate();

            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                BigDecimal currentRate = BigDecimal.ZERO;
                for (CareerChanges c : appliedChanges) {
                    if (!c.getEffectiveDate().isAfter(date)) {
                        currentRate = c.getNewSalary() != null ? c.getNewSalary() : currentRate;
                    }
                }
                BigDecimal dailyPortion = currentRate.divide(BigDecimal.valueOf(daysInMonth), 0, RoundingMode.HALF_UP);
                totalDeduction = totalDeduction.add(dailyPortion);
            }
        }
        return totalDeduction;
    }

    /**
     * Tính số ngày nghỉ có lương (status = Approved_Salary).
     */
    private int calculatePaidLeaveDays(Integer employeeId, LocalDate firstDay, LocalDate lastDay) {
        List<LeaveRequests> paidLeaves = leaveRequestsRepository
                .findByEmployeeIdAndStatus(employeeId, LeaveRequests.ApprovalStatus.Approved_Salary);

        int totalDays = 0;
        LocalDateTime monthStart = firstDay.atStartOfDay();
        LocalDateTime monthEnd = lastDay.atTime(23, 59, 59);

        for (LeaveRequests leave : paidLeaves) {
            LocalDateTime overlapStart = leave.getStartDate().isBefore(monthStart) ? monthStart : leave.getStartDate();
            LocalDateTime overlapEnd = leave.getEndDate().isAfter(monthEnd) ? monthEnd : leave.getEndDate();

            if (!overlapStart.isAfter(overlapEnd)) {
                long days = ChronoUnit.DAYS.between(overlapStart.toLocalDate(), overlapEnd.toLocalDate()) + 1;
                totalDays += (int) days;
            }
        }
        return totalDays;
    }

    /**
     * Tính tổng số ngày "Active" trong tháng (ngày có mức lương > 0).
     */
    private int calculateActiveDays(Integer employeeId, LocalDate firstDay, LocalDate lastDay, int daysInMonth) {
        List<CareerChanges> changes = careerChangesRepository.findByEmployeeIdOrderByIdDesc(employeeId);
        List<CareerChanges> appliedChanges = changes.stream()
                .filter(c -> c.getStatus() == CareerChanges.ApprovalStatus.Approved)
                .filter(c -> Boolean.TRUE.equals(c.getIsApplied()))
                .filter(c -> c.getEffectiveDate() != null)
                .sorted(Comparator.comparing(CareerChanges::getEffectiveDate))
                .collect(Collectors.toList());

        if (appliedChanges.isEmpty())
            return 0;

        BigDecimal salaryAtStartOfMonth = BigDecimal.ZERO;
        for (CareerChanges c : appliedChanges) {
            if (!c.getEffectiveDate().isAfter(firstDay)) {
                salaryAtStartOfMonth = c.getNewSalary() != null ? c.getNewSalary() : BigDecimal.ZERO;
            }
        }

        List<CareerChanges> changesInMonth = appliedChanges.stream()
                .filter(c -> !c.getEffectiveDate().isBefore(firstDay) && !c.getEffectiveDate().isAfter(lastDay))
                .collect(Collectors.toList());

        int totalActiveDays = 0;
        BigDecimal currentRate = salaryAtStartOfMonth;
        LocalDate currentStart = firstDay;

        for (CareerChanges c : changesInMonth) {
            long daysBefore = ChronoUnit.DAYS.between(currentStart, c.getEffectiveDate());
            if (daysBefore > 0 && currentRate.compareTo(BigDecimal.ZERO) > 0) {
                totalActiveDays += (int) daysBefore;
            }
            currentRate = c.getNewSalary() != null ? c.getNewSalary() : currentRate;
            currentStart = c.getEffectiveDate();
        }

        long daysRemaining = ChronoUnit.DAYS.between(currentStart, lastDay) + 1;
        if (daysRemaining > 0 && currentRate.compareTo(BigDecimal.ZERO) > 0) {
            totalActiveDays += (int) daysRemaining;
        }

        return totalActiveDays;
    }

    /**
     * Lấy danh sách trợ cấp/phạt áp dụng trong tháng.
     * Tính toán chính xác theo từng ngày (Daily Rate) dựa trên lịch sử Bật/Tắt.
     */
    private List<BonusDetailDTO> calculateBonuses(Integer employeeId, LocalDate firstDay, LocalDate lastDay) {
        List<Bonus> allBonuses = bonusRepository.findAll();

        return allBonuses.stream()
                .filter(b -> b.getEmployee().getId().equals(employeeId))
                .filter(b -> b.getStatus() == Bonus.ApprovalStatus.Approved)
                .filter(b -> !b.getStartDate().isAfter(lastDay))
                .filter(b -> b.getEndDate() == null || !b.getEndDate().isBefore(firstDay))
                .map(b -> {
                    LocalDate overlapStart = b.getStartDate().isBefore(firstDay) ? firstDay : b.getStartDate();
                    LocalDate overlapEnd = (b.getEndDate() == null || b.getEndDate().isAfter(lastDay)) ? lastDay
                            : b.getEndDate();

                    List<com.ct08.PharmacyManagement.modules.hr.entity.BonusToggleHistory> history = bonusToggleHistoryRepository
                            .findByBonus_IdOrderByToggledAtDesc(b.getId());

                    long activeDays = 0;
                    for (LocalDate date = overlapStart; !date.isAfter(overlapEnd); date = date.plusDays(1)) {
                        if (isBonusActiveAt(b, date, history)) {
                            activeDays++;
                        }
                    }

                    if (activeDays == 0)
                        return null;

                    BigDecimal totalAmount = b.getAmount().multiply(BigDecimal.valueOf(activeDays));
                    return BonusDetailDTO.builder()
                            .bonusId(b.getId())
                            .bonusName(b.getBonusName())
                            .amount(totalAmount)
                            .build();
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    /**
     * Kiểm tra xem tại một ngày cụ thể, Bonus có đang Active hay không.
     */
    private boolean isBonusActiveAt(Bonus bonus, LocalDate date,
            List<com.ct08.PharmacyManagement.modules.hr.entity.BonusToggleHistory> history) {
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        for (com.ct08.PharmacyManagement.modules.hr.entity.BonusToggleHistory entry : history) {
            if (entry.getToggledAt() != null && !entry.getToggledAt().isAfter(endOfDay)) {
                return Boolean.TRUE.equals(entry.getIsActive());
            }
        }
        return true;
    }

    // =====================================================
    // Private helper methods — Utility
    // =====================================================

    private void validateMonthYear(int month, int year) {
        if (month < 1 || month > 12) {
            throw new BadRequestException("Month must be between 1 and 12");
        }
        if (year < 2000 || year > 2100) {
            throw new BadRequestException("Year must be between 2000 and 2100");
        }
    }

    private Set<String> getRoles(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }
        return authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toSet());
    }

    private String buildFullName(Employees employee) {
        return ((employee.getLastName() != null ? employee.getLastName() : "")
                + " " + (employee.getFirstName() != null ? employee.getFirstName() : "")).trim();
    }

    /**
     * Lấy danh sách NV theo role: ADMIN xem tất cả, HM chỉ NV có role WS/SS.
     */
    private List<Employees> getEmployeesForRole(boolean isAdmin) {
        if (isAdmin) {
            return employeesRepository.findAll();
        }
        return employeesRepository.findEmployeesByUserRoles(Arrays.asList("ROLE_WS", "ROLE_SS"));
    }

    /**
     * Kiểm tra HM có quyền xem NV cụ thể không (NV phải có role WS hoặc SS).
     */
    private void validateHMAccess(Integer empId) {
        List<Employees> hmEmployees = employeesRepository
                .findEmployeesByUserRoles(Arrays.asList("ROLE_WS", "ROLE_SS"));
        boolean hasAccess = hmEmployees.stream()
                .anyMatch(e -> e.getId().equals(empId));
        if (!hasAccess) {
            throw new AccessDeniedException("You do not have permission to view this employee's payroll");
        }
    }

    private String getSalaryNote(Integer employeeId, LocalDate firstDay, LocalDate lastDay) {
        List<CareerChanges> changes = careerChangesRepository.findByEmployeeIdOrderByIdDesc(employeeId);
        List<CareerChanges> appliedChanges = changes.stream()
                .filter(c -> c.getStatus() == CareerChanges.ApprovalStatus.Approved)
                .filter(c -> Boolean.TRUE.equals(c.getIsApplied()))
                .filter(c -> c.getEffectiveDate() != null)
                .sorted(Comparator.comparing(CareerChanges::getEffectiveDate))
                .collect(Collectors.toList());

        BigDecimal salaryAtStartOfMonth = BigDecimal.ZERO;
        for (CareerChanges c : appliedChanges) {
            if (!c.getEffectiveDate().isAfter(firstDay)) {
                salaryAtStartOfMonth = c.getNewSalary() != null ? c.getNewSalary() : BigDecimal.ZERO;
            }
        }

        List<CareerChanges> changesInMonth = appliedChanges.stream()
                .filter(c -> !c.getEffectiveDate().isBefore(firstDay) && !c.getEffectiveDate().isAfter(lastDay))
                .collect(Collectors.toList());

        if (changesInMonth.isEmpty())
            return null;

        java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0");
        StringBuilder note = new StringBuilder();
        LocalDate currentStart = firstDay;
        BigDecimal currentRate = salaryAtStartOfMonth;

        for (CareerChanges c : changesInMonth) {
            LocalDate changeDate = c.getEffectiveDate();
            if (changeDate.isAfter(currentStart)) {
                note.append(currentStart.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")))
                        .append("-")
                        .append(changeDate.minusDays(1).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")))
                        .append(": ")
                        .append(df.format(currentRate))
                        .append("; ");
            }
            currentRate = c.getNewSalary() != null ? c.getNewSalary() : currentRate;
            currentStart = changeDate;
        }

        note.append(currentStart.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")))
                .append("-")
                .append(lastDay.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")))
                .append(": ")
                .append(df.format(currentRate));

        return note.toString();
    }

    /**
     * Tạo EmployeePayrollDTO cho 1 NV.
     */
    private EmployeePayrollDTO buildEmployeePayroll(Employees emp,
            LocalDate firstDay, LocalDate lastDay, int daysInMonth) {
        BigDecimal baseSalary = calculateBaseSalary(emp.getId(), firstDay, lastDay, daysInMonth);

        int unpaidLeaveDays = calculateUnpaidLeaveDays(emp.getId(), firstDay, lastDay);
        int paidLeaveDays = calculatePaidLeaveDays(emp.getId(), firstDay, lastDay);

        // Tính số ngày "Active" - những ngày có mức lương > 0 trong tháng (cho NV mới
        // vào/nghỉ việc)
        int activeDays = calculateActiveDays(emp.getId(), firstDay, lastDay, daysInMonth);
        int workingDays = activeDays - unpaidLeaveDays;

        BigDecimal leaveDeduction = calculatePreciseLeaveDeduction(emp.getId(), firstDay, lastDay, daysInMonth);

        List<BonusDetailDTO> bonuses = calculateBonuses(emp.getId(), firstDay, lastDay);
        BigDecimal totalAllowance = bonuses.stream()
                .map(BonusDetailDTO::getAmount)
                .filter(a -> a.compareTo(BigDecimal.ZERO) >= 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPenalty = bonuses.stream()
                .map(BonusDetailDTO::getAmount)
                .filter(a -> a.compareTo(BigDecimal.ZERO) < 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalBonus = totalAllowance.add(totalPenalty);

        BigDecimal totalSalary = baseSalary.subtract(leaveDeduction).add(totalBonus);

        return EmployeePayrollDTO.builder()
                .employeeId(emp.getId())
                .fullName(buildFullName(emp))
                .positionName(emp.getCurrentPosition() != null
                        ? emp.getCurrentPosition().getPositionName()
                        : null)
                .baseSalary(baseSalary)
                .salaryNote(getSalaryNote(emp.getId(), firstDay, lastDay))
                .workingDays(workingDays)
                .paidLeaveDays(paidLeaveDays)
                .unpaidLeaveDays(unpaidLeaveDays)
                .leaveDeduction(leaveDeduction)
                .bonuses(bonuses)
                .totalAllowance(totalAllowance)
                .totalPenalty(totalPenalty)
                .totalBonus(totalBonus)
                .totalSalary(totalSalary)
                .build();
    }

    /**
     * Xây dựng danh sách SalaryPeriodDTO — các giai đoạn lương trong tháng.
     */
    private List<EmployeePayrollDetailDTO.SalaryPeriodDTO> buildSalaryPeriods(
            Integer employeeId, LocalDate firstDay, LocalDate lastDay, int daysInMonth) {

        List<CareerChanges> changes = careerChangesRepository.findByEmployeeIdOrderByIdDesc(employeeId);
        List<CareerChanges> appliedChanges = changes.stream()
                .filter(c -> c.getStatus() == CareerChanges.ApprovalStatus.Approved)
                .filter(c -> Boolean.TRUE.equals(c.getIsApplied()))
                .filter(c -> c.getEffectiveDate() != null)
                .sorted(Comparator.comparing(CareerChanges::getEffectiveDate))
                .collect(Collectors.toList());

        List<EmployeePayrollDetailDTO.SalaryPeriodDTO> periods = new ArrayList<>();

        if (appliedChanges.isEmpty()) {
            return periods;
        }

        // Tìm lương đầu tháng
        BigDecimal salaryAtStart = BigDecimal.ZERO;
        for (CareerChanges c : appliedChanges) {
            if (c.getEffectiveDate().isBefore(firstDay) || c.getEffectiveDate().isEqual(firstDay)) {
                salaryAtStart = c.getNewSalary() != null ? c.getNewSalary() : BigDecimal.ZERO;
            }
        }

        // Tìm thay đổi trong tháng
        List<CareerChanges> changesInMonth = appliedChanges.stream()
                .filter(c -> !c.getEffectiveDate().isBefore(firstDay) && !c.getEffectiveDate().isAfter(lastDay))
                .collect(Collectors.toList());

        if (changesInMonth.isEmpty()) {
            // Không có thay đổi → 1 giai đoạn
            periods.add(EmployeePayrollDetailDTO.SalaryPeriodDTO.builder()
                    .fromDate(firstDay)
                    .toDate(lastDay)
                    .salary(salaryAtStart)
                    .days(daysInMonth)
                    .build());
        } else {
            BigDecimal currentRate = salaryAtStart;
            LocalDate periodStart = firstDay;

            for (CareerChanges c : changesInMonth) {
                if (c.getEffectiveDate().isAfter(periodStart)) {
                    int days = (int) ChronoUnit.DAYS.between(periodStart, c.getEffectiveDate());
                    if (days > 0) {
                        periods.add(EmployeePayrollDetailDTO.SalaryPeriodDTO.builder()
                                .fromDate(periodStart)
                                .toDate(c.getEffectiveDate().minusDays(1))
                                .salary(currentRate)
                                .days(days)
                                .build());
                    }
                }
                currentRate = c.getNewSalary() != null ? c.getNewSalary() : currentRate;
                periodStart = c.getEffectiveDate();
            }

            // Phần còn lại
            int remainingDays = (int) (ChronoUnit.DAYS.between(periodStart, lastDay) + 1);
            if (remainingDays > 0) {
                periods.add(EmployeePayrollDetailDTO.SalaryPeriodDTO.builder()
                        .fromDate(periodStart)
                        .toDate(lastDay)
                        .salary(currentRate)
                        .days(remainingDays)
                        .build());
            }
        }

        return periods;
    }

    /**
     * Xây dựng danh sách LeaveDetailDTO — chi tiết từng đơn nghỉ trong tháng.
     */
    private List<EmployeePayrollDetailDTO.LeaveDetailDTO> buildLeaveDetails(
            Integer employeeId, LocalDate firstDay, LocalDate lastDay) {

        // Lấy cả nghỉ không lương (Approved) và nghỉ có lương (Approved_Salary)
        List<LeaveRequests> leaves = leaveRequestsRepository.findByEmployeeIdAndStatusIn(
                employeeId,
                Arrays.asList(LeaveRequests.ApprovalStatus.Approved, LeaveRequests.ApprovalStatus.Approved_Salary));

        LocalDateTime monthStart = firstDay.atStartOfDay();
        LocalDateTime monthEnd = lastDay.atTime(23, 59, 59);

        return leaves.stream()
                .filter(leave -> {
                    // Lọc giao cắt với tháng
                    return !leave.getStartDate().isAfter(monthEnd) && !leave.getEndDate().isBefore(monthStart);
                })
                .map(leave -> {
                    LocalDateTime overlapStart = leave.getStartDate().isBefore(monthStart) ? monthStart
                            : leave.getStartDate();
                    LocalDateTime overlapEnd = leave.getEndDate().isAfter(monthEnd) ? monthEnd : leave.getEndDate();
                    int days = (int) (ChronoUnit.DAYS.between(overlapStart.toLocalDate(),
                            overlapEnd.toLocalDate()) + 1);

                    return EmployeePayrollDetailDTO.LeaveDetailDTO.builder()
                            .startDate(overlapStart.toLocalDate())
                            .endDate(overlapEnd.toLocalDate())
                            .days(days)
                            .type(leave.getStatus().name())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Comparator cho sắp xếp bảng lương.
     */
    private Comparator<EmployeePayrollDTO> getPayrollComparator(String sortBy) {
        if (sortBy == null)
            sortBy = "id";
        switch (sortBy.toLowerCase()) {
            case "name":
                return Comparator.comparing(EmployeePayrollDTO::getFullName,
                        Comparator.nullsLast(String::compareToIgnoreCase));
            case "totalsalary":
                return Comparator.comparing(EmployeePayrollDTO::getTotalSalary,
                        Comparator.nullsLast(BigDecimal::compareTo));
            case "basesalary":
                return Comparator.comparing(EmployeePayrollDTO::getBaseSalary,
                        Comparator.nullsLast(BigDecimal::compareTo));
            default:
                return Comparator.comparing(EmployeePayrollDTO::getEmployeeId,
                        Comparator.nullsLast(Integer::compareTo));
        }
    }
}
