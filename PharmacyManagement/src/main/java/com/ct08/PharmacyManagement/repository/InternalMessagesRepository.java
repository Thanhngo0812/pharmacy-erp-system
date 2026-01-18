package com.ct08.PharmacyManagement.repository;

import com.ct08.PharmacyManagement.entity.InternalMessages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InternalMessagesRepository extends JpaRepository<InternalMessages, Integer> {
}
