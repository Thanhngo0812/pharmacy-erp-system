package com.ct08.PharmacyManagement.modules.auth.repository;

import com.ct08.PharmacyManagement.modules.auth.entity.Roles;
import com.ct08.PharmacyManagement.modules.auth.entity.Users;
import com.ct08.PharmacyManagement.modules.hr.entity.Employees;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<Users> filterSalary(Integer id, String name, String status,
            BigDecimal minSalary, BigDecimal maxSalary,
            List<String> requiredRoles, List<String> excludedRoles) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always join Employee
            Join<Users, Employees> employeeJoin = root.join("employee", JoinType.INNER);

            // Filter: ID
            if (id != null) {
                predicates.add(criteriaBuilder.equal(employeeJoin.get("id"), id));
            }

            // Filter: Name (First or Last)
            if (StringUtils.hasText(name)) {
                String searchName = "%" + name.toLowerCase() + "%";
                Predicate firstNameLike = criteriaBuilder.like(criteriaBuilder.lower(employeeJoin.get("firstName")),
                        searchName);
                Predicate lastNameLike = criteriaBuilder.like(criteriaBuilder.lower(employeeJoin.get("lastName")),
                        searchName);
                predicates.add(criteriaBuilder.or(firstNameLike, lastNameLike));
            }

            // Filter: Status
            if (StringUtils.hasText(status)) {
                predicates.add(
                        criteriaBuilder.equal(employeeJoin.get("status"), Employees.EmployeeStatus.valueOf(status)));
            }

            // Filter: Salary Range
            if (minSalary != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(employeeJoin.get("currentSalary"), minSalary));
            }
            if (maxSalary != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(employeeJoin.get("currentSalary"), maxSalary));
            }

            // Handle Roles (Auth Constraints)
            Join<Users, Roles> rolesJoin = root.join("roles", JoinType.LEFT);

            if (requiredRoles != null && !requiredRoles.isEmpty()) {
                predicates.add(criteriaBuilder.or(
                        rolesJoin.get("roleName").in(requiredRoles),
                        criteriaBuilder.isEmpty(root.get("roles"))));
            }

            if (excludedRoles != null && !excludedRoles.isEmpty()) {
                jakarta.persistence.criteria.Subquery<Integer> subquery = query.subquery(Integer.class);
                jakarta.persistence.criteria.Root<Users> subRoot = subquery.from(Users.class);
                jakarta.persistence.criteria.Join<Users, Roles> subRolesJoin = subRoot.join("roles");
                subquery.select(subRoot.get("id"));
                subquery.where(subRolesJoin.get("roleName").in(excludedRoles));

                predicates.add(criteriaBuilder.not(root.get("id").in(subquery)));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Users> filter(Integer id, String name, String phone, String email, String roleName,
            String status, List<String> requiredRoles, List<String> excludedRoles) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always join Employee
            Join<Users, Employees> employeeJoin = root.join("employee", JoinType.INNER);

            // Filter: ID
            if (id != null) {
                predicates.add(criteriaBuilder.equal(employeeJoin.get("id"), id));
            }

            // Filter: Name (First or Last)
            if (StringUtils.hasText(name)) {
                String searchName = "%" + name.toLowerCase() + "%";
                Predicate firstNameLike = criteriaBuilder.like(criteriaBuilder.lower(employeeJoin.get("firstName")),
                        searchName);
                Predicate lastNameLike = criteriaBuilder.like(criteriaBuilder.lower(employeeJoin.get("lastName")),
                        searchName);
                predicates.add(criteriaBuilder.or(firstNameLike, lastNameLike));
            }

            // Filter: Phone
            if (StringUtils.hasText(phone)) {
                predicates.add(criteriaBuilder.like(employeeJoin.get("phone"), "%" + phone + "%"));
            }

            // Filter: Email
            if (StringUtils.hasText(email)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(employeeJoin.get("email")),
                        "%" + email.toLowerCase() + "%"));
            }

            // Filter: Status
            if (StringUtils.hasText(status)) {
                predicates.add(
                        criteriaBuilder.equal(employeeJoin.get("status"), Employees.EmployeeStatus.valueOf(status)));
            }

            // Handle Roles (Search + Auth Constraints)
            Join<Users, Roles> rolesJoin = root.join("roles", JoinType.LEFT);

            // 1. Auth Constraint: Limit to specific roles if required (e.g., HR viewing
            // only WS/SS)
            if (requiredRoles != null && !requiredRoles.isEmpty()) {
                predicates.add(criteriaBuilder.or(
                        rolesJoin.get("roleName").in(requiredRoles),
                        criteriaBuilder.isEmpty(root.get("roles"))));
            }

            // 2. Search Filter: Role Name
            if (StringUtils.hasText(roleName)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(rolesJoin.get("roleName")),
                        "%" + roleName.toLowerCase() + "%"));
            }

            // 3. Exclude specific roles
            if (excludedRoles != null && !excludedRoles.isEmpty()) {
                jakarta.persistence.criteria.Subquery<Integer> subquery = query.subquery(Integer.class);
                jakarta.persistence.criteria.Root<Users> subRoot = subquery.from(Users.class);
                jakarta.persistence.criteria.Join<Users, Roles> subRolesJoin = subRoot.join("roles");
                subquery.select(subRoot.get("id"));
                subquery.where(subRolesJoin.get("roleName").in(excludedRoles));

                predicates.add(criteriaBuilder.not(root.get("id").in(subquery)));
            }

            // To ensure distinct results because of joins
            // query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
