-- ==========================================================
-- 1. KHỞI TẠO HỆ THỐNG
-- ==========================================================
DROP DATABASE IF EXISTS PharmacyManagement;
CREATE DATABASE PharmacyManagement CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE PharmacyManagement;

-- ==========================================================
-- 2. QUẢN TRỊ HỆ THỐNG (ADMIN SERVER) 
-- ==========================================================

CREATE TABLE Roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE -- Admin, Manager, HR, Warehouse, Sales 
);

CREATE TABLE Positions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    position_name VARCHAR(100) NOT NULL UNIQUE -- Dược sĩ, Quản lý kho, Kế toán... 
);

CREATE TABLE Employees (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(15),
    image_url VARCHAR(255),
    current_position_id INT,
    current_salary DECIMAL(15, 2) DEFAULT 0,
    status ENUM('Active', 'Resigned') DEFAULT 'Active', -- Phù hợp yêu cầu cho nghỉ việc 
    hire_date DATE,
    FOREIGN KEY (current_position_id) REFERENCES Positions(id)
);

CREATE TABLE Users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (employee_id) REFERENCES Employees(id) ON DELETE SET NULL
);

CREATE TABLE User_Roles (
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES Roles(id) ON DELETE CASCADE
);

-- ==========================================================
-- 3. QUẢN LÝ NHÂN SỰ & BIẾN ĐỘNG (HR & MANAGER) 
-- ==========================================================

-- Đề xuất thay đổi lương/chức vụ để Quản lý duyệt 
CREATE TABLE Career_Changes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    change_type ENUM('Salary_Increase', 'Promotion', 'Bonus') NOT NULL,
    old_value VARCHAR(255),
    new_value VARCHAR(255),
    reason TEXT,
    status ENUM('Pending', 'Approved', 'Rejected') DEFAULT 'Pending',
    proposed_by INT, -- Nhân sự đề xuất
    approved_by INT, -- Quản lý duyệt
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES Employees(id),
    FOREIGN KEY (proposed_by) REFERENCES Users(id),
    FOREIGN KEY (approved_by) REFERENCES Users(id)
);

-- Đơn nghỉ phép/thai sản (leave_type là VARCHAR để tự nhập) 
CREATE TABLE Leave_Requests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    leave_type VARCHAR(100) NOT NULL, 
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    reason TEXT,
    status ENUM('Pending', 'Approved', 'Rejected') DEFAULT 'Pending',
    approved_by INT,
    FOREIGN KEY (employee_id) REFERENCES Employees(id),
    FOREIGN KEY (approved_by) REFERENCES Users(id)
);

-- Đơn xin nghỉ việc để quản lý duyệt 
CREATE TABLE Resignations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    reason TEXT,
    last_working_day DATE,
    status ENUM('Pending', 'Approved', 'Rejected') DEFAULT 'Pending',
    approved_by INT,
    FOREIGN KEY (employee_id) REFERENCES Employees(id),
    FOREIGN KEY (approved_by) REFERENCES Users(id)
);

-- Bảng lương lưu trữ lịch sử in bảng lương hàng tháng 
CREATE TABLE Payroll (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    month INT NOT NULL,
    year INT NOT NULL,
    base_salary DECIMAL(15, 2),
    bonus DECIMAL(15, 2) DEFAULT 0,
    deductions DECIMAL(15, 2) DEFAULT 0,
    total_salary DECIMAL(15, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES Employees(id)
);

-- ==========================================================
-- 4. DANH MỤC, SẢN PHẨM & ĐƠN VỊ TÍNH (KHO) 
-- ==========================================================

CREATE TABLE Categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE Suppliers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    phone VARCHAR(15)
);

CREATE TABLE Products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT,
    supplier_id INT,
    name VARCHAR(255) NOT NULL,
    image_url VARCHAR(255),
    min_stock_level INT DEFAULT 10,
    FOREIGN KEY (category_id) REFERENCES Categories(id),
    FOREIGN KEY (supplier_id) REFERENCES Suppliers(id)
);

-- Quản lý nhiều đơn vị tính (Viên, Vỉ, Hộp) và Giá niêm yết 
CREATE TABLE Product_Units (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    unit_name VARCHAR(50) NOT NULL,
    conversion_factor INT DEFAULT 1,
    is_base_unit BOOLEAN DEFAULT FALSE,
    listed_price DECIMAL(15, 2) NOT NULL, -- Giá bán niêm yết
    FOREIGN KEY (product_id) REFERENCES Products(id)
);

-- Lô hàng: Quản lý HSD và Giá nhập 
CREATE TABLE Batches (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    batch_code VARCHAR(50) NOT NULL,
    expiry_date DATE NOT NULL,
    quantity_remaining INT NOT NULL DEFAULT 0,
    purchase_price DECIMAL(15, 2) NOT NULL, -- Để tính lợi nhuận
    is_active BOOLEAN DEFAULT FALSE, -- Chỉ TRUE khi phiếu nhập được DUYỆT
    FOREIGN KEY (product_id) REFERENCES Products(id)
);

-- ==========================================================
-- 5. NHẬP KHO & BÁN HÀNG (COMMERCIAL) 
-- ==========================================================

-- Phiếu nhập: Nhân viên tạo, Quản lý duyệt 
CREATE TABLE Import_Notes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    supplier_id INT NOT NULL,
    created_by INT NOT NULL,
    approved_by INT,
    status ENUM('Pending', 'Approved', 'Rejected') DEFAULT 'Pending',
    total_value DECIMAL(15, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (supplier_id) REFERENCES Suppliers(id),
    FOREIGN KEY (created_by) REFERENCES Users(id),
    FOREIGN KEY (approved_by) REFERENCES Users(id)
);

CREATE TABLE Import_Details (
    id INT AUTO_INCREMENT PRIMARY KEY,
    import_note_id INT NOT NULL,
    product_id INT NOT NULL,
    batch_id INT, 
    quantity INT NOT NULL,
    unit_price DECIMAL(15, 2) NOT NULL,
    FOREIGN KEY (import_note_id) REFERENCES Import_Notes(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Products(id),
    FOREIGN KEY (batch_id) REFERENCES Batches(id)
);

-- Hóa đơn bán hàng 
CREATE TABLE Invoices (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sales_staff_id INT NOT NULL,
    customer_name VARCHAR(100),
    total_amount DECIMAL(15, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sales_staff_id) REFERENCES Users(id)
);

CREATE TABLE Invoice_Details (
    id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_id INT NOT NULL,
    product_id INT NOT NULL,
    batch_id INT NOT NULL,
    unit_id INT NOT NULL,
    quantity INT NOT NULL,
    selling_price DECIMAL(15, 2) NOT NULL, -- Giá bán thực tế
    FOREIGN KEY (invoice_id) REFERENCES Invoices(id) ON DELETE CASCADE,
    FOREIGN KEY (batch_id) REFERENCES Batches(id),
    FOREIGN KEY (unit_id) REFERENCES Product_Units(id)
);

-- ==========================================================
-- 6. AUDIT LOGS, MESSAGES & NOTIFICATIONS 
-- ==========================================================

CREATE TABLE Action_Logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    action VARCHAR(255),
    target_table VARCHAR(50),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(id)
);

CREATE TABLE Internal_Messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT, -- Có thể gửi đích danh cho Manager/Admin
    subject VARCHAR(255),
    content TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES Users(id),
    FOREIGN KEY (receiver_id) REFERENCES Users(id)
);