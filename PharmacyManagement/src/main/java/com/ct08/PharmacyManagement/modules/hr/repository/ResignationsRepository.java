package com.ct08.PharmacyManagement.modules.hr.repository;

import com.ct08.PharmacyManagement.modules.hr.entity.Resignations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResignationsRepository extends JpaRepository<Resignations, Integer> {
}
