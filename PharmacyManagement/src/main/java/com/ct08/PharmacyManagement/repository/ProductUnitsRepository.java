package com.ct08.PharmacyManagement.repository;

import com.ct08.PharmacyManagement.entity.ProductUnits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductUnitsRepository extends JpaRepository<ProductUnits, Integer> {
}
