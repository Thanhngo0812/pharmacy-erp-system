package com.ct08.PharmacyManagement.modules.hr.repository;

import com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class CareerChangesSpecification {

    public static Specification<CareerChanges> filterByHiredAndStatus(String status, Integer id, String employeeName,
            Integer proposedById) {
        return (root, query, criteriaBuilder) -> {
            Specification<CareerChanges> spec = Specification
                    .where((r, q, cb) -> cb.equal(r.get("changeType"), CareerChanges.ChangeType.Hired));

            if (StringUtils.hasText(status)) {
                try {
                    CareerChanges.ApprovalStatus approvalStatus = CareerChanges.ApprovalStatus.valueOf(status);
                    spec = spec.and((r, q, cb) -> cb.equal(r.get("status"), approvalStatus));
                } catch (IllegalArgumentException e) {
                    // Ignore invalid status format or handle gracefully
                }
            }

            if (id != null) {
                spec = spec.and((r, q, cb) -> cb.equal(r.get("id"), id));
            }

            if (StringUtils.hasText(employeeName)) {
                spec = spec.and((r, q, cb) -> {
                    var employeeJoin = r.join("employee");
                    String pattern = "%" + employeeName.trim().toLowerCase() + "%";
                    return cb.or(
                            cb.like(cb.lower(employeeJoin.get("firstName")), pattern),
                            cb.like(cb.lower(employeeJoin.get("lastName")), pattern));
                });
            }

            if (proposedById != null) {
                spec = spec.and((r, q, cb) -> {
                    var proposedByJoin = r.join("proposedBy");
                    return cb.equal(proposedByJoin.get("id"), proposedById);
                });
            }

            return spec.toPredicate(root, query, criteriaBuilder);
        };
    }

    public static Specification<CareerChanges> filterByNonHired(String status, Integer id, Integer employeeId,
            String changeType, Integer proposedById) {
        return (root, query, criteriaBuilder) -> {
            Specification<CareerChanges> spec = Specification
                    .where((r, q, cb) -> cb.notEqual(r.get("changeType"), CareerChanges.ChangeType.Hired));

            if (StringUtils.hasText(status)) {
                try {
                    CareerChanges.ApprovalStatus approvalStatus = CareerChanges.ApprovalStatus.valueOf(status);
                    spec = spec.and((r, q, cb) -> cb.equal(r.get("status"), approvalStatus));
                } catch (IllegalArgumentException e) {
                    // Ignore invalid status
                }
            }

            if (id != null) {
                spec = spec.and((r, q, cb) -> cb.equal(r.get("id"), id));
            }

            if (employeeId != null) {
                spec = spec.and((r, q, cb) -> {
                    var employeeJoin = r.join("employee");
                    return cb.equal(employeeJoin.get("id"), employeeId);
                });
            }

            if (StringUtils.hasText(changeType)) {
                try {
                    CareerChanges.ChangeType ct = CareerChanges.ChangeType.valueOf(changeType);
                    spec = spec.and((r, q, cb) -> cb.equal(r.get("changeType"), ct));
                } catch (IllegalArgumentException e) {
                    // Ignore invalid changeType
                }
            }

            if (proposedById != null) {
                spec = spec.and((r, q, cb) -> {
                    var proposedByJoin = r.join("proposedBy");
                    return cb.equal(proposedByJoin.get("id"), proposedById);
                });
            }

            return spec.toPredicate(root, query, criteriaBuilder);
        };
    }
}
