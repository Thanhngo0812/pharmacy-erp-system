package com.ct08.PharmacyManagement.entity;

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

    @ManyToOne
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
    @Column(columnDefinition = "ENUM('Pending', 'Approved', 'Rejected') DEFAULT 'Pending'")
    private ApprovalStatus status;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @ManyToOne
    @JoinColumn(name = "proposed_by")
    private Users proposedBy;

    @ManyToOne
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
