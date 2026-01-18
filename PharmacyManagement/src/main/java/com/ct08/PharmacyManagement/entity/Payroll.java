package com.ct08.PharmacyManagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Payroll")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payroll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employees employee;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "base_salary", precision = 15, scale = 2)
    private BigDecimal baseSalary;

    @Column(precision = 15, scale = 2)
    private BigDecimal bonus;

    @Column(precision = 15, scale = 2)
    private BigDecimal deductions;

    @Column(name = "total_salary", precision = 15, scale = 2)
    private BigDecimal totalSalary;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
