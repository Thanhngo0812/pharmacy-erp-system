package com.ct08.PharmacyManagement.repository;

import com.ct08.PharmacyManagement.entity.CareerChanges;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CareerChangesRepository extends JpaRepository<CareerChanges, Integer> {
}
