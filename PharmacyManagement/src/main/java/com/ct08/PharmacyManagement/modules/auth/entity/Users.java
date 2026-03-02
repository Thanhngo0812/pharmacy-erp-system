package com.ct08.PharmacyManagement.modules.auth.entity;

import com.ct08.PharmacyManagement.modules.hr.entity.Employees;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Users")
@Getter
@Setter
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", unique = true)
    private Employees employee;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "mailstatus", columnDefinition = "ENUM('sending', 'sended', 'failed') DEFAULT 'sending'")
    private String mailStatus;

    @Column(name = "reset_otp", length = 6)
    private String resetOtp;

    @Column(name = "otp_expiry")
    private LocalDateTime otpExpiry;

    @Column(name = "is_active")
    private Boolean isActive;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "User_Roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Roles> roles;

    public Users() {
    }

    public Users(Integer id, Employees employee, String username, String passwordHash, Boolean isActive,
            Set<Roles> roles) {
        this.id = id;
        this.employee = employee;
        this.username = username;
        this.passwordHash = passwordHash;
        this.isActive = isActive;
        this.roles = roles;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Employees getEmployee() {
        return employee;
    }

    public void setEmployee(Employees employee) {
        this.employee = employee;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Set<Roles> getRoles() {
        return roles;
    }

    public void setRoles(Set<Roles> roles) {
        this.roles = roles;
    }

    public String getMailStatus() {
        return mailStatus;
    }

    public void setMailStatus(String mailStatus) {
        this.mailStatus = mailStatus;
    }
}
