package com.ct08.PharmacyManagement.modules.inventory.repository;

import com.ct08.PharmacyManagement.modules.inventory.entity.Suppliers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuppliersRepository extends JpaRepository<Suppliers, Integer> {
}
