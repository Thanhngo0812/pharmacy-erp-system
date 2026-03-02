package com.ct08.PharmacyManagement.modules.hr.service;

import com.ct08.PharmacyManagement.modules.hr.dto.LeaveRequestApprovalDTO;
import com.ct08.PharmacyManagement.modules.hr.dto.LeaveRequestCreationDTO;
import com.ct08.PharmacyManagement.modules.hr.dto.LeaveRequestResponseDTO;

import java.util.List;

import java.time.LocalDate;

public interface LeaveRequestService {
    void createLeaveRequest(LeaveRequestCreationDTO request);

    void approveLeaveRequest(Integer id, LeaveRequestApprovalDTO approvalDTO);

    List<LeaveRequestResponseDTO> getAllLeaveRequests(String status, Integer employeeId, LocalDate startDate,
            LocalDate endDate);

    List<LeaveRequestResponseDTO> getMyLeaveRequests(String status);

    void deleteLeaveRequest(Integer id);
}
