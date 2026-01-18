package com.ct08.PharmacyManagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invoices {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "sales_staff_id", nullable = false)
    private Users salesStaff;

    @Column(name = "customer_name", length = 100)
    private String customerName;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
