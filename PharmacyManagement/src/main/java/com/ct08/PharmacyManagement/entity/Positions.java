package com.ct08.PharmacyManagement.entity;

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
}
