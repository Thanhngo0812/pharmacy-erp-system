package com.ct08.PharmacyManagement.modules.inventory.repository;

import com.ct08.PharmacyManagement.modules.inventory.entity.ImportNotes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportNotesRepository extends JpaRepository<ImportNotes, Integer> {
}
