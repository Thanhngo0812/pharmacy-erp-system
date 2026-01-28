package com.ct08.PharmacyManagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Products {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Categories category;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Suppliers supplier;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "min_stock_level")
    private Integer minStockLevel;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('Active', 'Discontinued') DEFAULT 'Discontinued'")
    private ProductStatus status;

    public enum ProductStatus {
        Active, Discontinued
    }
}
