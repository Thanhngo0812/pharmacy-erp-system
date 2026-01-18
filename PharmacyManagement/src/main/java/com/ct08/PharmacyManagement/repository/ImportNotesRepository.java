package com.ct08.PharmacyManagement.repository;

import com.ct08.PharmacyManagement.entity.ImportNotes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportNotesRepository extends JpaRepository<ImportNotes, Integer> {
}
