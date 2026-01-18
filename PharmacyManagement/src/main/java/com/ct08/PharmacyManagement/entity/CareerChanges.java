package com.ct08.PharmacyManagement.entity;

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

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employees employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false)
    private ChangeType changeType;

    @Column(name = "old_value")
    private String oldValue;

    @Column(name = "new_value")
    private String newValue;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('Pending', 'Approved', 'Rejected') DEFAULT 'Pending'")
    private ApprovalStatus status;

    @ManyToOne
    @JoinColumn(name = "proposed_by")
    private Users proposedBy;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private Users approvedBy;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum ChangeType {
        Salary_Increase, Promotion, Bonus
    }

    public enum ApprovalStatus {
        Pending, Approved, Rejected
    }
}
