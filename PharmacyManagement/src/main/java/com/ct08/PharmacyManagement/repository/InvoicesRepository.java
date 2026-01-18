package com.ct08.PharmacyManagement.repository;

import com.ct08.PharmacyManagement.entity.Invoices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoicesRepository extends JpaRepository<Invoices, Integer> {
}
