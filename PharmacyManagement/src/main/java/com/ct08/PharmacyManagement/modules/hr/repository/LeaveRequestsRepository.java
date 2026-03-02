package com.ct08.PharmacyManagement.modules.hr.repository;

import com.ct08.PharmacyManagement.modules.hr.entity.LeaveRequests;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRequestsRepository
                extends JpaRepository<LeaveRequests, Integer>, JpaSpecificationExecutor<LeaveRequests> {

        // Find all by employee and status
        @Query("SELECT lr FROM LeaveRequests lr WHERE lr.employee.id = :employeeId AND lr.status IN :statuses")
        List<LeaveRequests> findByEmployeeIdAndStatusIn(@Param("employeeId") Integer employeeId,
                        @Param("statuses") List<LeaveRequests.ApprovalStatus> statuses);

        // Find all requests
        List<LeaveRequests> findByStatus(LeaveRequests.ApprovalStatus status);

        // Find all requests by employee's roles
        @Query("SELECT lr FROM LeaveRequests lr JOIN Users u ON lr.employee.id = u.employee.id JOIN u.roles r WHERE r.roleName IN :roleNames")
        List<LeaveRequests> findAllByEmployeeRoles(@Param("roleNames") List<String> roleNames);

        @Query("SELECT lr FROM LeaveRequests lr JOIN Users u ON lr.employee.id = u.employee.id JOIN u.roles r WHERE r.roleName IN :roleNames AND lr.status = :status")
        List<LeaveRequests> findAllByEmployeeRolesAndStatus(@Param("roleNames") List<String> roleNames,
                        @Param("status") LeaveRequests.ApprovalStatus status);

        // Find all requests by specific employee id
        List<LeaveRequests> findByEmployeeId(Integer employeeId);

        List<LeaveRequests> findByEmployeeIdAndStatus(Integer employeeId, LeaveRequests.ApprovalStatus status);
}
