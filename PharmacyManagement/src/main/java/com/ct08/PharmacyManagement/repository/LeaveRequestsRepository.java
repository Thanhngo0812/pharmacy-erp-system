package com.ct08.PharmacyManagement.repository;

import com.ct08.PharmacyManagement.entity.LeaveRequests;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveRequestsRepository extends JpaRepository<LeaveRequests, Integer> {
}
