package com.ct08.PharmacyManagement.modules.hr.service;

import com.ct08.PharmacyManagement.common.exception.BadRequestException;
import com.ct08.PharmacyManagement.common.exception.ResourceNotFoundException;
import com.ct08.PharmacyManagement.modules.auth.entity.Users;
import com.ct08.PharmacyManagement.modules.auth.repository.UsersRepository;
import com.ct08.PharmacyManagement.modules.hr.dto.*;
import com.ct08.PharmacyManagement.modules.hr.entity.Bonus;
import com.ct08.PharmacyManagement.modules.hr.entity.BonusToggleHistory;
import com.ct08.PharmacyManagement.modules.hr.entity.Employees;
import com.ct08.PharmacyManagement.modules.hr.repository.BonusRepository;
import com.ct08.PharmacyManagement.modules.hr.repository.BonusToggleHistoryRepository;
import com.ct08.PharmacyManagement.modules.hr.repository.EmployeesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BonusService {

    private final BonusRepository bonusRepository;
    private final UsersRepository usersRepository;
    private final BonusToggleHistoryRepository toggleHistoryRepository;
    private final EmployeesRepository employeesRepository;

    /**
     * Lấy danh sách bonus gom nhóm theo (bonus_name, start_date, end_date, amount,
     * status).
     * ADMIN: xem tất cả. HM: chỉ xem bonus của NV có role WS/SS.
     * Hỗ trợ tìm kiếm nâng cao (amount, date range) và sắp xếp.
     */
    public List<BonusGroupResponseDTO> getBonusesGrouped(
            String bonusName, String status, BigDecimal minAmount, BigDecimal maxAmount,
            LocalDate searchStart, LocalDate searchEnd,
            String sortDirection, Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        Set<String> currentRoles = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toSet());

        boolean isAdmin = currentRoles.contains("ROLE_ADMIN");
        boolean isHM = currentRoles.contains("ROLE_HM");

        if (!isAdmin && !isHM) {
            throw new AccessDeniedException("You do not have permission to view bonuses");
        }

        // 1. Xây dựng Specification động
        Specification<Bonus> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // 1.1. Bonus Name
            if (bonusName != null && !bonusName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("bonusName")), "%" + bonusName.toLowerCase() + "%"));
            }

            // 1.2. Status
            if (status != null && !status.isBlank()) {
                try {
                    predicates.add(cb.equal(root.get("status"), Bonus.ApprovalStatus.valueOf(status)));
                } catch (IllegalArgumentException e) {
                }
            }

            // 1.3. Amount Range
            if (minAmount != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), minAmount));
            }
            if (maxAmount != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), maxAmount));
            }

            // 1.4. Date Range (Sub-range logic: bonus period is within search range [start,
            // end])
            if (searchStart != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), searchStart));
            }
            if (searchEnd != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), searchEnd));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        // 2. HM Role Restriction (Alternative if Criteria Join is hard)
        // Let's stick to the Spec for filtering attributes, but handle the role filter
        // carefully.

        // Actually, to keep it safe and functional within the current structure:
        Sort sort = "asc".equalsIgnoreCase(sortDirection)
                ? Sort.by("amount").ascending()
                : Sort.by("amount").descending();

        List<Bonus> bonuses = bonusRepository.findAll(spec, sort);

        // HM Filter (Post-processing to ensure SS/WS only if not Admin)
        if (isHM && !isAdmin) {
            bonuses = bonuses.stream().filter(b -> {
                try {
                    checkHmPermission(authentication, b);
                    return true;
                } catch (AccessDeniedException e) {
                    return false;
                }
            }).collect(Collectors.toList());
        }

        // Grouping logic (Group by name, start, end, amount, status)
        Map<String, List<Bonus>> grouped = new LinkedHashMap<>();
        for (Bonus b : bonuses) {
            String key = b.getBonusName() + "|" + b.getStartDate() + "|" + b.getEndDate() + "|" + b.getAmount() + "|"
                    + b.getStatus();
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(b);
        }

        // Convert to DTOs
        List<BonusGroupResponseDTO> result = new ArrayList<>();
        for (Map.Entry<String, List<Bonus>> entry : grouped.entrySet()) {
            List<Bonus> group = entry.getValue();
            Bonus representative = group.get(0);

            List<BonusEmployeeDTO> employeeDTOs = group.stream().map(b -> {
                String empName = "";
                if (b.getEmployee() != null) {
                    String lastName = b.getEmployee().getLastName() != null ? b.getEmployee().getLastName() : "";
                    String firstName = b.getEmployee().getFirstName() != null ? b.getEmployee().getFirstName() : "";
                    empName = (lastName + " " + firstName).trim();
                }
                String posName = null;
                if (b.getEmployee() != null && b.getEmployee().getCurrentPosition() != null) {
                    posName = b.getEmployee().getCurrentPosition().getPositionName();
                }
                return new BonusEmployeeDTO(
                        b.getId(),
                        b.getEmployee() != null ? b.getEmployee().getId() : null,
                        empName,
                        posName,
                        b.getIsActive(),
                        b.getStatus() != null ? b.getStatus().name() : null);
            }).collect(Collectors.toList());

            int activeCount = (int) employeeDTOs.stream()
                    .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                    .count();

            // proposedBy info
            Integer proposedById = null;
            String proposedByName = null;
            if (representative.getProposedBy() != null) {
                proposedById = representative.getProposedBy().getId();
                if (representative.getProposedBy().getEmployee() != null) {
                    proposedByName = (representative.getProposedBy().getEmployee().getLastName() != null
                            ? representative.getProposedBy().getEmployee().getLastName()
                            : "")
                            + " "
                            + (representative.getProposedBy().getEmployee().getFirstName() != null
                                    ? representative.getProposedBy().getEmployee().getFirstName()
                                    : "");
                    proposedByName = proposedByName.trim();
                }
            }

            // approvedBy info
            Integer approvedById = null;
            String approvedByName = null;
            if (representative.getApprovedBy() != null) {
                approvedById = representative.getApprovedBy().getId();
                if (representative.getApprovedBy().getEmployee() != null) {
                    approvedByName = (representative.getApprovedBy().getEmployee().getLastName() != null
                            ? representative.getApprovedBy().getEmployee().getLastName()
                            : "")
                            + " "
                            + (representative.getApprovedBy().getEmployee().getFirstName() != null
                                    ? representative.getApprovedBy().getEmployee().getFirstName()
                                    : "");
                    approvedByName = approvedByName.trim();
                }
            }

            BonusGroupResponseDTO dto = new BonusGroupResponseDTO(
                    representative.getBonusName(),
                    representative.getAmount(),
                    representative.getStartDate(),
                    representative.getEndDate(),
                    representative.getReason(),
                    representative.getStatus() != null ? representative.getStatus().name() : null,
                    representative.getApprovalReason(),
                    proposedById,
                    proposedByName,
                    approvedById,
                    approvedByName,
                    employeeDTOs.size(),
                    activeCount,
                    employeeDTOs);

            result.add(dto);
        }

        return result;
    }

    /**
     * Lấy danh sách nhân viên đủ điều kiện để hưởng phụ cấp/thưởng.
     * Điều kiện: Đang làm việc (Active) và có ngày nhận việc (hireDate) <=
     * startDate của khoản thưởng.
     */
    public List<com.ct08.PharmacyManagement.modules.hr.dto.EmployeeSalaryDTO> getEligibleEmployeesForBonus(
            LocalDate startDate, LocalDate endDate, Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        if (startDate == null) {
            throw new BadRequestException("startDate must not be null");
        }

        Set<String> currentRoles = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toSet());

        boolean isAdmin = currentRoles.contains("ROLE_ADMIN");
        boolean isHM = currentRoles.contains("ROLE_HM");

        if (!isAdmin && !isHM) {
            throw new AccessDeniedException("You do not have permission to view eligible employees");
        }

        List<Employees> allEmployees = employeesRepository.findAll();

        return allEmployees.stream()
                .filter(emp -> emp.getStatus() == Employees.EmployeeStatus.Active)
                .filter(emp -> emp.getHireDate() != null && !emp.getHireDate().isAfter(startDate))
                .filter(emp -> {
                    if (isAdmin)
                        return true;
                    // For HM, ensure employee user has role WS or SS
                    Users empUser = usersRepository.findByEmployeeId(emp.getId()).orElse(null);
                    return empUser != null && empUser.getRoles().stream()
                            .anyMatch(r -> r.getRoleName().equals("ROLE_WS") || r.getRoleName().equals("ROLE_SS"));
                })
                .map(emp -> new com.ct08.PharmacyManagement.modules.hr.dto.EmployeeSalaryDTO(emp))
                .collect(Collectors.toList());
    }

    private Users getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }
        return usersRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void checkHmPermission(Authentication authentication, Bonus bonus) {
        Set<String> currentRoles = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toSet());

        boolean isAdmin = currentRoles.contains("ROLE_ADMIN");
        if (isAdmin)
            return; // ADMIN can do anything

        boolean isHM = currentRoles.contains("ROLE_HM");
        if (!isHM) {
            throw new AccessDeniedException("You do not have permission to manage this bonus");
        }

        // Check if employee is WS or SS
        boolean employeeIsWsOrSs = false;
        if (bonus.getEmployee() != null) {
            Users employeeUser = usersRepository.findByEmployeeId(bonus.getEmployee().getId()).orElse(null);
            if (employeeUser != null) {
                employeeIsWsOrSs = employeeUser.getRoles().stream()
                        .anyMatch(r -> r.getRoleName().equals("ROLE_WS") || r.getRoleName().equals("ROLE_SS"));
            }
        }

        if (!employeeIsWsOrSs) {
            throw new AccessDeniedException("HM can only manage bonuses for WS and SS role employees");
        }
    }

    private void validateEndDate(LocalDate endDate, LocalDate startDate) {
        if (endDate != null) {
            if (startDate != null && endDate.isBefore(startDate)) {
                throw new BadRequestException("End date cannot be earlier than start date");
            }
        }
    }

    public void approveRejectBonus(Integer id, BonusActionRequestDTO dto, Authentication authentication) {
        Users currentUser = getCurrentUser(authentication);
        Bonus bonus = bonusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bonus not found with id: " + id));

        checkHmPermission(authentication, bonus);

        if (bonus.getStatus() != Bonus.ApprovalStatus.Pending) {
            throw new BadRequestException("Bonus is not in Pending status");
        }

        bonus.setStatus(dto.getStatus());
        bonus.setApprovalReason(dto.getApprovalReason());
        bonus.setApprovedBy(currentUser);
        bonusRepository.save(bonus);
    }

    public void approveRejectBulkBonus(BonusBulkActionRequestDTO dto, Authentication authentication) {
        Users currentUser = getCurrentUser(authentication);
        List<Bonus> bonuses = bonusRepository.findAllById(dto.getBonusIds());

        for (Bonus bonus : bonuses) {
            checkHmPermission(authentication, bonus);
            if (bonus.getStatus() == Bonus.ApprovalStatus.Pending) {
                bonus.setStatus(dto.getStatus());
                bonus.setApprovalReason(dto.getApprovalReason());
                bonus.setApprovedBy(currentUser);
            }
        }
        bonusRepository.saveAll(bonuses);
    }

    public void editSingleBonus(Integer id, BonusSingleEditRequestDTO dto, Authentication authentication) {
        Bonus bonus = bonusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bonus not found with id: " + id));

        checkHmPermission(authentication, bonus);
        validateEndDate(dto.getEndDate(), bonus.getStartDate());

        bonus.setBonusName(dto.getBonusName());
        bonus.setEndDate(dto.getEndDate());
        bonusRepository.save(bonus);
    }

    public void editBulkBonus(BonusBulkEditRequestDTO dto, Authentication authentication) {
        List<Bonus> bonuses = bonusRepository.findAllById(dto.getBonusIds());

        for (Bonus bonus : bonuses) {
            checkHmPermission(authentication, bonus);
            validateEndDate(dto.getEndDate(), bonus.getStartDate());
            bonus.setBonusName(dto.getBonusName());
            bonus.setEndDate(dto.getEndDate());
        }
        bonusRepository.saveAll(bonuses);
    }

    public void deleteBulkBonus(List<Integer> bonusIds, Authentication authentication) {
        if (bonusIds == null || bonusIds.isEmpty()) {
            throw new BadRequestException("List of bonus IDs cannot be empty");
        }

        List<Bonus> bonuses = bonusRepository.findAllById(bonusIds);

        for (Bonus bonus : bonuses) {
            checkHmPermission(authentication, bonus);
            if (bonus.getStatus() != Bonus.ApprovalStatus.Rejected) {
                throw new BadRequestException(
                        "Cannot delete bonus with ID " + bonus.getId() + " because it is not in Rejected status");
            }
        }
        bonusRepository.deleteAll(bonuses);
    }

    public void createBonus(BonusCreateRequestDTO dto, Authentication authentication) {
        if (dto.getEmployeeIds() == null || dto.getEmployeeIds().isEmpty()) {
            throw new BadRequestException("List of employee IDs cannot be empty");
        }

        validateEndDate(dto.getEndDate(), dto.getStartDate());

        Users currentUser = getCurrentUser(authentication);
        boolean isHM = currentUser.getRoles().stream()
                .anyMatch(r -> r.getRoleName().equals("ROLE_HM"));

        List<Employees> employees = employeesRepository.findAllById(dto.getEmployeeIds());
        List<Bonus> newBonuses = new ArrayList<>();

        for (Employees emp : employees) {
            // Check HM permission for each employee
            if (isHM) {
                Users empUser = usersRepository.findByEmployeeId(emp.getId()).orElse(null);
                if (empUser == null || empUser.getRoles().stream()
                        .noneMatch(r -> r.getRoleName().equals("ROLE_WS")
                                || r.getRoleName().equals("ROLE_SS"))) {
                    throw new AccessDeniedException("HM can only create bonuses for SS or WS employees");
                }
            }

            Bonus bonus = new Bonus();
            bonus.setEmployee(emp);
            bonus.setBonusName(dto.getBonusName());
            bonus.setAmount(dto.getAmount());
            bonus.setStartDate(dto.getStartDate());
            bonus.setEndDate(dto.getEndDate());
            bonus.setReason(dto.getReason());
            bonus.setIsActive(true); // Default active state
            bonus.setProposedBy(currentUser);

            if (!isHM) {
                // Admin creates -> Auto Approve
                bonus.setStatus(Bonus.ApprovalStatus.Approved);
                bonus.setApprovedBy(currentUser);
                bonus.setApprovalReason("Auto-approved by System Admin");
            } else {
                // HM creates -> Pending
                bonus.setStatus(Bonus.ApprovalStatus.Pending);
            }

            newBonuses.add(bonus);
        }

        bonusRepository.saveAll(newBonuses);
    }

    public void toggleBonusActive(BonusToggleRequestDTO dto, Authentication authentication) {
        if (dto.getBonusIds() == null || dto.getBonusIds().isEmpty()) {
            throw new BadRequestException("List of bonus IDs cannot be empty");
        }

        Users currentUser = getCurrentUser(authentication);
        List<Bonus> bonuses = bonusRepository.findAllById(dto.getBonusIds());
        List<BonusToggleHistory> historyLogs = new ArrayList<>();

        for (Bonus bonus : bonuses) {
            checkHmPermission(authentication, bonus);

            // Update status
            bonus.setIsActive(dto.getIsActive());

            // Add history
            BonusToggleHistory history = new BonusToggleHistory();
            history.setBonus(bonus);
            history.setIsActive(dto.getIsActive());
            history.setReason(dto.getReason());
            history.setToggledBy(currentUser);
            historyLogs.add(history);
        }

        bonusRepository.saveAll(bonuses);
        toggleHistoryRepository.saveAll(historyLogs);
    }

    public List<BonusToggleHistoryResponseDTO> getBonusToggleHistory(Integer bonusId, Authentication authentication) {
        Bonus bonus = bonusRepository.findById(bonusId)
                .orElseThrow(() -> new ResourceNotFoundException("Bonus not found with ID: " + bonusId));

        checkHmPermission(authentication, bonus);

        List<BonusToggleHistory> historyList = toggleHistoryRepository.findByBonus_IdOrderByToggledAtDesc(bonusId);

        return historyList.stream().map(history -> BonusToggleHistoryResponseDTO.builder()
                .bonusId(history.getBonus().getId())
                .isActive(history.getIsActive())
                .toggledAt(history.getToggledAt())
                .toggledById(history.getToggledBy() != null ? history.getToggledBy().getId() : null)
                .toggledByName(history.getToggledBy() != null ? history.getToggledBy().getUsername() : null)
                .reason(history.getReason())
                .build()).collect(Collectors.toList());
    }

    // =========================================================================================
    // AUTO TOGGLE BONUS LOGIC FOR RESIGN / REHIRE
    // =========================================================================================

    /**
     * Vô hiệu hóa tất cả các khoản thưởng/phạt đang hoạt động của một nhân viên.
     * Được gọi tự động khi nhân viên nghỉ việc (Resign).
     */
    public void disableAllActiveBonuses(Integer employeeId, Users systemUser, String reason) {
        List<Bonus> activeBonuses = bonusRepository.findAll().stream()
                .filter(b -> b.getEmployee().getId().equals(employeeId))
                .filter(b -> b.getStatus() == Bonus.ApprovalStatus.Approved)
                .filter(b -> Boolean.TRUE.equals(b.getIsActive()))
                .collect(Collectors.toList());

        if (activeBonuses.isEmpty())
            return;

        List<BonusToggleHistory> historyLogs = new ArrayList<>();
        for (Bonus bonus : activeBonuses) {
            bonus.setIsActive(false);

            BonusToggleHistory history = new BonusToggleHistory();
            history.setBonus(bonus);
            history.setIsActive(false);
            history.setReason(reason);
            history.setToggledBy(systemUser);
            historyLogs.add(history);
        }

        bonusRepository.saveAll(activeBonuses);
        toggleHistoryRepository.saveAll(historyLogs);
    }

    /**
     * Khôi phục lại các khoản thưởng/phạt đã bị vô hiệu hóa do nhân viên nghỉ việc.
     * Được gọi tự động khi nhân viên đi làm lại (Rehire).
     */
    public void restoreBonuses(Integer employeeId, Users systemUser, String reason) {
        List<Bonus> inactiveBonuses = bonusRepository.findAll().stream()
                .filter(b -> b.getEmployee().getId().equals(employeeId))
                .filter(b -> b.getStatus() == Bonus.ApprovalStatus.Approved)
                .filter(b -> Boolean.FALSE.equals(b.getIsActive()))
                .collect(Collectors.toList());

        if (inactiveBonuses.isEmpty())
            return;

        List<Bonus> bonusesToRestore = new ArrayList<>();
        List<BonusToggleHistory> historyLogs = new ArrayList<>();

        for (Bonus bonus : inactiveBonuses) {
            List<BonusToggleHistory> history = toggleHistoryRepository
                    .findByBonus_IdOrderByToggledAtDesc(bonus.getId());
            if (!history.isEmpty()) {
                BonusToggleHistory lastChange = history.get(0);
                if (Boolean.FALSE.equals(lastChange.getIsActive()) &&
                        lastChange.getReason() != null &&
                        lastChange.getReason().contains("Tự động tắt do nhân viên nghỉ việc")) {

                    bonus.setIsActive(true);
                    bonusesToRestore.add(bonus);

                    BonusToggleHistory newHistory = new BonusToggleHistory();
                    newHistory.setBonus(bonus);
                    newHistory.setIsActive(true);
                    newHistory.setReason(reason);
                    newHistory.setToggledBy(systemUser);
                    historyLogs.add(newHistory);
                }
            }
        }

        if (!bonusesToRestore.isEmpty()) {
            bonusRepository.saveAll(bonusesToRestore);
            toggleHistoryRepository.saveAll(historyLogs);
        }
    }
}
