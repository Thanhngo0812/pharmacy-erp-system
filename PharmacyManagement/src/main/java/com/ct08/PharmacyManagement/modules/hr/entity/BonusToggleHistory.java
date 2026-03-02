package com.ct08.PharmacyManagement.modules.hr.entity;

import com.ct08.PharmacyManagement.modules.auth.entity.Users;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Bonus_Toggle_History")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BonusToggleHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bonus_id", nullable = false)
    private Bonus bonus;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "toggled_at", insertable = false, updatable = false)
    private LocalDateTime toggledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "toggled_by")
    private Users toggledBy;

    @Column(columnDefinition = "TEXT")
    private String reason;
}
