package com.ct08.PharmacyManagement.modules.hr.repository;

import com.ct08.PharmacyManagement.modules.hr.entity.Employees;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmployeesRepository extends JpaRepository<Employees, Integer> {

    @Query("SELECT DISTINCT u.employee FROM Users u " +
           "JOIN u.roles r " +
           "WHERE r.roleName IN :roleNames")
    List<Employees> findEmployeesByUserRoles(@Param("roleNames") List<String> roleNames);

    java.util.Optional<Employees> findByPhone(String phone);
    
    boolean existsByEmail(String email);
    
    boolean existsByCurrentPositionId(Integer positionId);
}
