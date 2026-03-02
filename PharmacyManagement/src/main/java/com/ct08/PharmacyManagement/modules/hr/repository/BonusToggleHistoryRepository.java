package com.ct08.PharmacyManagement.modules.hr.repository;

import com.ct08.PharmacyManagement.modules.hr.entity.BonusToggleHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BonusToggleHistoryRepository extends JpaRepository<BonusToggleHistory, Integer> {
    List<BonusToggleHistory> findByBonus_IdOrderByToggledAtDesc(Integer bonusId);
}
