package com.ct08.PharmacyManagement.modules.hr.repository;

import com.ct08.PharmacyManagement.modules.hr.entity.Positions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface PositionsRepository extends JpaRepository<Positions, Integer> {
    Optional<Positions> findByPositionName(String positionName);
    
    List<Positions> findByStatus(Positions.ApprovalStatus status);

    @Query(value = "SELECT * FROM Positions p WHERE vn_unaccent(LOWER(p.position_name)) LIKE vn_unaccent(LOWER(CONCAT('%', CAST(:keyword AS text), '%')))", nativeQuery = true)
    List<Positions> searchPositionsByKeyword(@Param("keyword") String keyword);

    @Query(value = "SELECT * FROM Positions p WHERE vn_unaccent(LOWER(p.position_name)) LIKE vn_unaccent(LOWER(CONCAT('%', CAST(:keyword AS text), '%'))) AND p.status = CAST(:#{#status.name()} AS request_status_enum)", nativeQuery = true)
    List<Positions> searchPositionsByKeywordAndStatus(@Param("keyword") String keyword, @Param("status") Positions.ApprovalStatus status);
}
