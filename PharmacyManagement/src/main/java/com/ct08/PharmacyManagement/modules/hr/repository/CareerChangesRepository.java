package com.ct08.PharmacyManagement.modules.hr.repository;

import com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface CareerChangesRepository
        extends JpaRepository<CareerChanges, Integer>, JpaSpecificationExecutor<CareerChanges> {
    List<CareerChanges> findByEmployeeIdOrderByIdDesc(Integer employeeId);

    java.util.Optional<CareerChanges> findTopByEmployeeIdOrderByIdDesc(Integer employeeId);

    void deleteByEmployeeId(Integer employeeId);

    boolean existsByNewPositionIdOrOldPositionId(Integer newPositionId, Integer oldPositionId);

    List<CareerChanges> findByStatusAndIsAppliedFalseAndEffectiveDateLessThanEqual(
            CareerChanges.ApprovalStatus status,
            java.time.LocalDate effectiveDate);
}
