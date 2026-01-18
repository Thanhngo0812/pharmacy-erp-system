package com.ct08.PharmacyManagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "Action_Logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionLogs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @Column(length = 255)
    private String action;

    @Column(name = "target_table", length = 50)
    private String targetTable;

    @Column(insertable = false, updatable = false)
    private LocalDateTime timestamp;
}
