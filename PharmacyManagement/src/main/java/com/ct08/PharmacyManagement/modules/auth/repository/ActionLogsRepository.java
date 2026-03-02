package com.ct08.PharmacyManagement.modules.auth.repository;

import com.ct08.PharmacyManagement.modules.auth.entity.ActionLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionLogsRepository extends JpaRepository<ActionLogs, Integer> {
}
