package com.ct08.PharmacyManagement.repository;

import com.ct08.PharmacyManagement.entity.Positions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionsRepository extends JpaRepository<Positions, Integer> {
}
