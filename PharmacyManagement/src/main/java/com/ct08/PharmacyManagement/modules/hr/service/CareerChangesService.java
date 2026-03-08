package com.ct08.PharmacyManagement.modules.hr.service;

import com.ct08.PharmacyManagement.common.event.PasswordEmailEvent;
import com.ct08.PharmacyManagement.common.exception.ConflictException;
import com.ct08.PharmacyManagement.common.exception.ResourceNotFoundException;
import com.ct08.PharmacyManagement.common.infra.message.MessageProducerService;
import com.ct08.PharmacyManagement.modules.auth.entity.Users;
import com.ct08.PharmacyManagement.modules.auth.repository.UsersRepository;
import com.ct08.PharmacyManagement.modules.hr.dto.ApprovalRequest;
import com.ct08.PharmacyManagement.modules.hr.dto.HiredCareerChangeResponse;
import com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges;
import com.ct08.PharmacyManagement.modules.hr.entity.Employees;
import com.ct08.PharmacyManagement.modules.hr.repository.CareerChangesRepository;
import com.ct08.PharmacyManagement.modules.hr.repository.CareerChangesSpecification;
import com.ct08.PharmacyManagement.modules.hr.repository.EmployeesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CareerChangesService {

    @Autowired
    private CareerChangesRepository careerChangesRepository;

    @Autowired
    private EmployeesRepository employeesRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private MessageProducerService messageProducerService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BonusService bonusService;

    public List<HiredCareerChangeResponse> getHiredCareerChanges(String sortBy, String order, String status,
            Integer id, String employeeName, Integer proposedById,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        Set<String> currentRoles = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toSet());

        boolean isAdmin = currentRoles.contains("ROLE_ADMIN");
        boolean isHR = currentRoles.contains("ROLE_HR") || currentRoles.contains("ROLE_HM");

        if (!isAdmin && !isHR) {
            throw new AccessDeniedException("You do not have permission to view hired career changes");
        }

        Sort sort = createSort(sortBy, order);
        Specification<CareerChanges> spec = CareerChangesSpecification.filterByHiredAndStatus(status, id, employeeName,
                proposedById);

        List<CareerChanges> changes = careerChangesRepository.findAll(spec, sort);

        return changes.stream()
                .map(HiredCareerChangeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<com.ct08.PharmacyManagement.modules.hr.dto.CareerHistoryDTO> getCareerChanges(
            String sortBy, String order, String status,
            Integer id, Integer employeeId, String changeType, Integer proposedById,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        Set<String> currentRoles = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toSet());

        boolean isAdmin = currentRoles.contains("ROLE_ADMIN");
        boolean isHM = currentRoles.contains("ROLE_HM");

        if (!isAdmin && !isHM) {
            throw new AccessDeniedException("You do not have permission to view career changes");
        }

        Sort sort = createSort(sortBy, order);
        Specification<CareerChanges> spec = CareerChangesSpecification.filterByNonHired(
                status, id, employeeId, changeType, proposedById);

        List<CareerChanges> changes = careerChangesRepository.findAll(spec, sort);

        return changes.stream()
                .map(com.ct08.PharmacyManagement.modules.hr.dto.CareerHistoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<com.ct08.PharmacyManagement.modules.hr.dto.CareerHistoryDTO> getMyCareerChanges(
            String sortBy, String order, String status,
            Integer id, Integer employeeId, String changeType, Authentication authentication) {

        Users currentUser = usersRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return getCareerChanges(sortBy, order, status, id, employeeId, changeType, currentUser.getId(), authentication);
    }

    @Transactional
    public void approveOrRejectHiredCareerChange(Integer id, ApprovalRequest request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        Set<String> currentRoles = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toSet());

        if (!currentRoles.contains("ROLE_ADMIN")) {
            throw new AccessDeniedException("Only Admin can approve or reject hired records");
        }

        Users currentUser = usersRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Approver user not found"));

        CareerChanges careerChange = careerChangesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Career change record not found"));

        if (careerChange.getChangeType() != CareerChanges.ChangeType.Hired) {
            throw new ConflictException("Can only process 'Hired' career changes via this endpoint");
        }

        if (careerChange.getStatus() != CareerChanges.ApprovalStatus.Pending) {
            throw new ConflictException(
                    "Career change is already processed (Status: " + careerChange.getStatus() + ")");
        }

        Employees employee = careerChange.getEmployee();
        if (employee == null) {
            throw new ResourceNotFoundException("Associated employee not found");
        }

        Users targetUser = usersRepository.findByEmployeeId(employee.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Associated user not found"));

        if (request.getIsApproved()) {
            careerChange.setStatus(CareerChanges.ApprovalStatus.Approved);
            careerChange.setIsApplied(true);
            employee.setStatus(Employees.EmployeeStatus.Active);

            targetUser.setIsActive(true);
            targetUser.setMailStatus("sending");

            String newPassword = generateRandomPassword();
            targetUser.setPasswordHash(passwordEncoder.encode(newPassword));

            String fullName = employee.getLastName() + " " + employee.getFirstName();
            messageProducerService.sendMessage("user-password-email",
                    new PasswordEmailEvent(
                            targetUser.getId(), employee.getEmail(), fullName, newPassword));
        } else {
            careerChange.setStatus(CareerChanges.ApprovalStatus.Rejected);
            employee.setStatus(Employees.EmployeeStatus.Rejected);
            // targetUser remains inactive
        }

        careerChange.setApprovedBy(currentUser);
        careerChange.setApprovalReason(request.getReason());
        careerChangesRepository.save(careerChange);
        employeesRepository.save(employee);
        usersRepository.save(targetUser);
    }

    @Autowired
    private com.ct08.PharmacyManagement.modules.hr.repository.PositionsRepository positionsRepository;

    @Transactional
    public void createCareerChange(com.ct08.PharmacyManagement.modules.hr.dto.CareerChangeRequest request,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        // Parse and validate changeType
        CareerChanges.ChangeType changeType;
        try {
            changeType = CareerChanges.ChangeType.valueOf(request.getChangeType());
        } catch (IllegalArgumentException e) {
            throw new com.ct08.PharmacyManagement.common.exception.BadRequestException(
                    "Invalid changeType: " + request.getChangeType()
                            + ". Allowed: Salary_Increase, Promotion, Promotion_With_Salary, other");
        }

        // Block Hired type
        if (changeType == CareerChanges.ChangeType.Hired) {
            throw new com.ct08.PharmacyManagement.common.exception.BadRequestException(
                    "Cannot create 'Hired' via this endpoint. Use the dedicated API.");
        }

        // Auth & role check
        Users currentUser = usersRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Set<String> currentRoles = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toSet());

        boolean isAdmin = currentRoles.contains("ROLE_ADMIN");
        boolean isHM = currentRoles.contains("ROLE_HM");

        if (!isAdmin && !isHM) {
            throw new AccessDeniedException("You do not have permission to create career changes");
        }

        // Find target employee
        Employees employee = employeesRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (changeType == CareerChanges.ChangeType.Rehired) {
            if (employee.getStatus() != Employees.EmployeeStatus.Resigned) {
                throw new ConflictException("Chỉ có thể đề xuất làm lại cho nhân viên đang nghỉ việc");
            }
        } else {
            if (employee.getStatus() != Employees.EmployeeStatus.Active) {
                throw new ConflictException("Chỉ có thể đề xuất biến động cho nhân viên đang Active");
            }
        }

        // HM can only propose for WS/SS employees
        if (isHM && !isAdmin) {
            Users targetUser = usersRepository.findByEmployeeId(employee.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Associated user not found"));
            boolean isTargetAllowed = targetUser.getRoles().stream()
                    .anyMatch(role -> "ROLE_WS".equals(role.getRoleName()) || "ROLE_SS".equals(role.getRoleName()));
            boolean hasHigherRole = targetUser.getRoles().stream()
                    .anyMatch(role -> "ROLE_ADMIN".equals(role.getRoleName())
                            || "ROLE_HM".equals(role.getRoleName()) || "ROLE_WM".equals(role.getRoleName()));
            if (!isTargetAllowed || hasHigherRole) {
                throw new AccessDeniedException(
                        "HR Manager chỉ có thể đề xuất biến động cho nhân viên có role WS hoặc SS");
            }
        }

        // Validate effectiveDate
        if (request.getEffectiveDate().isBefore(java.time.LocalDate.now())) {
            throw new com.ct08.PharmacyManagement.common.exception.BadRequestException(
                    "Ngày hiệu lực không được nhỏ hơn ngày hôm nay");
        }

        java.util.List<CareerChanges> history = careerChangesRepository.findByEmployeeIdOrderByIdDesc(employee.getId());
        for (CareerChanges cc : history) {
            if (cc.getStatus() != CareerChanges.ApprovalStatus.Rejected) {
                if (request.getEffectiveDate().isBefore(cc.getEffectiveDate())) {
                    throw new com.ct08.PharmacyManagement.common.exception.BadRequestException(
                            "Ngày hiệu lực không được nhỏ hơn ngày hiệu lực của đề xuất gần nhất ("
                                    + cc.getEffectiveDate() + ")");
                }
                break;
            }
        }

        // Validate fields based on changeType
        switch (changeType) {
            case Salary_Increase:
                if (request.getNewSalary() == null) {
                    throw new com.ct08.PharmacyManagement.common.exception.BadRequestException(
                            "newSalary is required for Salary_Increase");
                }
                if (request.getNewSalary().compareTo(employee.getCurrentSalary()) <= 0) {
                    throw new com.ct08.PharmacyManagement.common.exception.BadRequestException(
                            "newSalary must be greater than current salary (" + employee.getCurrentSalary() + ")");
                }
                break;
            case Promotion:
                if (request.getNewPositionName() == null || request.getNewPositionName().isBlank()) {
                    throw new com.ct08.PharmacyManagement.common.exception.BadRequestException(
                            "newPositionName is required for Promotion");
                }
                break;
            case Promotion_With_Salary:
                if (request.getNewSalary() == null) {
                    throw new com.ct08.PharmacyManagement.common.exception.BadRequestException(
                            "newSalary is required for Promotion_With_Salary");
                }
                if (request.getNewPositionName() == null || request.getNewPositionName().isBlank()) {
                    throw new com.ct08.PharmacyManagement.common.exception.BadRequestException(
                            "newPositionName is required for Promotion_With_Salary");
                }
                if (request.getNewSalary().compareTo(employee.getCurrentSalary()) <= 0) {
                    throw new com.ct08.PharmacyManagement.common.exception.BadRequestException(
                            "newSalary must be greater than current salary (" + employee.getCurrentSalary() + ")");
                }
                break;
            case Resigned:
                // Không bắt buộc field gì
                break;
            case Rehired:
                if (request.getNewSalary() == null && employee.getCurrentSalary() == null) {
                    throw new com.ct08.PharmacyManagement.common.exception.BadRequestException(
                            "newSalary is required for Rehired if employee has no current salary record");
                }
                break;
            default: // 'other' — flexible, no strict validation
                break;
        }

        // Resolve new position if provided
        com.ct08.PharmacyManagement.modules.hr.entity.Positions newPosition = null;
        if (request.getNewPositionName() != null && !request.getNewPositionName().isBlank()) {
            newPosition = positionsRepository.findByPositionName(request.getNewPositionName())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Position not found: " + request.getNewPositionName()));
            if (newPosition
                    .getStatus() != com.ct08.PharmacyManagement.modules.hr.entity.Positions.ApprovalStatus.Approved) {
                throw new ConflictException("Position '" + request.getNewPositionName() + "' chưa được duyệt");
            }
            // For Promotion types, ensure it's a different position
            if (changeType == CareerChanges.ChangeType.Promotion
                    || changeType == CareerChanges.ChangeType.Promotion_With_Salary) {
                if (employee.getCurrentPosition() != null
                        && employee.getCurrentPosition().getId().equals(newPosition.getId())) {
                    throw new com.ct08.PharmacyManagement.common.exception.BadRequestException(
                            "newPosition must be different from current position");
                }
            }
        }

        // Build CareerChanges entity
        CareerChanges change = new CareerChanges();
        change.setEmployee(employee);
        change.setChangeType(changeType);
        change.setOldSalary(changeType == CareerChanges.ChangeType.Rehired ? java.math.BigDecimal.ZERO
                : employee.getCurrentSalary());
        change.setNewSalary(
                changeType == CareerChanges.ChangeType.Resigned ? java.math.BigDecimal.ZERO : request.getNewSalary());
        change.setOldPosition(changeType == CareerChanges.ChangeType.Rehired ? null : employee.getCurrentPosition());
        change.setNewPosition(changeType == CareerChanges.ChangeType.Resigned ? null : newPosition);
        change.setEffectiveDate(request.getEffectiveDate());
        change.setReason(request.getReason());
        change.setProposedBy(currentUser);

        if (isAdmin) {
            // ADMIN: auto-approve and apply changes immediately
            change.setStatus(CareerChanges.ApprovalStatus.Approved);
            change.setApprovedBy(currentUser);
            change.setApprovalReason("Auto-approved by Admin");

            if (!request.getEffectiveDate().isAfter(java.time.LocalDate.now())) {
                applyCareerChange(employee, change);
                change.setIsApplied(true);
            } else {
                change.setIsApplied(false);
            }
            employeesRepository.save(employee);
        } else {
            // HM: pending, wait for ADMIN approval
            change.setStatus(CareerChanges.ApprovalStatus.Pending);
            change.setApprovedBy(null);
        }

        careerChangesRepository.save(change);
    }

    @Transactional
    public void approveOrRejectCareerChange(Integer id, ApprovalRequest request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        Set<String> currentRoles = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toSet());

        if (!currentRoles.contains("ROLE_ADMIN")) {
            throw new AccessDeniedException("Only Admin can approve or reject career changes");
        }

        Users currentUser = usersRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Approver user not found"));

        CareerChanges careerChange = careerChangesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Career change record not found"));

        // Block Hired type — use dedicated endpoint
        if (careerChange.getChangeType() == CareerChanges.ChangeType.Hired) {
            throw new ConflictException(
                    "Use the dedicated /api/v1/career-changes/hired/{id}/action endpoint for Hired type");
        }

        if (careerChange.getStatus() != CareerChanges.ApprovalStatus.Pending) {
            throw new ConflictException(
                    "Career change is already processed (Status: " + careerChange.getStatus() + ")");
        }

        Employees employee = careerChange.getEmployee();
        if (employee == null) {
            throw new ResourceNotFoundException("Associated employee not found");
        }

        if (request.getIsApproved()) {
            careerChange.setStatus(CareerChanges.ApprovalStatus.Approved);

            if (!careerChange.getEffectiveDate().isAfter(java.time.LocalDate.now())) {
                applyCareerChange(employee, careerChange);
                careerChange.setIsApplied(true);
            } else {
                careerChange.setIsApplied(false);
            }
        } else {
            careerChange.setStatus(CareerChanges.ApprovalStatus.Rejected);
        }

        careerChange.setApprovedBy(currentUser);
        careerChange.setApprovalReason(request.getReason());
        careerChangesRepository.save(careerChange);
        employeesRepository.save(employee);
    }

    public void applyCareerChange(Employees employee, CareerChanges careerChange) {
        if (careerChange.getChangeType() == CareerChanges.ChangeType.Resigned) {
            employee.setStatus(Employees.EmployeeStatus.Resigned);
            usersRepository.findByEmployeeId(employee.getId()).ifPresent(u -> {
                u.setIsActive(false);
                usersRepository.save(u);
            });
            bonusService.disableAllActiveBonuses(employee.getId(), careerChange.getApprovedBy(),
                    "Tự động tắt do nhân viên nghỉ việc: " + careerChange.getReason());
        } else if (careerChange.getChangeType() == CareerChanges.ChangeType.Rehired) {
            employee.setStatus(Employees.EmployeeStatus.Active);
            usersRepository.findByEmployeeId(employee.getId()).ifPresent(u -> {
                u.setIsActive(true);
                usersRepository.save(u);
            });
            bonusService.restoreBonuses(employee.getId(), careerChange.getApprovedBy(),
                    "Tự động bật lại do nhân viên đi làm lại: " + careerChange.getReason());
            if (careerChange.getNewSalary() != null) {
                employee.setCurrentSalary(careerChange.getNewSalary());
            } else if (employee.getCurrentSalary() == null && careerChange.getOldSalary() != null) {
                employee.setCurrentSalary(careerChange.getOldSalary());
            }
            if (careerChange.getNewPosition() != null) {
                employee.setCurrentPosition(careerChange.getNewPosition());
            }
        } else {
            if (careerChange.getNewSalary() != null) {
                employee.setCurrentSalary(careerChange.getNewSalary());
            }
            if (careerChange.getNewPosition() != null) {
                employee.setCurrentPosition(careerChange.getNewPosition());
            }
        }
    }

    private Sort createSort(String sortBy, String order) {
        String sortProperty;
        switch (sortBy != null ? sortBy.toLowerCase() : "") {
            case "effectivedate":
                sortProperty = "effectiveDate";
                break;
            case "newsalary":
                sortProperty = "newSalary";
                break;
            case "id":
            default:
                sortProperty = "id";
                break;
        }

        Sort.Direction direction = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, sortProperty);
    }

    private String generateRandomPassword() {
        int length = 8;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
