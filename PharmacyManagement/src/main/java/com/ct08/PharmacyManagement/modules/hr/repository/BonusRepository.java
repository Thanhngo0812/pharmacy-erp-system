package com.ct08.PharmacyManagement.modules.hr.repository;

import com.ct08.PharmacyManagement.modules.hr.entity.Bonus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface BonusRepository extends JpaRepository<Bonus, Integer>, JpaSpecificationExecutor<Bonus> {

    @Query("SELECT b FROM Bonus b " +
            "JOIN FETCH b.employee e " +
            "LEFT JOIN FETCH b.proposedBy pb " +
            "LEFT JOIN FETCH pb.employee pbe " +
            "LEFT JOIN FETCH b.approvedBy ab " +
            "LEFT JOIN FETCH ab.employee abe " +
            "WHERE (:bonusName = '' OR LOWER(b.bonusName) LIKE LOWER(CONCAT('%', :bonusName, '%'))) " +
            "AND (:hasStatus = false OR b.status = :status) " +
            "ORDER BY b.bonusName, b.endDate, b.amount")
    List<Bonus> findAllWithFilters(
            @Param("bonusName") String bonusName,
            @Param("hasStatus") boolean hasStatus,
            @Param("status") Bonus.ApprovalStatus status);

    @Query("SELECT b FROM Bonus b " +
            "JOIN FETCH b.employee e " +
            "JOIN com.ct08.PharmacyManagement.modules.auth.entity.Users u ON u.employee = e " +
            "JOIN u.roles r " +
            "LEFT JOIN FETCH b.proposedBy pb " +
            "LEFT JOIN FETCH pb.employee pbe " +
            "LEFT JOIN FETCH b.approvedBy ab " +
            "LEFT JOIN FETCH ab.employee abe " +
            "WHERE r.roleName IN ('ROLE_WS', 'ROLE_SS') " +
            "AND (:bonusName = '' OR LOWER(b.bonusName) LIKE LOWER(CONCAT('%', :bonusName, '%'))) " +
            "AND (:hasStatus = false OR b.status = :status) " +
            "ORDER BY b.bonusName, b.endDate, b.amount")
    List<Bonus> findAllForHM(
            @Param("bonusName") String bonusName,
            @Param("hasStatus") boolean hasStatus,
            @Param("status") Bonus.ApprovalStatus status);
}
