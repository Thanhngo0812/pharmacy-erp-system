package com.ct08.PharmacyManagement.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Employees")
public class Employees {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(unique = true, length = 100)
    private String email;

    @Column(length = 15)
    private String phone;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "current_position_id")
    private Positions currentPosition;

    @Column(name = "current_salary", precision = 15, scale = 2)
    private BigDecimal currentSalary;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('Active', 'Resigned') DEFAULT 'Active'")
    private EmployeeStatus status;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    public enum EmployeeStatus {
        Active, Resigned
    }

    public Employees() {
    }

    public Employees(Integer id, String fullName, String email, String phone, String imageUrl, Positions currentPosition, BigDecimal currentSalary, EmployeeStatus status, LocalDate hireDate) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.imageUrl = imageUrl;
        this.currentPosition = currentPosition;
        this.currentSalary = currentSalary;
        this.status = status;
        this.hireDate = hireDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Positions getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(Positions currentPosition) {
        this.currentPosition = currentPosition;
    }

    public BigDecimal getCurrentSalary() {
        return currentSalary;
    }

    public void setCurrentSalary(BigDecimal currentSalary) {
        this.currentSalary = currentSalary;
    }

    public EmployeeStatus getStatus() {
        return status;
    }

    public void setStatus(EmployeeStatus status) {
        this.status = status;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }
}
