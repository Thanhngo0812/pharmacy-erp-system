package com.ct08.PharmacyManagement.common.infra.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Integer> {

    // Retrieve events oldest first
    List<OutboxEvent> findAllByOrderByCreatedAtAsc();
}
