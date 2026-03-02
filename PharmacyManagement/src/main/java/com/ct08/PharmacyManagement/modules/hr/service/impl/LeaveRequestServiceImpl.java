package com.ct08.PharmacyManagement.modules.hr.service.impl;

import com.ct08.PharmacyManagement.common.exception.BadRequestException;
import com.ct08.PharmacyManagement.common.exception.ConflictException;
import com.ct08.PharmacyManagement.common.exception.ResourceNotFoundException;
import com.ct08.PharmacyManagement.modules.auth.entity.Roles;
import com.ct08.PharmacyManagement.modules.auth.entity.Users;
import com.ct08.PharmacyManagement.modules.auth.repository.UsersRepository;
import com.ct08.PharmacyManagement.modules.hr.dto.LeaveRequestApprovalDTO;
import com.ct08.PharmacyManagement.modules.hr.dto.LeaveRequestApprovalDTO;
import com.ct08.PharmacyManagement.modules.hr.dto.LeaveRequestCreationDTO;
import com.ct08.PharmacyManagement.modules.hr.dto.LeaveRequestResponseDTO;
import com.ct08.PharmacyManagement.modules.hr.entity.LeaveRequests;
import com.ct08.PharmacyManagement.modules.hr.repository.LeaveRequestsRepository;
import com.ct08.PharmacyManagement.modules.hr.service.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestsRepository leaveRequestsRepository;
    private final UsersRepository usersRepository;

    @Override
    @Transactional
    public void createLeaveRequest(LeaveRequestCreationDTO request) {
        Users currentUser = getCurrentUser();

        if (request.getStartDate().toLocalDate().isBefore(LocalDate.now().plusDays(1))) {
            throw new BadRequestException("Start date must be at least the day after today");
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date must be before or equal to end date");
        }

        checkOverlapCurrentEmployee(currentUser.getEmployee().getId(), request.getStartDate(), request.getEndDate());

        LeaveRequests leaveRequest = new LeaveRequests();
        leaveRequest.setEmployee(currentUser.getEmployee());
        leaveRequest.setLeaveType(request.getLeaveType());
        leaveRequest.setStartDate(request.getStartDate());
        leaveRequest.setEndDate(request.getEndDate());
        leaveRequest.setReason(request.getReason());

        if (hasRole(currentUser, "ROLE_ADMIN")) {
            if (Boolean.TRUE.equals(request.getIsPaidLeave())) {
                leaveRequest.setStatus(LeaveRequests.ApprovalStatus.Approved_Salary);
            } else {
                leaveRequest.setStatus(LeaveRequests.ApprovalStatus.Approved);
            }
            leaveRequest.setApprovalReason("Tự động duyệt bởi Admin");
            leaveRequest.setApprovedBy(currentUser);
        } else {
            leaveRequest.setStatus(LeaveRequests.ApprovalStatus.Pending);
        }

        leaveRequestsRepository.save(leaveRequest);
    }

    @Override
    @Transactional
    public void approveLeaveRequest(Integer id, LeaveRequestApprovalDTO approvalDTO) {
        Users currentUser = getCurrentUser();
        LeaveRequests leaveRequest = getLeaveRequestById(id);
        Users creatorUser = getUserByEmployeeId(leaveRequest.getEmployee().getId());

        if (hasRole(currentUser, "ROLE_ADMIN")) {
            // ADMIN can approve anything
        } else if (hasRole(currentUser, "ROLE_HM")) {
            // HM can only approve WS or SS
            if (!hasRole(creatorUser, "ROLE_WS") && !hasRole(creatorUser, "ROLE_SS")) {
                throw new AccessDeniedException("HM role can only approve leave requests for WS and SS roles");
            }
        } else {
            throw new AccessDeniedException("You do not have permission to approve leave requests");
        }

        if (leaveRequest.getStatus() != LeaveRequests.ApprovalStatus.Pending) {
            throw new BadRequestException(
                    "Cannot approve/reject a request that is already " + leaveRequest.getStatus());
        }

        leaveRequest.setStatus(approvalDTO.getStatus());
        leaveRequest.setApprovalReason(approvalDTO.getApprovalReason());
        leaveRequest.setApprovedBy(currentUser);
        leaveRequestsRepository.save(leaveRequest);
    }

    @Override
    public List<LeaveRequestResponseDTO> getAllLeaveRequests(String statusParam, Integer employeeId,
            LocalDate startDate, LocalDate endDate) {
        Users currentUser = getCurrentUser();
        LeaveRequests.ApprovalStatus status = parseStatusOrNull(statusParam);

        List<LeaveRequests> requests;

        if (hasRole(currentUser, "ROLE_ADMIN")) {
            Specification<LeaveRequests> spec = buildSpecification(status, employeeId, startDate, endDate, null);
            requests = leaveRequestsRepository.findAll(spec);
        } else if (hasRole(currentUser, "ROLE_HM")) {
            List<String> roles = Arrays.asList("ROLE_WS", "ROLE_SS");
            Specification<LeaveRequests> spec = buildSpecification(status, employeeId, startDate, endDate, roles);
            requests = leaveRequestsRepository.findAll(spec);
        } else {
            throw new AccessDeniedException("You do not have permission to view all leave requests");
        }

        return requests.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private Specification<LeaveRequests> buildSpecification(LeaveRequests.ApprovalStatus status, Integer employeeId,
            LocalDate startDate, LocalDate endDate, List<String> allowedRoles) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (employeeId != null) {
                predicates.add(cb.equal(root.get("employee").get("id"), employeeId));
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), startDate.atStartOfDay()));
            }

            if (endDate != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("endDate"), endDate.plusDays(1).atStartOfDay().minusNanos(1)));
            }

            if (allowedRoles != null && !allowedRoles.isEmpty()) {
                jakarta.persistence.criteria.Subquery<Integer> sq = query.subquery(Integer.class);
                jakarta.persistence.criteria.Root<Users> usersRoot = sq.from(Users.class);
                jakarta.persistence.criteria.Join<Users, Roles> rolesJoin = usersRoot.join("roles");
                sq.select(usersRoot.get("employee").get("id")).where(rolesJoin.get("roleName").in(allowedRoles));
                predicates.add(root.get("employee").get("id").in(sq));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public List<LeaveRequestResponseDTO> getMyLeaveRequests(String statusParam) {
        Users currentUser = getCurrentUser();
        LeaveRequests.ApprovalStatus status = parseStatusOrNull(statusParam);

        List<LeaveRequests> requests;
        Integer employeeId = currentUser.getEmployee().getId();
        if (status == null) {
            requests = leaveRequestsRepository.findByEmployeeId(employeeId);
        } else {
            requests = leaveRequestsRepository.findByEmployeeIdAndStatus(employeeId, status);
        }

        return requests.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteLeaveRequest(Integer id) {
        Users currentUser = getCurrentUser();
        LeaveRequests leaveRequest = getLeaveRequestById(id);

        if (!leaveRequest.getEmployee().getId().equals(currentUser.getEmployee().getId())) {
            throw new AccessDeniedException("You can only delete your own leave requests");
        }

        if (leaveRequest.getStatus() != LeaveRequests.ApprovalStatus.Pending) {
            throw new BadRequestException("You can only delete pending leave requests");
        }

        leaveRequestsRepository.delete(leaveRequest);
    }

    // --- Helper Methods ---

    private Users getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return usersRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
    }

    private Users getUserByEmployeeId(Integer employeeId) {
        return usersRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for employee id " + employeeId));
    }

    private LeaveRequests getLeaveRequestById(Integer id) {
        return leaveRequestsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id " + id));
    }

    private boolean hasRole(Users user, String roleName) {
        return user.getRoles().stream().anyMatch(role -> role.getRoleName().equals(roleName));
    }

    private LeaveRequests.ApprovalStatus parseStatusOrNull(String statusValue) {
        if (statusValue == null || statusValue.trim().isEmpty()) {
            return null;
        }
        try {
            return LeaveRequests.ApprovalStatus.valueOf(statusValue);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + statusValue);
        }
    }

    private void checkOverlapCurrentEmployee(Integer employeeId, LocalDateTime newStart, LocalDateTime newEnd) {
        List<LeaveRequests> activeRequests = leaveRequestsRepository.findByEmployeeIdAndStatusIn(
                employeeId, Arrays.asList(LeaveRequests.ApprovalStatus.Pending, LeaveRequests.ApprovalStatus.Approved,
                        LeaveRequests.ApprovalStatus.Approved_Salary));

        for (LeaveRequests existing : activeRequests) {
            LocalDateTime eStart = existing.getStartDate();
            LocalDateTime eEnd = existing.getEndDate();

            // Overlap condition: max(start1, start2) < min(end1, end2)
            LocalDateTime maxStart = newStart.isAfter(eStart) ? newStart : eStart;
            LocalDateTime minEnd = newEnd.isBefore(eEnd) ? newEnd : eEnd;

            if (maxStart.isBefore(minEnd)) {
                throw new ConflictException(
                        "The leave request overlaps with an existing request (ID: " + existing.getId() + ")");
            }
        }
    }

    private LeaveRequestResponseDTO mapToDTO(LeaveRequests entity) {
        LeaveRequestResponseDTO dto = new LeaveRequestResponseDTO();
        dto.setId(entity.getId());
        dto.setEmployeeId(entity.getEmployee().getId());
        dto.setEmployeeName(entity.getEmployee().getLastName() + " " + entity.getEmployee().getFirstName());
        dto.setLeaveType(entity.getLeaveType());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setReason(entity.getReason());
        dto.setApprovalReason(entity.getApprovalReason());
        dto.setStatus(entity.getStatus());
        if (entity.getApprovedBy() != null) {
            dto.setApprovedByUsername(entity.getApprovedBy().getUsername());
        }
        return dto;
    }
}
