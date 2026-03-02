package com.ct08.PharmacyManagement.modules.hr.service;

import com.ct08.PharmacyManagement.common.exception.ConflictException;
import com.ct08.PharmacyManagement.common.exception.ResourceNotFoundException;
import com.ct08.PharmacyManagement.modules.auth.entity.Users;
import com.ct08.PharmacyManagement.modules.auth.repository.UsersRepository;
import com.ct08.PharmacyManagement.modules.hr.dto.PositionRequest;
import com.ct08.PharmacyManagement.modules.hr.dto.PositionResponse;
import com.ct08.PharmacyManagement.modules.hr.dto.PositionStatusUpdateRequest;
import com.ct08.PharmacyManagement.modules.hr.entity.Positions;
import com.ct08.PharmacyManagement.modules.hr.repository.CareerChangesRepository;
import com.ct08.PharmacyManagement.modules.hr.repository.EmployeesRepository;
import com.ct08.PharmacyManagement.modules.hr.repository.PositionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PositionService {

    @Autowired
    private PositionsRepository positionsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private EmployeesRepository employeesRepository;

    @Autowired
    private CareerChangesRepository careerChangesRepository;

    public List<PositionResponse> getAllPositionsAdmin(Authentication authentication) {
        // Admin or HM can view all, controller has @PreAuthorize
        List<Positions> positions = positionsRepository.findAll();
        return positions.stream().map(PositionResponse::new).collect(Collectors.toList());
    }

    public List<PositionResponse> searchPositionsAdmin(String keyword, String statusStr,
            Authentication authentication) {
        Positions.ApprovalStatus status = null;
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                status = Positions.ApprovalStatus.valueOf(statusStr);
            } catch (IllegalArgumentException e) {
                // Ignore invalid status mapping or throw exception if preferred
            }
        }

        String searchKeyword = (keyword == null) ? "" : keyword;
        List<Positions> positions;

        if (status == null) {
            positions = positionsRepository.searchPositionsByKeyword(searchKeyword);
        } else {
            positions = positionsRepository.searchPositionsByKeywordAndStatus(searchKeyword, status);
        }

        return positions.stream().map(PositionResponse::new).collect(Collectors.toList());
    }

    public void createPosition(PositionRequest request, Authentication authentication) {
        String username = authentication.getName();
        Users currentUser = usersRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (positionsRepository.findByPositionName(request.getPositionName()).isPresent()) {
            throw new ConflictException("Tên chức vụ đã tồn tại");
        }

        Set<String> roles = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority()).collect(Collectors.toSet());
        boolean isAdmin = roles.contains("ROLE_ADMIN");

        Positions position = new Positions();
        position.setPositionName(request.getPositionName());
        position.setReason(request.getReason());
        position.setProposedBy(currentUser);

        if (isAdmin) {
            position.setStatus(Positions.ApprovalStatus.Approved);
            position.setApprovedBy(currentUser);
            position.setApprovalReason(request.getReason()); // Use creation reason as approval reason when
                                                             // auto-approved by admin
        } else {
            position.setStatus(Positions.ApprovalStatus.Pending);
            position.setApprovedBy(null);
        }

        positionsRepository.save(position);
    }

    public void updatePositionStatus(Integer id, PositionStatusUpdateRequest request, Authentication authentication) {
        Positions position = positionsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Position not found"));

        if (position.getStatus() != Positions.ApprovalStatus.Pending) {
            throw new ConflictException("Chỉ có thể duyệt chức vụ đang ở trạng thái Pending");
        }

        String username = authentication.getName();
        Users currentUser = usersRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        position.setStatus(request.getStatus());
        position.setApprovalReason(request.getApprovalReason());
        position.setApprovedBy(currentUser);

        positionsRepository.save(position);
    }

    public void updatePositionName(Integer id, PositionRequest request, Authentication authentication) {
        Positions position = positionsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Position not found"));

        if (!position.getPositionName().equals(request.getPositionName()) &&
                positionsRepository.findByPositionName(request.getPositionName()).isPresent()) {
            throw new ConflictException("Tên chức vụ đã tồn tại");
        }

        position.setPositionName(request.getPositionName());
        positionsRepository.save(position);
    }

    public void deletePosition(Integer id, Authentication authentication) {
        Positions position = positionsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Position not found"));

        if (employeesRepository.existsByCurrentPositionId(id)) {
            throw new ConflictException("Không thể xóa chức vụ này vì đang có nhân viên giữ chức vụ này");
        }

        if (careerChangesRepository.existsByNewPositionIdOrOldPositionId(id, id)) {
            throw new ConflictException("Không thể xóa chức vụ này vì có liên kết với lịch sử công tác của nhân viên");
        }

        positionsRepository.delete(position);
    }
}
