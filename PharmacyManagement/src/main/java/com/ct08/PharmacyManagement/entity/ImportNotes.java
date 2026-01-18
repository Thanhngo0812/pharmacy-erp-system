package com.ct08.PharmacyManagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Import_Notes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportNotes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = false)
    private Suppliers supplier;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private Users createdBy;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private Users approvedBy;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('Pending', 'Approved', 'Rejected') DEFAULT 'Pending'")
    private ApprovalStatus status;

    @Column(name = "total_value", precision = 15, scale = 2)
    private BigDecimal totalValue;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum ApprovalStatus {
        Pending, Approved, Rejected
    }
}
