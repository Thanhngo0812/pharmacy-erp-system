package com.ct08.PharmacyManagement.modules.hr.entity;

import com.ct08.PharmacyManagement.modules.auth.entity.Users;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "Career_Changes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CareerChanges {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employees employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false)
    private ChangeType changeType;

    @Column(name = "old_salary", precision = 15, scale = 2)
    private java.math.BigDecimal oldSalary;

    @Column(name = "new_salary", precision = 15, scale = 2)
    private java.math.BigDecimal newSalary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_position_id")
    private Positions oldPosition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_position_id")
    private Positions newPosition;

    @Column(name = "effective_date", nullable = false)
    private java.time.LocalDate effectiveDate;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('Pending', 'Approved', 'Rejected') DEFAULT 'Pending'")
    private ApprovalStatus status;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposed_by")
    private Users proposedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Users approvedBy;

    @Column(name = "approval_reason", columnDefinition = "TEXT")
    private String approvalReason;

    @Column(name = "is_applied")
    private Boolean isApplied = false;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum ChangeType {
        Hired, Salary_Increase, Promotion, Promotion_With_Salary, other, Resigned, Rehired
    }

    public enum ApprovalStatus {
        Pending, Approved, Rejected
    }
}
