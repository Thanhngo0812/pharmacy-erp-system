-- ==========================================================
-- 1. KHỞI TẠO HỆ THỐNG
-- ==========================================================
-- PostgreSQL doesn't support 'CREATE DATABASE' inside a transaction block or script easily in the same way.
-- Usually the database is created beforehand (e.g. by docker-compose environment vars).
-- We will skip CREATE DATABASE and USE.

-- ==========================================================
-- 2. QUẢN TRỊ HỆ THỐNG (ADMIN SERVER) 
-- ==========================================================

CREATE TABLE Roles (
    id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE -- Admin, Manager, HR, Warehouse, Sales 
);

CREATE TABLE Positions (
    id SERIAL PRIMARY KEY,
    position_name VARCHAR(100) NOT NULL UNIQUE -- Dược sĩ, Quản lý kho, Kế toán... 
);

-- Create custom types for ENUMs
CREATE TYPE employee_status_enum AS ENUM ('Active', 'Resigned');

CREATE TABLE Employees (
    id SERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(15),
    image_url VARCHAR(255),
    current_position_id INT,
    current_salary DECIMAL(15, 2) DEFAULT 0,
    status employee_status_enum DEFAULT 'Active', -- Phù hợp yêu cầu cho nghỉ việc 
    hire_date DATE,
    FOREIGN KEY (current_position_id) REFERENCES Positions(id)
);

CREATE TABLE Users (
    id SERIAL PRIMARY KEY,
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

CREATE TYPE career_change_type_enum AS ENUM ('Hired','Salary_Increase', 'Promotion', 'Promotion_With_Salary','other');
CREATE TYPE request_status_enum AS ENUM ('Pending', 'Approved', 'Rejected');
CREATE TYPE leave_status_enum AS ENUM ('Pending', 'Approved','Approved_Salary' ,'Rejected');

CREATE TABLE Career_Changes (
    id SERIAL PRIMARY KEY,
    employee_id INT NOT NULL,
    
    -- Phân loại rõ ràng để logic Backend dễ xử lý
    change_type career_change_type_enum NOT NULL,

    -- Chuyển từ VARCHAR sang DECIMAL để tính toán trực tiếp, không cần ép kiểu (CAST)
    old_salary DECIMAL(15, 2) DEFAULT 0,
    new_salary DECIMAL(15, 2) DEFAULT 0,

    -- Lưu vết thay đổi chức vụ (nếu có)
    old_position_id INT,
    new_position_id INT,

    -- CỘT QUAN TRỌNG NHẤT: Ngày bắt đầu áp dụng thay đổi này
    -- Ví dụ: Duyệt ngày 10, nhưng 15 mới bắt đầu tính lương mới
    effective_date DATE NOT NULL, 

    status request_status_enum DEFAULT 'Pending',    
    reason TEXT,
    proposed_by INT, -- Nhân sự đề xuất
    approved_by INT, -- Quản lý duyệt
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (employee_id) REFERENCES Employees(id),
    FOREIGN KEY (old_position_id) REFERENCES Positions(id),
    FOREIGN KEY (new_position_id) REFERENCES Positions(id),
    FOREIGN KEY (proposed_by) REFERENCES Users(id),
    FOREIGN KEY (approved_by) REFERENCES Users(id)
);

-- Đơn nghỉ phép/thai sản (leave_type là VARCHAR để tự nhập) 
CREATE TABLE Leave_Requests (
    id SERIAL PRIMARY KEY,
    employee_id INT NOT NULL,
    leave_type VARCHAR(100) NOT NULL, 
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    reason TEXT,
    status leave_status_enum DEFAULT 'Pending',
    approved_by INT,
    FOREIGN KEY (employee_id) REFERENCES Employees(id),
    FOREIGN KEY (approved_by) REFERENCES Users(id)
);

-- Đơn xin nghỉ việc để quản lý duyệt 
CREATE TABLE Resignations (
    id SERIAL PRIMARY KEY,
    employee_id INT NOT NULL,
    reason TEXT,
    last_working_day DATE,
    status request_status_enum DEFAULT 'Pending',
    approved_by INT,
    FOREIGN KEY (employee_id) REFERENCES Employees(id),
    FOREIGN KEY (approved_by) REFERENCES Users(id)
);

-- ==========================================================
-- BỔ SUNG: QUẢN LÝ THƯỞNG ĐỊNH KỲ
-- ==========================================================

CREATE TABLE Bonus (
    id SERIAL PRIMARY KEY,
    employee_id INT NOT NULL,
    bonus_name VARCHAR(255) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    start_date DATE NOT NULL,          -- Ngày bắt đầu áp dụng
    end_date DATE DEFAULT NULL,        -- Ngày kết thúc (nếu có)
    
    -- Quy trình duyệt
    status request_status_enum DEFAULT 'Pending',
    is_active BOOLEAN DEFAULT TRUE,    -- Dùng để tạm ngưng khoản thưởng sau khi đã duyệt
    
    reason TEXT,                       -- Lý do thưởng
    proposed_by INT,                   -- ID nhân sự/quản lý tạo đề xuất
    approved_by INT,                   -- ID sếp/admin duyệt
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (employee_id) REFERENCES Employees(id),
    FOREIGN KEY (proposed_by) REFERENCES Users(id),
    FOREIGN KEY (approved_by) REFERENCES Users(id)
);

-- Trigger for updated_at in Bonus table
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_bonus_updated_at
    BEFORE UPDATE ON Bonus
    FOR EACH ROW
    EXECUTE PROCEDURE update_updated_at_column();

-- ==========================================================
-- 4. DANH MỤC, SẢN PHẨM & ĐƠN VỊ TÍNH (KHO) 
-- ==========================================================

CREATE TABLE Categories (
    id SERIAL PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE Suppliers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    phone VARCHAR(15)
);

CREATE TYPE product_status_enum AS ENUM ('Active', 'Discontinued');

CREATE TABLE Products (
    id SERIAL PRIMARY KEY,
    category_id INT,
    supplier_id INT,
    name VARCHAR(255) NOT NULL,
    image_url VARCHAR(255),
    min_stock_level INT DEFAULT 10,
    status product_status_enum DEFAULT 'Discontinued',
    FOREIGN KEY (category_id) REFERENCES Categories(id),
    FOREIGN KEY (supplier_id) REFERENCES Suppliers(id)
);

-- Quản lý nhiều đơn vị tính (Viên, Vỉ, Hộp/cai -> thung) và Giá niêm yết 
CREATE TABLE Product_Units (
    id SERIAL PRIMARY KEY,
    product_id INT NOT NULL,
    unit_name VARCHAR(50) NOT NULL,
    conversion_factor INT DEFAULT 1,
    is_base_unit BOOLEAN DEFAULT FALSE,
    listed_price DECIMAL(15, 2) NOT NULL, -- Giá bán niêm yết
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (product_id) REFERENCES Products(id)
);

-- Lô hàng: Quản lý HSD và Giá nhập 
CREATE TABLE Batches (
    id SERIAL PRIMARY KEY,
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
    id SERIAL PRIMARY KEY,
    supplier_id INT NOT NULL,
    created_by INT NOT NULL,
    approved_by INT,
    status request_status_enum DEFAULT 'Pending',
    total_value DECIMAL(15, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (supplier_id) REFERENCES Suppliers(id),
    FOREIGN KEY (created_by) REFERENCES Users(id),
    FOREIGN KEY (approved_by) REFERENCES Users(id)
);

CREATE TABLE Import_Details (
    id SERIAL PRIMARY KEY,
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
    id SERIAL PRIMARY KEY,
    sales_staff_id INT NOT NULL,
    customer_name VARCHAR(100),
    total_amount DECIMAL(15, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sales_staff_id) REFERENCES Users(id)
);

CREATE TABLE Invoice_Details (
    id SERIAL PRIMARY KEY,
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
    id SERIAL PRIMARY KEY,
    user_id INT,
    action VARCHAR(255),
    target_table VARCHAR(50),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(id)
);

CREATE TABLE Internal_Messages (
    id SERIAL PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT, -- Có thể gửi đích danh cho Manager/Admin
    subject VARCHAR(255),
    content TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES Users(id),
    FOREIGN KEY (receiver_id) REFERENCES Users(id)
);


-- ==========================================================
-- 1. TẠO CÁC ROLE (VAI TRÒ)
-- ==========================================================
INSERT INTO Roles (role_name) VALUES 
('ROLE_ADMIN'),   -- Admin Server
('ROLE_MANAGER'), -- Quản lý
('ROLE_HM'),      -- Quản lý nhân sự (Human Resource Manager)
('ROLE_WM'),      -- Quản lý kho (Warehouse Manager)
('ROLE_WS'),      -- Nhân viên kho (Warehouse Staff)
('ROLE_SS');      -- Nhân viên bán hàng (Sales Staff)

-- ==========================================================
-- 2. TẠO CÁC CHỨC VỤ (POSITIONS)
-- ==========================================================
INSERT INTO Positions (position_name) VALUES 
('Quản trị hệ thống'),
('Quản lý chi nhánh'),
('Trưởng phòng nhân sự'),
('Trưởng kho'),
('Nhân viên kho'),
('Dược sĩ bán hàng');

-- ==========================================================
-- 3. TẠO NHÂN VIÊN (EMPLOYEES)
-- ==========================================================
INSERT INTO Employees (full_name, email, phone, current_position_id, current_salary, hire_date, status) VALUES 
('Nguyễn Admin', 'admin@pharmacy.com', '0901111111', 1, 30000000, '2024-01-01', 'Active'),
('Trần Manager', 'manager@pharmacy.com', '0902222222', 2, 25000000, '2024-01-01', 'Active'),
('Lê HR', 'hm@pharmacy.com', '0903333333', 3, 20000000, '2024-01-10', 'Active'),
('Phạm Warehouse', 'wm@pharmacy.com', '0904444444', 4, 18000000, '2024-01-15', 'Active'),
('Hoàng Stocker', 'ws@pharmacy.com', '0905555555', 5, 10000000, '2024-01-20', 'Active'),
('Vũ Seller', 'ss@pharmacy.com', '0906666666', 6, 12000000, '2024-01-25', 'Active');

-- ==========================================================
-- 4. TẠO TÀI KHOẢN NGƯỜI DÙNG (USERS)
-- Mật khẩu chung: $2a$10$6Nd8ldRPTu/wxsXnVP1/ruYQZdnftPnDmfN2.6azl.BwUvpMYpEby
-- ==========================================================
INSERT INTO Users (employee_id, username, password_hash, is_active) VALUES 
(1, 'admin@pharmacy.com', '$2a$10$6Nd8ldRPTu/wxsXnVP1/ruYQZdnftPnDmfN2.6azl.BwUvpMYpEby', TRUE),
(2, 'manager@pharmacy.com', '$2a$10$6Nd8ldRPTu/wxsXnVP1/ruYQZdnftPnDmfN2.6azl.BwUvpMYpEby', TRUE),
(3, 'hm@pharmacy.com', '$2a$10$6Nd8ldRPTu/wxsXnVP1/ruYQZdnftPnDmfN2.6azl.BwUvpMYpEby', TRUE),
(4, 'wm@pharmacy.com', '$2a$10$6Nd8ldRPTu/wxsXnVP1/ruYQZdnftPnDmfN2.6azl.BwUvpMYpEby', TRUE),
(5, 'ws@pharmacy.com', '$2a$10$6Nd8ldRPTu/wxsXnVP1/ruYQZdnftPnDmfN2.6azl.BwUvpMYpEby', TRUE),
(6, 'ss@pharmacy.com', '$2a$10$6Nd8ldRPTu/wxsXnVP1/ruYQZdnftPnDmfN2.6azl.BwUvpMYpEby', TRUE);

-- ==========================================================
-- 5. PHÂN QUYỀN (USER_ROLES)
-- ==========================================================
INSERT INTO User_Roles (user_id, role_id) VALUES 
(1, 1), -- Admin -> ROLE_ADMIN
(2, 2), -- Manager -> ROLE_MANAGER
(3, 3), -- HR -> ROLE_HM
(4, 4), -- Warehouse Manager -> ROLE_WM
(5, 5), -- Warehouse Staff -> ROLE_WS
(6, 6); -- Sales Staff -> ROLE_SS

-- ==========================================================
-- 6. GHI NHẬN BIẾN ĐỘNG LƯƠNG/HỢP ĐỒNG LÚC MỚI VÀO (CAREER_CHANGES)
-- Thiết lập trạng thái 'Hired' và 'Approved' (Lấy user Admin id=1 duyệt)
-- ==========================================================
INSERT INTO Career_Changes (employee_id, change_type, old_salary, new_salary, old_position_id, new_position_id, effective_date, status, reason, proposed_by, approved_by) VALUES 
(1, 'Hired', 0, 30000000, NULL, 1, '2024-01-01', 'Approved', 'Tuyển dụng mới - Quản trị hệ thống', 1, 1),
(2, 'Hired', 0, 25000000, NULL, 2, '2024-01-01', 'Approved', 'Tuyển dụng mới - Quản lý', 1, 1),
(3, 'Hired', 0, 20000000, NULL, 3, '2024-01-10', 'Approved', 'Tuyển dụng mới - Quản lý nhân sự', 1, 1),
(4, 'Hired', 0, 18000000, NULL, 4, '2024-01-15', 'Approved', 'Tuyển dụng mới - Trưởng kho', 1, 1),
(5, 'Hired', 0, 10000000, NULL, 5, '2024-01-20', 'Approved', 'Tuyển dụng mới - Nhân viên kho', 1, 1),
(6, 'Hired', 0, 12000000, NULL, 6, '2024-01-25', 'Approved', 'Tuyển dụng mới - Dược sĩ bán hàng', 1, 1);
