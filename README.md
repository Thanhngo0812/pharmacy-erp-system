# 🏥 Pharmacy ERP System (PharmaMaster)
> A comprehensive Pharmacy Management Solution built with **Spring Boot 3** and **Modular Monolith Architecture**.

## 🌟 Project Overview
PharmaMaster is an Enterprise Resource Planning (ERP) system designed for pharmacy chains. It tackles complex real-world challenges such as **Batch Tracking**, **FEFO-based Inventory**, and **Granular Role-Based Access Control (RBAC)**.



## 🛠 Tech Stack
* **Backend:** Java 17+, Spring Boot 3.x
* **Database:** MySQL 8.0
* **Database Migration:** Flyway
* **Security:** Spring Security & JWT (Stateless Authentication)
* **Mapping:** MapStruct (Entity-DTO conversion)
* **Documentation:** SpringDoc OpenAPI (Swagger UI)
* **Utilities:** Project Lombok, Hibernate Validator

## ✨ Key Features
* **Smart Inventory Management:** Track products by **Batch Number** and **Expiry Date**.
* **FEFO Sales Strategy:** Automatic inventory deduction following **First Expired, First Out** rules.
* **Multi-Unit Conversion:** Manage sales across multiple units (e.g., Pill, Blister, Box) with automatic conversion rates.
* **Enterprise RBAC:** Fine-grained permissions for Admin, Branch Managers, and Pharmacists.
* **HR & Payroll:** Complete employee lifecycle management, including **Soft Delete offboarding** and salary history tracking.
* **Branch-Specific Settings:** Manage product availability and shelf locations per branch.

## 📐 Architecture: Modular Monolith
The project is structured into independent modules to ensure high maintainability and a clear path toward **Microservices** in the future.

* `modules/identity`: Authentication, JWT, and User management.
* `modules/core`: Branch and Global System configurations.
* `modules/hr`: Employee profiles, Payroll, and Leave requests.
* `modules/catalog`: Master data for Products, Categories, and Units.
* `modules/inventory`: Procurement (Purchase Orders) and Batch tracking.
* `modules/sales`: Point of Sale (POS) logic and Invoice generation.



## 🚀 Getting Started

### Prerequisites
* Java 17 or higher
* Maven 3.6+
* MySQL 8.0

### Installation
1. **Clone the repository:**
   ```bash
   git clone [https://github.com/your-username/pharmacy-erp-system.git](https://github.com/your-username/pharmacy-erp-system.git)
