USE PharmacyManagement;

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