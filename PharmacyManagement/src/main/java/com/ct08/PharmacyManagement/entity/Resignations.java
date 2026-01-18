package com.ct08.PharmacyManagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "Resignations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resignations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employees employee;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "last_working_day")
    private LocalDate lastWorkingDay;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('Pending', 'Approved', 'Rejected') DEFAULT 'Pending'")
    private ApprovalStatus status;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private Users approvedBy;

    public enum ApprovalStatus {
        Pending, Approved, Rejected
    }
}
