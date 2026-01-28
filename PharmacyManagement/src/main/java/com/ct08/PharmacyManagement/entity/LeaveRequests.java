package com.ct08.PharmacyManagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "Leave_Requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequests {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employees employee;

    @Column(name = "leave_type", nullable = false, length = 100)
    private String leaveType;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('Pending', 'Approved','Approved_Salary','Rejected') DEFAULT 'Pending'")
    private ApprovalStatus status;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private Users approvedBy;

    public enum ApprovalStatus {
        Pending, Approved, Approved_Salary, Rejected
    }
}
