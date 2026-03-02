package com.ct08.PharmacyManagement.modules.hr.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Positions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Positions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "position_name", nullable = false, unique = true, length = 100)
    private String positionName;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('Pending', 'Approved', 'Rejected') DEFAULT 'Pending'")
    private ApprovalStatus status;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "approval_reason", columnDefinition = "TEXT")
    private String approvalReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposed_by")
    private com.ct08.PharmacyManagement.modules.auth.entity.Users proposedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private com.ct08.PharmacyManagement.modules.auth.entity.Users approvedBy;

    public enum ApprovalStatus {
        Pending, Approved, Rejected
    }
}
