package com.ct08.PharmacyManagement.modules.auth.repository;

import com.ct08.PharmacyManagement.modules.auth.entity.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolesRepository extends JpaRepository<Roles, Integer> {
    java.util.Optional<Roles> findByRoleName(String roleName);
}
