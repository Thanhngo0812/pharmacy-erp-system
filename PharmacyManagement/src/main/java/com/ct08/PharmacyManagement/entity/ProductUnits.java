package com.ct08.PharmacyManagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "Product_Units")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUnits {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Products product;

    @Column(name = "unit_name", nullable = false, length = 50)
    private String unitName;

    @Column(name = "conversion_factor")
    private Integer conversionFactor;

    @Column(name = "is_base_unit")
    private Boolean isBaseUnit;

    @Column(name = "listed_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal listedPrice;
}
