package com.ct08.PharmacyManagement.modules.inventory.repository;

import com.ct08.PharmacyManagement.modules.inventory.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductsRepository extends JpaRepository<Products, Integer> {
    java.util.List<Products> findByStatus(Products.ProductStatus status);
}
