package com.ct08.PharmacyManagement.modules.sales.repository;

import com.ct08.PharmacyManagement.modules.sales.entity.InvoiceDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceDetailsRepository extends JpaRepository<InvoiceDetails, Integer> {
}
