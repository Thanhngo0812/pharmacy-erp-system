package com.ct08.PharmacyManagement.modules.hr.service;

import com.ct08.PharmacyManagement.common.event.PasswordEmailEvent;
import com.ct08.PharmacyManagement.common.exception.ConflictException;
import com.ct08.PharmacyManagement.modules.auth.entity.Users;
import com.ct08.PharmacyManagement.modules.auth.repository.UserSpecification;
import com.ct08.PharmacyManagement.modules.auth.repository.UsersRepository;
import com.ct08.PharmacyManagement.modules.hr.dto.EmployeeProfileUpdateRequest;
import com.ct08.PharmacyManagement.modules.hr.dto.EmployeeResponse;
import com.ct08.PharmacyManagement.modules.hr.entity.Employees;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.ct08.PharmacyManagement.common.exception.ResourceNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import com.ct08.PharmacyManagement.modules.hr.dto.EmployeeUpdateRequest;
import com.ct08.PharmacyManagement.modules.auth.entity.Roles;
import java.util.HashSet;

@Service
public class EmployeeService {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private com.ct08.PharmacyManagement.modules.auth.repository.RolesRepository rolesRepository;

    @Autowired
    private com.ct08.PharmacyManagement.modules.hr.repository.EmployeesRepository employeesRepository;

    @Autowired
    private com.ct08.PharmacyManagement.common.infra.message.MessageProducerService messageProducerService;

    @Autowired
    private com.ct08.PharmacyManagement.modules.hr.repository.PositionsRepository positionsRepository;

    @Autowired
    private com.ct08.PharmacyManagement.modules.hr.service.BonusService bonusService;

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

    public List<EmployeeResponse> getEmployees(Authentication authentication,
            String sortBy, String order,
            Integer id, String name, String phone,
            String email, String roleName, String status) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        Sort sort = createSort(sortBy, order);

        Set<String> currentRoles = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toSet());

        boolean isAdmin = currentRoles.contains("ROLE_ADMIN");
        boolean isHR = currentRoles.contains("ROLE_HR") || currentRoles.contains("ROLE_HM");

        List<String> requiredRoles = null;
        List<String> excludedRoles = null;
        if (isAdmin) {
            // Admin sees all, no role restriction
            requiredRoles = null;
        } else if (isHR) {
            // HR sees only WS and SS
            requiredRoles = Arrays.asList("ROLE_WS", "ROLE_SS");
            excludedRoles = Arrays.asList("ROLE_ADMIN", "ROLE_HM", "ROLE_WM");
        } else {
            throw new AccessDeniedException("You do not have permission to view employee list");
        }

        Specification<Users> spec = UserSpecification.filter(id, name, phone, email, roleName, status, requiredRoles,
                excludedRoles);
        List<Users> users = usersRepository.findAll(spec, sort);

        return users.stream()
                .filter(user -> user.getEmployee() != null)
                .distinct()
                .map(user -> new EmployeeResponse(user.getEmployee(), user.getRoles(), user.getIsActive(),
                        user.getMailStatus()))
                .collect(Collectors.toList());
    }

    public java.util.Map<String, Object> getEmployeeSalaryList(Authentication authentication,
            String sortBy, String order,
            Integer id, String name, String status,
            java.math.BigDecimal minSalary, java.math.BigDecimal maxSalary) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        Sort sort = createSalarySort(sortBy, order);

        Set<String> currentRoles = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toSet());

        boolean isAdmin = currentRoles.contains("ROLE_ADMIN");
        boolean isHM = currentRoles.contains("ROLE_HM");

        if (!isAdmin && !isHM) {
            throw new AccessDeniedException("You do not have permission to view salary list");
        }

        List<String> requiredRoles = null;
        List<String> excludedRoles = null;
        if (!isAdmin && isHM) {
            requiredRoles = Arrays.asList("ROLE_WS", "ROLE_SS");
            excludedRoles = Arrays.asList("ROLE_ADMIN", "ROLE_HM", "ROLE_WM");
        }

        Specification<Users> spec = UserSpecification.filterSalary(id, name, status,
                minSalary, maxSalary, requiredRoles, excludedRoles);
        List<Users> users = usersRepository.findAll(spec, sort);

        List<com.ct08.PharmacyManagement.modules.hr.dto.EmployeeSalaryDTO> salaryList = users.stream()
                .filter(user -> user.getEmployee() != null)
                .distinct()
                .map(user -> new com.ct08.PharmacyManagement.modules.hr.dto.EmployeeSalaryDTO(user.getEmployee()))
                .collect(Collectors.toList());

        java.math.BigDecimal totalSalaryFund = salaryList.stream()
                .map(dto -> dto.getCurrentSalary() != null ? dto.getCurrentSalary() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("employees", salaryList);
        result.put("totalSalaryFund", totalSalaryFund);
        result.put("totalEmployees", salaryList.size());

        return result;
    }

    private Sort createSalarySort(String sortBy, String order) {
        String sortProperty;
        switch (sortBy != null ? sortBy : "id") {
            case "salary":
                sortProperty = "employee.currentSalary";
                break;
            case "hiredate":
                sortProperty = "employee.hireDate";
                break;
            default:
                sortProperty = "employee.id";
        }
        Sort.Direction direction = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, sortProperty);
    }

    @Autowired
    private com.ct08.PharmacyManagement.modules.hr.repository.CareerChangesRepository careerChangesRepository;

    public List<com.ct08.PharmacyManagement.modules.hr.dto.CareerHistoryDTO> getMyCareerHistory() {
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        String username = authentication.getName();
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getEmployee() == null) {
            throw new RuntimeException("User is not linked to an employee record");
        }

        List<com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges> history = careerChangesRepository
                .findByEmployeeIdOrderByIdDesc(user.getEmployee().getId());

        return history.stream()
                .map(com.ct08.PharmacyManagement.modules.hr.dto.CareerHistoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<com.ct08.PharmacyManagement.modules.hr.dto.CareerHistoryDTO> getCareerHistoryByEmployeeId(
            Integer employeeId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        Users targetUser = usersRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Set<String> currentRoles = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toSet());

        boolean isAdmin = currentRoles.contains("ROLE_ADMIN");
        boolean isHM = currentRoles.contains("ROLE_HM");

        if (!isAdmin) {
            if (isHM) {
                boolean isTargetAllowed = targetUser.getRoles().isEmpty() || targetUser.getRoles().stream()
                        .anyMatch(role -> "ROLE_WS".equals(role.getRoleName()) || "ROLE_SS".equals(role.getRoleName()));
                boolean hasHigherRole = targetUser.getRoles().stream()
                        .anyMatch(role -> "ROLE_ADMIN".equals(role.getRoleName())
                                || "ROLE_HM".equals(role.getRoleName()) || "ROLE_WM".equals(role.getRoleName()));
                if (!isTargetAllowed || hasHigherRole) {
                    throw new AccessDeniedException(
                            "You do not have permission to view this employee's career history");
                }
            } else {
                throw new AccessDeniedException("You do not have permission to view career history");
            }
        }

        List<com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges> history = careerChangesRepository
                .findByEmployeeIdOrderByIdDesc(employeeId);

        return history.stream()
                .map(com.ct08.PharmacyManagement.modules.hr.dto.CareerHistoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public EmployeeResponse getEmployeeDetail(Integer id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        Users targetUser = usersRepository.findByEmployeeId(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Set<String> currentRoles = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toSet());

        boolean isAdmin = currentRoles.contains("ROLE_ADMIN");
        boolean isHM = currentRoles.contains("ROLE_HM");

        // Admin can view everyone. HM needs to be checked.
        if (!isAdmin) {
            if (isHM) {
                // Check if target user has ROLE_WS or ROLE_SS or no roles
                boolean isTargetAllowed = targetUser.getRoles().isEmpty() || targetUser.getRoles().stream()
                        .anyMatch(role -> "ROLE_WS".equals(role.getRoleName()) || "ROLE_SS".equals(role.getRoleName()));
                boolean hasHigherRole = targetUser.getRoles().stream()
                        .anyMatch(role -> "ROLE_ADMIN".equals(role.getRoleName())
                                || "ROLE_HM".equals(role.getRoleName()) || "ROLE_WM".equals(role.getRoleName()));
                if (!isTargetAllowed || hasHigherRole) {
                    throw new AccessDeniedException("You do not have permission to view this employee's details");
                }
            } else {
                // Other roles cannot view details (unless we want to allow viewing self? The
                // requirement didn't specify, but usually 'Manager' implies managing others)
                // Assuming strictly Admin and HM for this specific API as per request
                // guidelines for "Management" view.
                // However, let's allow users to view themselves if ID matches? The request said
                // "Manager can view...".
                // Let's stick to the request: Admin view all, HM view WS/SS.
                throw new AccessDeniedException("You do not have permission to view employee details");
            }
        }

        EmployeeResponse response = new EmployeeResponse(targetUser.getEmployee(), targetUser.getRoles(),
                targetUser.getIsActive(), targetUser.getMailStatus());

        List<com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges> changes = careerChangesRepository
                .findByEmployeeIdOrderByIdDesc(id);
        com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges hiringChange = null;
        for (com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges change : changes) {
            if (change.getChangeType() == com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges.ChangeType.Hired
                    ||
                    change.getChangeType() == com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges.ChangeType.Rehired) {
                hiringChange = change;
            }
        }

        if (hiringChange != null) {
            if (hiringChange.getProposedBy() != null) {
                response.setProposedById(hiringChange.getProposedBy().getId());
                response.setProposedByName(hiringChange.getProposedBy().getEmployee() != null
                        ? hiringChange.getProposedBy().getEmployee().getLastName() + " "
                                + hiringChange.getProposedBy().getEmployee().getFirstName()
                        : hiringChange.getProposedBy().getUsername());
            }
            if (hiringChange.getApprovedBy() != null) {
                response.setApprovedById(hiringChange.getApprovedBy().getId());
                response.setApprovedByName(hiringChange.getApprovedBy().getEmployee() != null
                        ? hiringChange.getApprovedBy().getEmployee().getLastName() + " "
                                + hiringChange.getApprovedBy().getEmployee().getFirstName()
                        : hiringChange.getApprovedBy().getUsername());
            }
        }

        return response;
    }

    private Sort createSort(String sortBy, String order) {
        String sortProperty;
        switch (sortBy) {
            case "salary":
                sortProperty = "employee.currentSalary";
                break;
            case "firstname":
                sortProperty = "employee.firstName";
                break;
            case "hiredate":
                sortProperty = "employee.hireDate";
                break;
            case "status":
                sortProperty = "employee.status";
                break;
            case "position":
                sortProperty = "employee.currentPosition.positionName";
                break;
            case "phone":
                sortProperty = "employee.phone";
                break;
            case "email":
                sortProperty = "employee.email";
                break;
            case "role":
                sortProperty = "roles.roleName";
                break;
            default:
                sortProperty = "id";
        }

        Sort.Direction direction = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, sortProperty);
    }

    public void updateEmployee(Integer id, EmployeeUpdateRequest request, MultipartFile image,
            Authentication authentication) throws IOException {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        Users targetUser = usersRepository.findByEmployeeId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Employees targetEmployee = targetUser.getEmployee();

        Set<String> currentRoles = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toSet());

        boolean isAdmin = currentRoles.contains("ROLE_ADMIN");
        boolean isHM = currentRoles.contains("ROLE_HM");

        if (!isAdmin) {
            if (isHM) {
                boolean isTargetAllowed = targetUser.getRoles().isEmpty() || targetUser.getRoles().stream()
                        .anyMatch(role -> "ROLE_WS".equals(role.getRoleName()) || "ROLE_SS".equals(role.getRoleName()));
                boolean hasHigherRole = targetUser.getRoles().stream()
                        .anyMatch(role -> "ROLE_ADMIN".equals(role.getRoleName())
                                || "ROLE_HM".equals(role.getRoleName()) || "ROLE_WM".equals(role.getRoleName()));
                if (!isTargetAllowed || hasHigherRole) {
                    throw new AccessDeniedException(
                            "You can only update employees with WS or SS roles and no higher level roles");
                }
            } else {
                throw new AccessDeniedException("You do not have permission to update employees");
            }
        }

        // Update Fields
        if (request.getLastName() != null)
            targetEmployee.setLastName(request.getLastName());
        if (request.getFirstName() != null)
            targetEmployee.setFirstName(request.getFirstName());

        if (request.getPhone() != null && !request.getPhone().equals(targetEmployee.getPhone())) {
            if (employeesRepository.findByPhone(request.getPhone()).isPresent()) {
                throw new ConflictException("Số điện thoại đã được sử dụng");
            }
            targetEmployee.setPhone(request.getPhone());
        }

        // Email & Username Sync
        if (request.getEmail() != null && !request.getEmail().equals(targetEmployee.getEmail())) {
            // Check if email already exists
            // We can check username existence since username == email
            if (usersRepository.findByUsername(request.getEmail()).isPresent()) {
                throw new ConflictException("Email đã được sử dụng");
            }
            targetEmployee.setEmail(request.getEmail());
            targetUser.setUsername(request.getEmail());

            // Password change logic for new email
            String newPassword = generateRandomPassword();
            targetUser.setPasswordHash(passwordEncoder.encode(newPassword));
            targetUser.setMailStatus("sending");

            String fullName = targetEmployee.getLastName() + " " + targetEmployee.getFirstName();
            messageProducerService.sendMessage("user-password-email",
                    new PasswordEmailEvent(
                            targetUser.getId(), request.getEmail(), fullName, newPassword));
        }

        // Image Handling
        if (image != null && !image.isEmpty()) {
            String uploadDir = "uploads/images/employees/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = image.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + fileExtension;

            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String oldImageUrl = targetEmployee.getImageUrl();

            // Send to Kafka for async upload
            try {
                com.ct08.PharmacyManagement.common.event.ImageUpdateEvent event = new com.ct08.PharmacyManagement.common.event.ImageUpdateEvent(
                        id,
                        uploadPath.resolve(newFilename).toString(),
                        oldImageUrl);
                messageProducerService.sendMessage("employee-image-upload", event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Roles Handling
        if (request.getRoles() != null) {
            Set<Roles> newRoles = new HashSet<>();
            for (String roleName : request.getRoles()) {
                if (isHM) {
                    // HM can only assign WS or SS
                    if (!roleName.equals("ROLE_WS") && !roleName.equals("ROLE_SS")) {
                        continue; // Skip unauthorized roles
                    }
                }
                Roles role = rolesRepository.findByRoleName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
                newRoles.add(role);
            }
            // If HM and filtered list is empty but request wasn't, might be an issue, but
            // let's just set what's valid.
            // Requirement: HM edits "owned roles".
            targetUser.setRoles(newRoles);
        }

        usersRepository.save(targetUser);
    }

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public void createEmployee(com.ct08.PharmacyManagement.modules.hr.dto.EmployeeCreationRequest request,
            MultipartFile image, Authentication authentication) throws IOException {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        String currentUsername = authentication.getName();
        Users currentUser = usersRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Creator user not found"));

        Set<String> currentRoles = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toSet());

        boolean isAdmin = currentRoles.contains("ROLE_ADMIN");
        boolean isHM = currentRoles.contains("ROLE_HM");

        if (!isAdmin && !isHM) {
            throw new AccessDeniedException("You do not have permission to create employees");
        }

        // Exclusivity Checks
        boolean hasAdmin = request.getRoles().contains("ROLE_ADMIN");
        boolean hasHM = request.getRoles().contains("ROLE_HM");
        if (hasAdmin && hasHM) {
            throw new com.ct08.PharmacyManagement.common.exception.BadRequestException(
                    "Một nhân viên không thể vừa làm Admin vừa làm HR Manager");
        }

        boolean hasWM = request.getRoles().contains("ROLE_WM");
        boolean hasWS = request.getRoles().contains("ROLE_WS");
        if (hasWM && hasWS) {
            throw new com.ct08.PharmacyManagement.common.exception.BadRequestException(
                    "Một nhân viên không thể vừa làm Warehouse Manager vừa làm Warehouse Staff");
        }

        if (isHM && !isAdmin) {
            // Check if HM is trying to create roles they are not allowed to (like Admin or
            // another Manager)
            for (String role : request.getRoles()) {
                if (!role.equals("ROLE_WS") && !role.equals("ROLE_SS")) {
                    throw new AccessDeniedException("HR Manager can only create Employee with WS or SS roles");
                }
            }
        }

        if (employeesRepository.existsByEmail(request.getEmail())
                || usersRepository.findByUsername(request.getEmail()).isPresent()) {
            throw new ConflictException("Email đã được sử dụng");
        }

        if (employeesRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new ConflictException("Số điện thoại đã được sử dụng");
        }

        com.ct08.PharmacyManagement.modules.hr.entity.Positions position = positionsRepository
                .findByPositionName(request.getPositionName())
                .orElseThrow(() -> new ResourceNotFoundException("Position not found: " + request.getPositionName()));

        Employees employee = new Employees();
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setCurrentPosition(position);
        employee.setCurrentSalary(
                request.getCurrentSalary() != null ? request.getCurrentSalary() : java.math.BigDecimal.ZERO);
        employee.setHireDate(request.getHireDate());

        // Status logic based on creator role
        if (isAdmin) {
            employee.setStatus(Employees.EmployeeStatus.Active);
        } else {
            employee.setStatus(Employees.EmployeeStatus.Waiting);
        }

        employeesRepository.save(employee);

        // Image Handling
        if (image != null && !image.isEmpty()) {
            String uploadDir = "uploads/images/employees/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = image.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + fileExtension;

            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // set url locally initially
            employee.setImageUrl(uploadPath.resolve(newFilename).toString());
            employeesRepository.save(employee);

            // Send to Kafka for async upload
            try {
                com.ct08.PharmacyManagement.common.event.ImageUpdateEvent event = new com.ct08.PharmacyManagement.common.event.ImageUpdateEvent(
                        employee.getId(),
                        uploadPath.resolve(newFilename).toString(),
                        "");
                messageProducerService.sendMessage("employee-image-upload", event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Users logic
        Users user = new Users();
        user.setEmployee(employee);
        user.setUsername(request.getEmail());

        String newPassword = generateRandomPassword();
        user.setPasswordHash(passwordEncoder.encode(newPassword));

        Set<Roles> roles = new HashSet<>();
        for (String roleName : request.getRoles()) {
            Roles role = rolesRepository.findByRoleName(roleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
            roles.add(role);
        }
        user.setRoles(roles);

        if (isAdmin) {
            user.setIsActive(true);
            user.setMailStatus("sending");
        } else {
            user.setIsActive(false);
        }

        usersRepository.save(user);

        if (isAdmin) {
            String fullName = employee.getLastName() + " " + employee.getFirstName();
            messageProducerService.sendMessage("user-password-email",
                    new PasswordEmailEvent(
                            user.getId(), request.getEmail(), fullName, newPassword));
        }

        // Career Change logic
        com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges change = new com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges();
        change.setEmployee(employee);
        change.setChangeType(com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges.ChangeType.Hired);
        change.setOldSalary(java.math.BigDecimal.ZERO);
        change.setNewSalary(employee.getCurrentSalary());
        change.setOldPosition(null);
        change.setNewPosition(position);
        change.setEffectiveDate(employee.getHireDate());
        change.setProposedBy(currentUser);
        change.setReason("Tuyển dụng mới");

        if (isAdmin) {
            change.setStatus(com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges.ApprovalStatus.Approved);
            change.setApprovedBy(currentUser);
        } else {
            change.setStatus(com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges.ApprovalStatus.Pending);
            change.setApprovedBy(null);
        }

        careerChangesRepository.save(change);
    }

    @org.springframework.transaction.annotation.Transactional
    public void deleteWaitingEmployee(Integer id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        Users targetUser = usersRepository.findByEmployeeId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Employees targetEmployee = targetUser.getEmployee();

        if (targetEmployee.getStatus() != Employees.EmployeeStatus.Waiting) {
            throw new ConflictException("Chỉ có thể xóa nhân viên đang ở trạng thái Waiting");
        }

        Set<String> currentRoles = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toSet());

        boolean isAdmin = currentRoles.contains("ROLE_ADMIN");
        boolean isHM = currentRoles.contains("ROLE_HM");

        if (!isAdmin && !isHM) {
            throw new AccessDeniedException("Bạn không có quyền xóa nhân viên");
        }

        if (isHM && !isAdmin) {
            boolean isTargetAllowed = targetUser.getRoles().stream()
                    .anyMatch(role -> "ROLE_WS".equals(role.getRoleName()) || "ROLE_SS".equals(role.getRoleName()));
            boolean hasHigherRole = targetUser.getRoles().stream()
                    .anyMatch(role -> "ROLE_ADMIN".equals(role.getRoleName()) || "ROLE_HM".equals(role.getRoleName())
                            || "ROLE_WM".equals(role.getRoleName()));
            if (!isTargetAllowed || hasHigherRole) {
                throw new AccessDeniedException(
                        "HR Manager chỉ có thể xóa nhân viên có role WS hoặc SS và không có quyền cao hơn");
            }
        }

        // Must delete in order due to foreign keys: CareerChanges -> Users -> Employees
        careerChangesRepository.deleteByEmployeeId(id);
        usersRepository.delete(targetUser);
        employeesRepository.delete(targetEmployee);
    }

    public void updateMyProfile(EmployeeProfileUpdateRequest request, MultipartFile image,
            Authentication authentication) throws IOException {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        String currentUsername = authentication.getName();
        Users currentUser = usersRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Employees currentEmployee = currentUser.getEmployee();
        if (currentEmployee == null) {
            throw new ResourceNotFoundException("Employee profile not found");
        }

        // Validate Phone Uniqueness
        if (request.getPhone() != null && !request.getPhone().equals(currentEmployee.getPhone())) {
            if (employeesRepository.findByPhone(request.getPhone()).isPresent()) {
                throw new ConflictException("Số điện thoại đã được sử dụng");
            }
            currentEmployee.setPhone(request.getPhone());
        }

        // Validate Email Uniqueness & Update Username
        if (request.getEmail() != null && !request.getEmail().equals(currentEmployee.getEmail())) {
            // Check if email already exists
            if (usersRepository.findByUsername(request.getEmail()).isPresent()) {
                throw new ConflictException("Email đã được sử dụng");
            }
            currentEmployee.setEmail(request.getEmail());
            currentUser.setUsername(request.getEmail());

            // Password change logic for new email
            String newPassword = generateRandomPassword();
            currentUser.setPasswordHash(passwordEncoder.encode(newPassword));
            currentUser.setMailStatus("sending");

            String fullName = currentEmployee.getLastName() + " " + currentEmployee.getFirstName();
            messageProducerService.sendMessage("user-password-email",
                    new PasswordEmailEvent(
                            currentUser.getId(), request.getEmail(), fullName, newPassword));
        }

        // Update Name
        if (request.getLastName() != null)
            currentEmployee.setLastName(request.getLastName());
        if (request.getFirstName() != null)
            currentEmployee.setFirstName(request.getFirstName());

        // Image Handling
        if (image != null && !image.isEmpty()) {
            String uploadDir = "uploads/images/employees/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = image.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + fileExtension;

            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String oldImageUrl = currentEmployee.getImageUrl();

            // Send to Kafka for async upload
            try {
                com.ct08.PharmacyManagement.common.event.ImageUpdateEvent event = new com.ct08.PharmacyManagement.common.event.ImageUpdateEvent(
                        currentEmployee.getId(),
                        uploadPath.resolve(newFilename).toString(),
                        oldImageUrl);
                messageProducerService.sendMessage("employee-image-upload", event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        usersRepository.save(currentUser);
    }

    public void resignEmployee(Integer id, com.ct08.PharmacyManagement.modules.hr.dto.ResignationRequest request,
            Authentication authentication) {
        // 1. Check permissions (Admin or HM)
        // This is handled by controller roles, but extra check is fine or trust
        // controller
        // authentication check:
        // Already handled by @PreAuthorize in controller usually, but let's be safe if
        // we want logic here
        // We will assume controller handles role check.

        Users currentUser = usersRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2. Find Employee
        Employees employee = employeesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (employee.getStatus() == Employees.EmployeeStatus.Resigned) {
            throw new ConflictException("Employee is already resigned");
        }

        // 3. Update Status
        employee.setStatus(Employees.EmployeeStatus.Resigned);
        // Note: We keep currentSalary as is for record keeping of "last salary", or we
        // can set to 0.
        // Requirement said: "Resignation: New Salary = 0" in CareerChanges.
        // Let's keep employee.id, but update CareerChanges.

        com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges change = new com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges();
        change.setEmployee(employee);
        change.setChangeType(com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges.ChangeType.Resigned);
        change.setOldSalary(employee.getCurrentSalary());
        change.setNewSalary(java.math.BigDecimal.ZERO);
        change.setEffectiveDate(request.getDate());
        change.setReason(request.getReason());
        change.setStatus(com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges.ApprovalStatus.Approved);
        change.setIsApplied(true);
        change.setProposedBy(currentUser);
        change.setApprovedBy(currentUser); // Auto-approved since it's an admin action
        change.setOldPosition(employee.getCurrentPosition());
        change.setNewPosition(null); // Or keep old? Usually position is lost.

        careerChangesRepository.save(change);
        employeesRepository.save(employee);

        // Auto disable bonuses
        bonusService.disableAllActiveBonuses(id, currentUser,
                "Tự động tắt do nhân viên nghỉ việc: " + request.getReason());

        // Also deactivate user account
        usersRepository.findByEmployeeId(id).ifPresent(u -> {
            u.setIsActive(false);
            usersRepository.save(u);
        });
    }

    public void rehireEmployee(Integer id, com.ct08.PharmacyManagement.modules.hr.dto.RehireRequest request,
            Authentication authentication) {
        Users currentUser = usersRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Employees employee = employeesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (employee.getStatus() == Employees.EmployeeStatus.Active) {
            throw new ConflictException("Employee is already active");
        }

        // Determine New Salary
        java.math.BigDecimal newSalary = request.getNewSalary();
        if (newSalary == null) {
            // Restore old salary from history or current (if preserved)
            // Strategy: Find latest CareerChange. If it was Resigned, check its oldSalary.
            java.util.Optional<com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges> lastChangeOpt = careerChangesRepository
                    .findTopByEmployeeIdOrderByIdDesc(id);

            if (lastChangeOpt.isPresent()) {
                com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges lastChange = lastChangeOpt.get();
                if (lastChange
                        .getChangeType() == com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges.ChangeType.Resigned) {
                    newSalary = lastChange.getOldSalary();
                } else {
                    // Fallback to whatever is in employee record
                    newSalary = employee.getCurrentSalary();
                }
            } else {
                newSalary = employee.getCurrentSalary();
            }
        }

        // Validate salary
        if (newSalary == null || newSalary.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new com.ct08.PharmacyManagement.common.exception.BadRequestException(
                    "Cannot determine valid salary for re-hire");
        }

        // Update Status
        employee.setStatus(Employees.EmployeeStatus.Active);
        employee.setCurrentSalary(newSalary);

        com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges change = new com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges();
        change.setEmployee(employee);
        change.setChangeType(com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges.ChangeType.Rehired);
        change.setOldSalary(java.math.BigDecimal.ZERO); // Coming from unemployment
        change.setNewSalary(newSalary);
        change.setEffectiveDate(request.getDate());
        change.setReason(request.getReason());
        change.setStatus(com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges.ApprovalStatus.Approved);
        change.setIsApplied(true);
        change.setProposedBy(currentUser);
        change.setApprovedBy(currentUser);
        change.setOldPosition(null);
        change.setNewPosition(employee.getCurrentPosition()); // Restore position? assume yes

        careerChangesRepository.save(change);
        employeesRepository.save(employee);

        // Auto restore bonuses
        bonusService.restoreBonuses(id, currentUser, "Tự động bật lại do nhân viên đi làm lại: " + request.getReason());

        // Reactivate user account
        usersRepository.findByEmployeeId(id).ifPresent(u -> {
            u.setIsActive(true);
            usersRepository.save(u);
        });
    }
}
