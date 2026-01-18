package com.ct08.PharmacyManagement.repository;

import com.ct08.PharmacyManagement.entity.Batches;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchesRepository extends JpaRepository<Batches, Integer> {
}
