package com.ct08.PharmacyManagement.modules.inventory.repository;

import com.ct08.PharmacyManagement.modules.inventory.entity.ProductUnits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductUnitsRepository extends JpaRepository<ProductUnits, Integer> {
    java.util.List<ProductUnits> findByIsActive(Boolean isActive);
    java.util.List<ProductUnits> findByProductIdAndIsActive(Integer productId, Boolean isActive);
}
