# Quản Lý Thưởng / Trợ Cấp (Bonus)## APIs

### 1. Lọc danh sách nhân viên đủ điều kiện nhận phụ cấp/thưởng
Lấy danh sách các nhân viên **đang làm việc** (Active) và có ngày bắt đầu làm việc (hireDate) **nhỏ hơn hoặc bằng** ngày bắt đầu (`startDate`) của khoản tính phụ cấp.

- **URL:** `GET /api/v1/hr/bonuses/eligible-employees`
- **Method:** `GET`
- **Role Requirement:** `ADMIN`, `HM` (HM chỉ xem được NV thuộc role `WS` hoặc `SS`)

**Query Parameters:**
| Tham số | Loại | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `startDate` | Date (YYYY-MM-DD) | Có | Ngày bắt đầu khoảng thời gian trợ cấp (dùng để lọc NV đã vào làm trước/tại mốc này) |
| `endDate` | Date (YYYY-MM-DD) | Không | Ngày kết thúc khoảng thời gian (tùy chọn) |

**Response (Success 200 OK):**
```json
{
  "status": "success",
  "message": "Fetched eligible employees successfully",
  "data": [
    {
      "id": 10,
      "lastName": "Nguyễn",
      "firstName": "Văn A",
      "email": "vana@example.com",
      "phone": "0901234567",
      "imageUrl": "ava.png",
      "currentPosition": {
        "id": 1,
        "positionName": "Store Manager"
      },
      "currentSalary": 15000000.00,
      "status": "Active",
      "hireDate": "2023-01-15"
    }
  ]
}
```

---

### 2. Danh Sách Phụ Cấp (Gom nhóm) - Cập nhật
Lấy danh sách các phụ cấp đã được gom nhóm theo tên khoản phụ cấp, khoảng thời gian hiệu lực và số tiền. Tích hợp thanh tìm kiếm và bộ lọc.

- **URL:** `GET /api/v1/hr/bonuses`
- **Method:** `GET`
- **Authentication:** `Bearer Token`
- **Permissions:** `ROLE_ADMIN`, `ROLE_HM`

### Phân quyền
| Role | Phạm vi xem |
| :--- | :--- |
| **ADMIN** | Xem **tất cả** khoản thưởng |
| **HM** | Chỉ xem thưởng của NV có role **WS** hoặc **SS** |

### Query Parameters
| Tham số | Kiểu | Trạng thái | Mô tả |
| :--- | :--- | :--- | :--- |
| `bonusName` | string | Tùy chọn | Tìm kiếm tương đối theo tên khoản thưởng |
| `minAmount` | decimal | Tùy chọn | Tìm kiếm trợ cấp có số tiền từ mức này trở lên |
| `maxAmount` | decimal | Tùy chọn | Tìm kiếm trợ cấp có số tiền từ mức này trở xuống |
| `startDate` | date (ISO) | Tùy chọn | Trợ cấp bắt đầu từ ngày này trở đi |
| `endDate` | date (ISO) | Tùy chọn | Trợ cấp kết thúc trước ngày này |
| `sortDirection` | string | Tùy chọn | Sắp xếp theo số tiền: `asc` hoặc `desc` |
| `status` | string | Tùy chọn | Lọc theo trạng thái: `Pending`, `Approved`, `Rejected` |

### Success Response (200 OK)
```json
{
    "success": true,
    "message": "Fetched bonuses successfully",
    "data": [
        {
            "bonusName": "Trợ cấp xăng xe",
            "amount": 500000.00,
            "startDate": "2026-03-01",
            "endDate": "2026-05-01",
            "reason": "Hỗ trợ chi phí đi lại",
            "status": "Approved",
            "approvalReason": "Đồng ý hỗ trợ nhân viên",
            "proposedByName": "Phan Cẩm Cường",
            "approvedByName": "Nguyễn Văn An",
            "employeeCount": 3,
            "activeCount": 2,
            "employees": [
                {
                    "bonusId": 1,
                    "employeeId": 4,
                    "employeeName": "Phạm Warehouse Thu",
                    "positionName": "Trưởng kho",
                    "isActive": true,
                    "status": "Approved"
                },
                {
                    "bonusId": 2,
                    "employeeId": 5,
                    "employeeName": "Hoàng Stocker Thiên",
                    "positionName": "Nhân viên kho",
                    "isActive": true,
                    "status": "Approved"
                },
                {
                    "bonusId": 3,
                    "employeeId": 6,
                    "employeeName": "Vũ Seller Hán",
                    "positionName": "Dược sĩ bán hàng",
                    "isActive": false,
                    "status": "Approved"
                }
            ]
        },
        {
            "bonusName": "Thưởng hiệu suất Q1",
            "amount": 2000000.00,
            "startDate": "2026-03-01",
            "endDate": "2026-03-01",
            "reason": "Hoàn thành xuất sắc chỉ tiêu",
            "status": "Pending",
            "approvalReason": null,
            "proposedByName": "Phan Cẩm Cường",
            "approvedByName": null,
            "employeeCount": 2,
            "activeCount": 2,
            "employees": [
                {
                    "bonusId": 4,
                    "employeeId": 5,
                    "employeeName": "Hoàng Stocker Thiên",
                    "positionName": "Nhân viên kho",
                    "isActive": true,
                    "status": "Pending"
                },
                {
                    "bonusId": 5,
                    "employeeId": 6,
                    "employeeName": "Vũ Seller Hán",
                    "positionName": "Dược sĩ bán hàng",
                    "isActive": true,
                    "status": "Pending"
                }
            ]
        }
    ]
}
```

### Cách gom nhóm
Các khoản thưởng được gom vào **cùng nhóm** khi có cùng:
- `bonus_name` (tên khoản thưởng)
- `start_date` (ngày bắt đầu)
- `end_date` (ngày kết thúc)
- `amount` (số tiền)

> **Ví dụ:** "Trợ cấp xăng xe" 500k từ 3/1→5/1 cho NV 4, 5, 6 → gom thành 1 nhóm. Nếu NV 4 được nâng `end_date` thành 12/1 → tách ra nhóm riêng.

### Cách tính thưởng theo tháng
- `start_date = 2026-03-01`, `end_date = 2026-05-01` → **3 tháng** (T3, T4, T5) → 500k × 3 = 1.5M
- `start_date = end_date = 2026-03-01` → **1 tháng** (T3) → thưởng 1 lần
- `end_date = NULL` → thưởng vô thời hạn, tính đến tháng hiện tại

### Error Responses
| HTTP Status | Trường hợp |
| :--- | :--- |
| **401 Unauthorized** | Token không hợp lệ hoặc thiếu Token |
| **403 Forbidden** | Không có quyền (không phải ADMIN hoặc HM) |

---

### Ghi chú thiết kế

#### Bảng `Bonus`
| Cột | Kiểu | Mô tả |
| :--- | :--- | :--- |
| `id` | SERIAL PK | ID khoản thưởng |
| `employee_id` | INT FK | Nhân viên được thưởng |
| `bonus_name` | VARCHAR(255) | Tên khoản thưởng |
| `amount` | DECIMAL(15,2) | Số tiền mỗi tháng |
| `start_date` | DATE | Ngày bắt đầu áp dụng |
| `end_date` | DATE (nullable) | Ngày kết thúc (NULL = vô thời hạn) |
| `status` | ENUM | Pending / Approved / Rejected |
| `is_active` | BOOLEAN | Trạng thái bật/tắt |
| `reason` | TEXT | Lý do đề xuất thưởng |
| `approval_reason` | TEXT | Lý do duyệt/từ chối |
| `proposed_by` | INT FK | Người đề xuất |
| `approved_by` | INT FK | Người duyệt |

#### Bảng `Bonus_Toggle_History`
Lưu lịch sử mỗi lần bật/tắt `is_active`, phục vụ truy xuất lương cũ/mới theo tháng.

| Cột | Kiểu | Mô tả |
| :--- | :--- | :--- |
| `id` | SERIAL PK | ID lịch sử |
| `bonus_id` | INT FK | Khoản thưởng |
| `is_active` | BOOLEAN | true = bật, false = tắt |
| `toggled_at` | TIMESTAMP | Ngày giờ thao tác |
| `toggled_by` | INT FK | Người thao tác |
| `reason` | TEXT | Lý do bật/tắt |

## 2. Duy?t / T? ch?i Kho?n thu?ng (Single)

API duy?t m?t kho?n thu?ng cho 1 nh�n vi�n.

- **URL:** /api/v1/hr/bonuses/{id}/action`r
- **Method:** PUT`r
- **Authentication:** Bearer Token`r
- **Permissions:** ROLE_ADMIN, ROLE_HM`r

### Request Body
| Tru?ng | Ki?u | B?t bu?c | M� t? |
| :--- | :--- | :--- | :--- |
| status | ENUM | Yes | Approved ho?c Rejected |
| pprovalReason | string | No | L� do duy?t/t? ch?i |

`json
{
  "status": "Approved",
  "approvalReason": "Khen thu?ng nh�n vi�n xu?t s?c trong th�ng"
}
``r

---

## 3. Duy?t / T? ch?i Kho?n thu?ng (Bulk)

API duy?t h�ng lo?t kho?n thu?ng cho nhi?u nh�n vi�n c�ng l�c.

- **URL:** /api/v1/hr/bonuses/bulk/action`r
- **Method:** PUT`r

### Request Body
| Tru?ng | Ki?u | B?t bu?c | M� t? |
| :--- | :--- | :--- | :--- |
| onusIds | array | Yes | Danh s�ch c�c ID kho?n thu?ng c?n duy?t |
| status | ENUM | Yes | Approved ho?c Rejected |
| pprovalReason | string | No | L� do |

`json
{
  "bonusIds": [1, 2, 3],
  "status": "Approved",
  "approvalReason": "Duy?t chung cho d?t thu?ng L?"
}
``r

---

## 4. S?a Kho?n thu?ng (Single)

API s?a th�ng tin onusName v� endDate c?a 1 nh�n vi�n.

- **URL:** /api/v1/hr/bonuses/{id}`r
- **Method:** PUT`r

### Request Body
| Tru?ng | Ki?u | B?t bu?c | M� t? |
| :--- | :--- | :--- | :--- |
| onusName | string | Yes | T�n kho?n thu?ng m?i |
| endDate | string (Date) | No | Ng�y k?t th�c m?i (Kh�ng du?c s?m hon h�m nay ho?c startDate) |

`json
{
  "bonusName": "C?p nh?t t�n kho?n thu?ng",
  "endDate": "2026-12-31"
}
``r

---

## 5. S?a Kho?n thu?ng (Bulk)

API s?a th�ng tin onusName v� endDate c?a nhi?u nh�n vi�n c�ng l�c.

- **URL:** /api/v1/hr/bonuses/bulk`r
- **Method:** PUT`r

### Request Body
| Tru?ng | Ki?u | B?t bu?c | M� t? |
| :--- | :--- | :--- | :--- |
| onusIds | array | Yes | Danh s�ch c�c ID kho?n thu?ng (nh�m) c?n s?a |
| onusName | string | Yes | T�n kho?n thu?ng m?i |
| endDate | string (Date) | No | Ng�y k?t th�c m?i |

`json
{
  "bonusIds": [4, 5],
  "bonusName": "T�n thu?ng d� s?a",
  "endDate": "2026-12-31"
}
``r


## 6. Xoá nhóm khoản thưởng (Bulk Delete)

API cho phép xoá (huỷ hoàn toàn) nhiều khoản thưởng, nhưng chỉ chấp nhận những khoản thưởng có trạng thái là `Rejected`.

- **URL:** /api/v1/hr/bonuses/bulk`r
- **Method:** DELETE`r
- **Authentication:** Bearer Token`r
- **Permissions:** ROLE_ADMIN, ROLE_HM`r

### Query Parameters
| Tham số | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| ids | array (query) | Yes | Danh sách các ID khoản thưởng cần xoá (VD: ?ids=1,2,3) |

### Error Responses

### Query Parameters
| Tham số | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| ids | array (query) | Yes | Danh sách các ID khoản thưởng cần xoá (VD: ?ids=1,2,3) |

### Error Responses
| HTTP Status | Trường hợp |
| :--- | :--- |
| **400 Bad Request** | Không cung cấp danh sách ID, hoặc có khoản thưởng không nằm ở trạng thái Rejected. |


## 7. Tạo mới nhóm Khoản thưởng (Create Bonus)

API cho phép Admin hoặc HM tạo một khoản bù/khấu trừ mới cho nhiều nhân viên cùng lúc.
- **Lưu ý:** Nếu người tạo mang quyền `ROLE_ADMIN`, hệ thống sẽ tự động duyệt (`Approved`) khoản thưởng đó. Nếu người tạo mang quyền `ROLE_HM`, khoản thưởng sẽ được khởi tạo với trạng thái chờ duyệt (`Pending`).

- **URL:** /api/v1/hr/bonuses`r
- **Method:** POST`r
- **Authentication:** Bearer Token`r
### Request Body (JSON)
| Tham số | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| employeeIds | array | Yes | Danh sách các ID nhân viên sẽ nhận khoản bù phép này |
| onusName | string | Yes | Tên lý do bù phép/trừ phép |
| mount | decimal | Yes | Giá trị (VD: 50.0) |
| startDate | string | Yes | Ngày hiệu lực bắt đầu (YYYY-MM-DD) |
| endDate | string | No | Ngày kết thúc hiệu lực (YYYY-MM-DD), phải lớn hơn hoặc bằng startDate và ngày dự kiến |
| 
eason | string | No | Ghi chú thêm (khởi tạo) |

### Error Responses
| HTTP Status | Trường hợp |
| :--- | :--- |
| **400 Bad Request** | Danh sách nhân viên bị trống hoặc Ngày cung cấp không hợp lệ. |
| **403 Forbidden** | Không được cấp quyền do không có phận sự, hoặc HM cố tình tạo cho Manager. |

## 8. Bật/tắt trạng thái hiển thị (Toggle Active)

API cho phép bật hoặc tắt hàng loạt các khoản thưởng đang áp dụng, có lưu lịch sử hệ thống vào Bonus_Toggle_History.

- **URL:** /api/v1/hr/bonuses/bulk/toggle-active`r
- **Method:** PUT`r
- **Authentication:** Bearer Token`r
- **Permissions:** ROLE_ADMIN, ROLE_HM`r

### Request Body (JSON)
| Tham số | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| onusIds | array | Yes | Danh sách các ID khoản thưởng |
| isActive | boolean | Yes | Trạng thái hiển thị mới (	rue hoặc alse) |
| 
eason | string | No | Lý do cho việc thay đổi trên |

### Error Responses
| HTTP Status | Trường hợp |
| :--- | :--- |
| **400 Bad Request** | Không cung cấp danh sách ID hoặc có biến bị rỗng. |
| **403 Forbidden** | Cố tình Toggle loại thưởng ngoài tầm quản lý. |


*Lưu ý: tham số `amount` hiện đã hỗ trợ giá trị âm để sử dụng cho mục đích Khấu trừ/Phạt (Penalty).*


## 9. Lịch sử hiển thị khoản thưởng (Toggle History)

API cho phép truy xuất lịch sử bật tắt (Active/Inactive) của một khoản thưởng bất kỳ.

- **URL:** /api/v1/hr/bonuses/{id}/history`r
- **Method:** GET`r
- **Authentication:** Bearer Token`r
- **Permissions:** ROLE_ADMIN, ROLE_HM (HM chỉ xem được lịch sử khoản thưởng của nhân viên thuộc khối SS hoặc WS)

### Error Responses
| HTTP Status | Trường hợp |
| :--- | :--- |
| **403 Forbidden** | Cố tình xem lịch sử chức vụ ngoài tầm phân quyền bảo mật. |
| **404 Not Found** | Khoản thưởng không tồn tại. |

