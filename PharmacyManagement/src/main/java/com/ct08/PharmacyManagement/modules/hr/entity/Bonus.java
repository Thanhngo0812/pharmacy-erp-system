package com.ct08.PharmacyManagement.modules.hr.entity;

import com.ct08.PharmacyManagement.modules.auth.entity.Users;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Bonus")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bonus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employees employee;

    @Column(name = "bonus_name", nullable = false)
    private String bonusName;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "request_status_enum DEFAULT 'Pending'")
    private ApprovalStatus status;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "approval_reason", columnDefinition = "TEXT")
    private String approvalReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposed_by")
    private Users proposedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Users approvedBy;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public enum ApprovalStatus {
        Pending, Approved, Rejected
    }
}
