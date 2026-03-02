package com.ct08.PharmacyManagement.modules.auth.repository;

import com.ct08.PharmacyManagement.modules.auth.entity.Users;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Integer>, JpaSpecificationExecutor<Users> {
    Optional<Users> findByUsername(String username);

    List<Users> findByRoles_RoleNameIn(Collection<String> roleNames, Sort sort);

    Optional<Users> findByEmployeeId(Integer employeeId);

    Optional<Users> findByEmployee_Email(String email);
}
