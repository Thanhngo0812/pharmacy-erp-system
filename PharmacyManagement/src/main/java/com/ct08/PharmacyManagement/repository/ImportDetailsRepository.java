package com.ct08.PharmacyManagement.repository;

import com.ct08.PharmacyManagement.entity.ImportDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportDetailsRepository extends JpaRepository<ImportDetails, Integer> {
}
