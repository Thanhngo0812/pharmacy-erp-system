# API Payroll - Quản lý Lương Hàng Tháng

## Tổng quan

| # | API | Method | Role | Mô tả |
|---|-----|--------|------|--------|
| 0 | `/api/v1/hr/payroll/my-salary` | GET | Tất cả NV | NV tự tra cứu lương |
| 1 | `/api/v1/hr/payroll/monthly` | GET | ADMIN, HM | Bảng lương tháng toàn bộ NV |
| 2 | `/api/v1/hr/payroll/monthly/{id}` | GET | ADMIN, HM | Chi tiết lương 1 NV |
| 3 | `/api/v1/hr/payroll/summary` | GET | ADMIN | Thống kê quỹ lương |
| 4 | `/api/v1/hr/payroll/monthly/export` | GET | ADMIN | Xuất bảng lương CSV |
| 5 | `/api/v1/hr/payroll/monthly/export/pdf` | GET | ADMIN | Xuất bảng lương PDF |

> **Phân quyền HM:** HM chỉ xem được NV có role `WS` hoặc `SS`.

---

## 1. Bảng lương tháng (Monthly Payroll)

- **URL:** `GET /api/v1/hr/payroll/monthly`
- **Authentication:** `Bearer Token`
- **Permissions:** `ADMIN`, `HM`

### Query Parameters

| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---|:---|
| `month` | int | ✅ | Tháng (1–12) |
| `year` | int | ✅ | Năm (2000–2100) |
| `status` | String | ❌ | Lọc trạng thái NV: `Active`, `Resigned` |
| `employeeId` | Integer | ❌ | Lọc theo ID nhân viên |
| `name` | String | ❌ | Tìm theo tên NV |
| `sortBy` | String | ❌ | `id`, `name`, `totalSalary`, `baseSalary` (mặc định: `id`) |
| `order` | String | ❌ | `asc` / `desc` (mặc định: `asc`) |

### Success Response (200 OK)

```json
{
  "status": "success",
  "message": "Monthly payroll calculated successfully",
  "data": {
    "month": 3,
    "year": 2026,
    "summary": {
      "totalEmployees": 12,
      "totalPayroll": 120000000,
      "totalAllowance": 6500000,
      "totalPenalty": -500000,
      "totalBonus": 6000000,
      "totalDeduction": 3500000
    },
    "employees": [
      {
        "employeeId": 5,
        "fullName": "Hoàng Stocker Thiên",
        "positionName": "Nhân viên kho",
        "baseSalary": 10000000,
        "unpaidLeaveDays": 2,
        "leaveDeduction": 645161,
        "totalAllowance": 500000,
        "totalPenalty": 0,
        "totalBonus": 500000,
        "totalSalary": 9854839
      }
    ]
  }
}
```

### Ví dụ gọi API

```bash
# Lấy bảng lương tháng 3/2026
curl -X GET "http://localhost:8080/api/v1/hr/payroll/monthly?month=3&year=2026" \
  -H "Authorization: Bearer <your_token>"

# Lọc NV Active, sắp xếp theo lương thực nhận giảm dần
curl -X GET "http://localhost:8080/api/v1/hr/payroll/monthly?month=3&year=2026&status=Active&sortBy=totalSalary&order=desc" \
  -H "Authorization: Bearer <your_token>"
```

---

## 2. Chi tiết lương 1 NV (Employee Payroll Detail)

- **URL:** `GET /api/v1/hr/payroll/monthly/{employeeId}`
- **Authentication:** `Bearer Token`
- **Permissions:** `ADMIN`, `HM`

### Path Parameters

| Tham số | Kiểu | Mô tả |
|:---|:---|:---|
| `employeeId` | int | ID nhân viên cần xem |

### Query Parameters

| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---|:---|
| `month` | int | ✅ | Tháng (1–12) |
| `year` | int | ✅ | Năm (2000–2100) |

### Success Response (200 OK)

```json
{
  "status": "success",
  "message": "Employee payroll detail calculated successfully",
  "data": {
    "employeeId": 5,
    "fullName": "Hoàng Stocker Thiên",
    "positionName": "Nhân viên kho",
    "month": 3,
    "year": 2026,
    "baseSalary": 10000000,
    "salaryChanges": [
      {
        "fromDate": "2026-03-01",
        "toDate": "2026-03-15",
        "salary": 9000000,
        "days": 15
      },
      {
        "fromDate": "2026-03-16",
        "toDate": "2026-03-31",
        "salary": 10000000,
        "days": 16
      }
    ],
    "unpaidLeaveDays": 2,
    "leaveDeduction": 645161,
    "leaveDetails": [
      {
        "startDate": "2026-03-10",
        "endDate": "2026-03-11",
        "days": 2,
        "type": "Approved"
      }
    ],
    "bonuses": [
      {
        "bonusId": 2,
        "bonusName": "Trợ cấp xăng xe (31 ngày)",
        "amount": 500000
      }
    ],
    "totalBonus": 500000,
    "totalSalary": 9854839
  }
}
```

### Giải thích response

| Trường | Mô tả |
|:---|:---|
| `salaryChanges` | Các giai đoạn lương trong tháng (nếu có thay đổi giữa tháng) |
| `leaveDetails` | Chi tiết từng đơn nghỉ: `Approved` = nghỉ không lương, `Approved_Salary` = nghỉ có lương |
| `bonuses` | Từng khoản trợ cấp/phạt, amount = dailyRate × activeDays |

### Ví dụ

```bash
curl -X GET "http://localhost:8080/api/v1/hr/payroll/monthly/5?month=3&year=2026" \
  -H "Authorization: Bearer <your_token>"
```

---

## 3. Thống kê quỹ lương (Payroll Summary)

- **URL:** `GET /api/v1/hr/payroll/summary`
- **Authentication:** `Bearer Token`
- **Permissions:** `ADMIN` only

### Query Parameters

| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---|:---|
| `fromMonth` | int | ✅ | Tháng bắt đầu |
| `fromYear` | int | ✅ | Năm bắt đầu |
| `toMonth` | int | ✅ | Tháng kết thúc |
| `toYear` | int | ✅ | Năm kết thúc |

### Success Response (200 OK)

```json
{
  "status": "success",
  "message": "Payroll summary retrieved successfully",
  "data": [
    {
      "month": 1,
      "year": 2026,
      "totalEmployees": 12,
      "totalPayroll": 118000000,
      "totalAllowance": 6500000,
      "totalPenalty": -1000000,
      "totalBonus": 5500000,
      "totalDeduction": 3200000
    },
    {
      "month": 2,
      "year": 2026,
      "totalEmployees": 12,
      "totalPayroll": 120000000,
      "totalAllowance": 6500000,
      "totalPenalty": -500000,
      "totalBonus": 6000000,
      "totalDeduction": 3500000
    }
  ]
}
```

### Ví dụ

```bash
curl -X GET "http://localhost:8080/api/v1/hr/payroll/summary?fromMonth=1&fromYear=2026&toMonth=3&toYear=2026" \
  -H "Authorization: Bearer <your_token>"
```

---

## 4. Xuất bảng lương CSV (Export)

- **URL:** `GET /api/v1/hr/payroll/monthly/export`
- **Authentication:** `Bearer Token`
- **Permissions:** `ADMIN` only
- **Response:** File CSV download

### Query Parameters

| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---|:---|
| `month` | int | ✅ | Tháng cần xuất |
| `year` | int | ✅ | Năm cần xuất |

### Response

File CSV với header:
```
Mã NV,Họ tên,Chức vụ,Lương cơ bản,Ngày nghỉ KL,Khấu trừ,Tổng trợ cấp,Lương thực nhận
```

### Ví dụ

```bash
curl -X GET "http://localhost:8080/api/v1/hr/payroll/monthly/export?month=3&year=2026" \
  -H "Authorization: Bearer <your_token>" \
  -o payroll_2026_3.csv
```

---

## 5. Xuất bảng lương PDF (Export Jasper)

Xuất bảng lương tháng dưới dạng file PDF đẹp mắt bằng Jasper Reports. Yêu cầu có data để đổ vào template PDF.

- **URL:** `GET /api/v1/hr/payroll/monthly/export/pdf`
- **Authentication:** `Bearer Token`
- **Permissions:** `ADMIN` only
- **Response:** File PDF download

### Query Parameters

| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---|:---|
| `month` | int | ✅ | Tháng cần xuất |
| `year` | int | ✅ | Năm cần xuất |

### Response

File PDF tải về với tên: `bang_luong_thang_MM_YYYY.pdf`

### Ví dụ

```bash
curl -X GET "http://localhost:8080/api/v1/hr/payroll/monthly/export/pdf?month=3&year=2026" \
  -H "Authorization: Bearer <your_token>" \
  -o bang_luong_thang_3_2026.pdf
```

---

## Error Responses chung

| HTTP Status | Trường hợp |
|:---|:---|
| **400 Bad Request** | `month` không nằm trong khoảng 1–12, `year` không hợp lệ, hoặc fromDate > toDate |
| **401 Unauthorized** | Chưa đăng nhập hoặc Token hết hạn |
| **403 Forbidden** | Không có quyền truy cập (VD: HM xem NV ngoài WS/SS, hoặc NV thường truy cập API ADMIN) |
| **404 Not Found** | Không tìm thấy nhân viên (API 2) |
