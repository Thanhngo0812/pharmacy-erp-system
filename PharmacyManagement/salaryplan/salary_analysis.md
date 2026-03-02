# Phân Tích Tính Lương Tháng X / Năm Y

## 1. Bối cảnh

Hệ thống hiện tại lưu trữ **lương cơ bản hiện hành** (`current_salary`) trực tiếp trên bảng `Employees`, và mỗi khi có biến động lương (tăng lương, thăng chức, nghỉ việc, tái tuyển…), hệ thống ghi nhận vào `Career_Changes` với `effective_date` và `new_salary`. Ngoài ra còn có bảng `Bonus` (trợ cấp / phạt) và `Leave_Requests` (nghỉ phép).

Mục tiêu: Xây dựng **API tính lương tháng** cho phép truy vấn: *"Nhân viên X nhận bao nhiêu tiền trong tháng M/năm Y?"*

---

## 2. Các nguồn dữ liệu ảnh hưởng đến lương tháng

### 2.1. Lương cơ bản — `Career_Changes`

| Cột | Ý nghĩa |
|---|---|
| `employee_id` | Nhân viên |
| `new_salary` | Mức lương mới |
| `effective_date` | Ngày **bắt đầu áp dụng** lương mới |
| `status` | Phải là `Approved` |
| `is_applied` | `true` = đã được áp dụng bởi scheduler |
| `change_type` | `Hired`, `Salary_Increase`, `Promotion_With_Salary`, `Resigned`, `Rehired`… |

> **Quy tắc:** Lương cơ bản của nhân viên tại **một thời điểm bất kỳ** = `new_salary` của bản ghi `Career_Changes` cuối cùng (đã Approved + is_applied) có `effective_date <= thời điểm đó`.

**Trường hợp đặc biệt — giữa tháng thay đổi lương:**
- Nếu `effective_date` nằm giữa tháng M, cần tính lương **theo tỷ lệ ngày**:
  - Giai đoạn 1: Ngày 1 → `effective_date - 1` → Áp dụng mức lương cũ
  - Giai đoạn 2: `effective_date` → cuối tháng → Áp dụng mức lương mới

### 2.2. Trợ cấp / Phạt — `Bonus`

| Cột | Ý nghĩa |
|---|---|
| `employee_id` | Nhân viên |
| `amount` | Giá trị (dương = thưởng, âm = phạt) |
| `start_date` | Ngày bắt đầu áp dụng |
| `end_date` | Ngày kết thúc (NULL = vô thời hạn) |
| `status` | Phải là `Approved` |
| `is_active` | `true` = đang hiệu lực |

> **Quy tắc áp dụng:** Một khoản `Bonus` được tính vào tháng M/Y **khi và chỉ khi**:
> 1. `status = 'Approved'`
> 2. `is_active = true`
> 3. `start_date <= cuối tháng M/Y` (tháng đó đã bắt đầu áp dụng)
> 4. `end_date IS NULL` HOẶC `end_date >= đầu tháng M/Y` (chưa hết hạn)

### 2.3. Nghỉ phép — `Leave_Requests`

| Cột | Ý nghĩa |
|---|---|
| `employee_id` | Nhân viên |
| `start_date` / `end_date` | Khoảng thời gian nghỉ |
| `status` | Trạng thái duyệt |

Hệ thống có **3 trạng thái** nghỉ phép liên quan đến lương:

| Trạng thái | Ý nghĩa lương |
|---|---|
| `Approved_Salary` | Nghỉ **có lương** (thai sản, phép năm…) → **Không trừ lương** |
| `Approved` | Nghỉ **không lương** → **Trừ lương theo số ngày nghỉ** |
| `Pending` / `Rejected` | Không ảnh hưởng |

> **Quy tắc:** Nếu trong tháng M/Y nhân viên có đơn nghỉ `Approved` (không lương) và khoảng thời gian nghỉ nằm trong tháng M/Y, trừ lương tỷ lệ theo số ngày nghỉ.

---

## 3. Công thức tính lương tháng

```
LƯƠNG_THÁNG = LƯƠNG_CƠ_BẢN_THÁNG - KHẤU_TRỪ_NGHỈ_KHÔNG_LƯƠNG + TỔNG_TRỢ_CẤP
```

### 3.1. LƯƠNG_CƠ_BẢN_THÁNG

```
Số ngày trong tháng = N (VD: 30, 31, 28…)
Lương ngày = current_salary / N

Nếu có thay đổi lương effective_date giữa tháng:
  LƯƠNG_CƠ_BẢN = (Lương_cũ × số_ngày_trước) + (Lương_mới × số_ngày_sau)
                  ────────────────────────────────────────────────────────
                                        N
Nếu không có thay đổi:
  LƯƠNG_CƠ_BẢN = current_salary (toàn bộ tháng)
```

### 3.2. KHẤU_TRỪ_NGHỈ_KHÔNG_LƯƠNG

```
Lọc Leave_Requests:
  - status = 'Approved' (KHÔNG phải Approved_Salary)
  - Giao cắt thời gian nghỉ với tháng M/Y
  - Tính số ngày nghỉ thực tế trong tháng

KHẤU_TRỪ = Lương_ngày × Số_ngày_nghỉ_không_lương
```

### 3.3. TỔNG_TRỢ_CẤP / PHẠT

Một khoản `Bonus` có `amount` là số tiền **mỗi ngày**. Với hệ thống cho phép Bật/Tắt trợ cấp, số ngày thực nhận được tính bằng cách đối soát với lịch sử thay đổi (`Bonus_Toggle_History`).

```
Lọc Bonus:
  - status = 'Approved'
  - Giao cắt thời gian [start_date, end_date] với tháng M/Y

Xác định trạng thái từng ngày trong tháng:
  1. Lấy trạng thái mặc định (isActive hiện tại) và lịch sử Bật/Tắt.
  2. Một ngày được tính là Active nếu tại thời điểm đó trạng thái gần nhất là Active.
  3. Đếm số ngày Active thực tế trong tháng = d.

TRỢ_CẤP_KHOẢN_I = amount × d
TỔNG_TRỢ_CẤP = SUM(TRỢ_CẤP_KHOẢN_I)
```

---

## 4. Thiết kế API

### Endpoint đề xuất

```
GET /api/v1/hr/payroll?month=3&year=2026
```

**Response:**
```json
{
  "data": {
    "month": 3,
    "year": 2026,
    "employees": [
      {
        "employeeId": 5,
        "fullName": "Hoàng Stocker Thiên",
        "positionName": "Nhân viên kho",
        "baseSalary": 10000000,
        "unpaidLeaveDays": 2,
        "leaveDeduction": 666667,
        "bonuses": [
          { "bonusName": "Trợ cấp xăng xe", "amount": 500000 }
        ],
        "totalBonus": 500000,
        "totalSalary": 9833333
      }
    ],
    "totalPayroll": 95000000,
    "totalEmployees": 6
  }
}
```

### Query Parameters

| Tham số | Kiểu | Bắt buộc | Mô tả |
|---|---|---|---|
| `month` | int | Yes | Tháng (1–12) |
| `year` | int | Yes | Năm |
| `employeeId` | int | No | Lọc theo NV cụ thể |
| `status` | string | No | Lọc theo trạng thái NV (`Active`) |

---

## 5. Kế hoạch Implementation

### Cần tạo mới

| File | Mô tả |
|---|---|
| `PayrollResponseDTO.java` | DTO tổng thể chứa danh sách NV + tổng quỹ lương |
| `EmployeePayrollDTO.java` | DTO chi tiết lương cho từng NV |
| `BonusDetailDTO.java` | DTO chi tiết từng khoản trợ cấp/phạt áp dụng |
| `PayrollService.java` | Service tính toán lương theo tháng/năm |
| `PayrollController.java` | Controller expose API GET |

### Cần sửa

| File | Mô tả |
|---|---|
| `BonusRepository.java` | Thêm query lọc Bonus theo tháng/năm + trạng thái |
| `CareerChangesRepository.java` | Thêm query tìm lương cơ bản tại một thời điểm |
| `LeaveRequestsRepository.java` | Thêm query lọc nghỉ phép theo tháng/năm |

---

## 6. Verification Plan

### Automated Tests
- Biên dịch project: `.\mvnw.cmd compile`

### Manual Verification
1. Tạo dữ liệu mẫu: NV có lương thay đổi giữa tháng, có bonus active, có ngày nghỉ không lương.
2. Gọi API `GET /api/v1/hr/payroll?month=3&year=2026` và verify từng thành phần lương.
3. Kiểm tra edge cases: NV nghỉ việc, NV mới vào giữa tháng, bonus hết hạn giữa tháng.
