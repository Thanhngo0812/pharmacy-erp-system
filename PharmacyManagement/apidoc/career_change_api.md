# Quản lý Duyệt Phê Chuẩn Hồ Sơ Tuyển Dụng (Hired Career Changes) API

## 1. Lấy Danh Sách Hồ Sơ Tuyển Dụng (Hired)
API này cho phép Admin lập danh sách các nhân viên mới được tuyển dụng (đang nằm trong trạng thái `Waiting`) chờ được phê duyệt. Danh sách này có thể sắp xếp và thanh lọc theo trạng thái duyệt.

- **URL:** `/api/v1/career-changes/hired`
- **Method:** `GET`
- **Authentication:** `Bearer Token` (Quyền: `ROLE_ADMIN` hoặc `ROLE_HR`/`ROLE_HM`)

### Query Parameters
| Tham số | Kiểu dữ liệu | Trạng thái | Mặc định | Mô tả |
| :--- | :--- | :--- | :--- | :--- |
| `sortBy` | `string` | Tùy chọn | `id` | Trường để sắp xếp: `id`, `effectiveDate`, `newSalary` |
| `order` | `string` | Tùy chọn | `asc` | Hướng sắp xếp: `asc` (tăng dần) hoặc `desc` (giảm dần) |
| `status` | `string` | Tùy chọn | Bỏ qua | Lọc theo trạng thái phê duyệt: `Pending`, `Approved`, `Rejected` |
| `id` | `integer` | Tùy chọn | Bỏ qua | Tìm kiếm chính xác theo ID bộ hồ sơ |
| `employeeName` | `string` | Tùy chọn | Bỏ qua | Tìm kiếm tương đối theo tên hoặc họ của nhân viên |
| `proposedById` | `integer` | Tùy chọn | Bỏ qua | Tìm kiếm chính xác theo ID của người đề xuất (HR/HM) |

### Success Response (Thành công - 200 OK)
```json
{
    "success": true,
    "message": "Fetched hired career changes",
    "data": [
        {
            "id": 1,
            "employeeId": 105,
            "employeeName": "Nguyen Van A",
            "positionName": "Dược Sĩ",
            "effectiveDate": "2024-03-01",
            "newSalary": 15000000.00,
            "status": "Pending",
            "reason": "Tuyển dụng mới tháng 3",
            "proposedByName": "Tran Manager",
            "createdAt": "2024-02-28T10:15:30"
        }
    ]
}
```

### Error Responses
- **401 Unauthorized:** Token không hợp lệ hoặc thiếu Token.
- **403 Forbidden:** Người dùng không có quyền truy cập (không phải Admin hoặc HR).

---

## 2b. Lấy Danh Sách Đề Xuất Biến Động Nhân Sự (Non-Hired)
API lấy danh sách các đề xuất biến động (tăng lương, thăng chức, v.v.) — **không bao gồm** loại `Hired`. Hỗ trợ lọc và sắp xếp.

- **URL:** `/api/career-changes`
- **Method:** `GET`
- **Authentication:** `Bearer Token`
- **Permissions:** `ROLE_ADMIN`, `ROLE_HM`

### Query Parameters
| Tham số | Kiểu | Trạng thái | Mặc định | Mô tả |
| :--- | :--- | :--- | :--- | :--- |
| `sortBy` | string | Tùy chọn | `id` | Trường sắp xếp: `id`, `effectiveDate`, `newSalary` |
| `order` | string | Tùy chọn | `desc` | Hướng sắp xếp: `asc` hoặc `desc` |
| `status` | string | Tùy chọn | | Lọc: `Pending`, `Approved`, `Rejected` |
| `id` | integer | Tùy chọn | | Tìm theo ID của đề xuất |
| `employeeId` | integer | Tùy chọn | | Tìm theo ID nhân viên |
| `changeType` | string | Tùy chọn | | Lọc theo loại: `Salary_Increase`, `Promotion`, `Promotion_With_Salary`, `other` |
| `proposedById` | integer | Tùy chọn | | Tìm theo ID người đề xuất |

### Success Response (200 OK)
```json
{
    "success": true,
    "message": "Fetched career changes",
    "data": [
        {
            "id": 10,
            "employeeId": 5,
            "employeeName": "Nguyễn Ngọc Huy",
            "changeType": "Salary_Increase",
            "oldSalary": 10000000.00,
            "newSalary": 15000000.00,
            "oldPositionName": "Nhân viên kho",
            "newPositionName": null,
            "effectiveDate": "2025-03-01",
            "status": "Pending",
            "reason": "Hiệu suất tốt quý 4",
            "proposedById": 3,
            "proposedByName": "Phan Cẩm Cường",
            "approvedById": null,
            "approvedByName": null,
            "approvalReason": null
        }
    ]
}
```

### Error Responses
- **401 Unauthorized:** Token không hợp lệ.
- **403 Forbidden:** Không có quyền (không phải ADMIN hoặc HM).

---

## 2c. Lấy Danh Sách Đề Xuất Của Tôi (My Proposals)
API này dành cho HM để xem danh sách các đề xuất do chính mình tạo ra. Có bộ lọc giống y chang API **2b**.

- **URL:** `/api/career-changes/me`
- **Method:** `GET`
- **Authentication:** `Bearer Token` (Quyền: `ROLE_ADMIN`, `ROLE_HM`)

### Query Parameters
Bao gồm toàn bộ các tham số của API **2b** (trừ `proposedById` vì hệ thống tự động gán bằng ID của người thực hiện request).

### Success Response (200 OK)
Cấu trúc Response giống hệt API **2b**.

---

## 2. Duyệt Hoặc Từ Chối Hồ Sơ Tuyển Dụng (Approve/Reject Hired)
API này dùng để duyệt (`Approve`) hoặc từ chối (`Reject`) một bộ hồ sơ tuyển dụng. Khi được duyệt, nhân viên sẽ chuyển thành `Active`, hệ thống tự động tạo mật khẩu mới cho nhân viên và gửi về email đã đăng ký. Khi bị từ chối, tài khoản vẫn `Inactive` và hồ sơ bị đánh dấu `Rejected`.

- **URL:** `/api/v1/career-changes/hired/{id}/action`
- **Method:** `PUT`
- **Authentication:** `Bearer Token` (Quyền: Yêu cầu `ROLE_ADMIN`)

### Path Variable
- `id` (Integer): ID của record `CareerChange`

### Request Body
```json
{
    "isApproved": true,
    "reason": "Hồ sơ đầy đủ, đạt yêu cầu"
}
```

| Field | Type | Required | Mô tả |
| :--- | :--- | :--- | :--- |
| `isApproved` | boolean | ✅ | `true` để Duyệt, `false` để Từ chối |
| `reason` | string | ✅ | Lý do duyệt/từ chối |

### Success Response (Thành công - 200 OK)
```json
{
    "success": true,
    "message": "Hired career change approved successfully",
    "data": null
}
```

### Error Responses
- **400 Bad Request:** Dữ liệu Request body không hợp lệ (ví dụ thiếu trường `isApproved` hoặc `reason`).
- **401 Unauthorized:** Token không hợp lệ.
- **403 Forbidden:** Không có quyền thao tác (Yêu cầu quyền Admin).
- **404 Not Found:** Không tìm thấy hồ sơ CareerChange, Nhân viên(Employee), hoặc Tài khoản (User) cần duyệt.
- **409 Conflict:** 
  - Hành động này chỉ hỗ trợ cho loại hồ sơ `Hired`.
  - Hồ sơ này đã không còn ở trạng thái `Pending` (đã có người duyệt hoặc từ chối trước đó).

---

## 3. Tạo Đề Xuất Biến Động Nhân Sự (Create Career Change)
API này cho phép ADMIN hoặc HM tạo đề xuất biến động nhân sự cho nhân viên: tăng lương, thăng chức, thăng chức kèm tăng lương, hoặc khác.

- **URL:** `/api/career-changes`
- **Method:** `POST`
- **Authentication:** `Bearer Token`
- **Permissions:** `ROLE_ADMIN`, `ROLE_HM`

### Logic phân quyền
- **ADMIN**: Đề xuất cho **tất cả** nhân viên. Đề xuất **tự động duyệt** (`Approved`), cập nhật lương/chức vụ nhân viên ngay.
- **HM**: Chỉ đề xuất cho nhân viên có role **WS** hoặc **SS**. Đề xuất ở trạng thái **Pending**, chờ ADMIN duyệt.

### Request Body
```json
{
    "employeeId": 5,
    "changeType": "Salary_Increase",
    "newSalary": 15000000,
    "newPositionName": null,
    "effectiveDate": "2025-03-01",
    "reason": "Hiệu suất tốt quý 4"
}
```

| Field | Type | Required | Mô tả |
| :--- | :--- | :--- | :--- |
| `employeeId` | integer | ✅ | ID nhân viên được đề xuất |
| `changeType` | string | ✅ | Loại biến động (xem bảng dưới) |
| `newSalary` | decimal | Tùy loại | Mức lương mới |
| `newPositionName` | string | Tùy loại | Tên chức vụ mới (phải tồn tại và đã Approved) |
| `effectiveDate` | date | ✅ | Ngày hiệu lực (yyyy-MM-dd). Phải >= ngày hiện tại và >= ngày hiệu lực của đề xuất gần nhất |
| `reason` | string | ✅ | Lý do đề xuất |

### Quy tắc theo `changeType`

| changeType | `newSalary` | `newPositionName` | Điều kiện |
| :--- | :--- | :--- | :--- |
| `Salary_Increase` | ✅ Bắt buộc | ❌ | `newSalary` > lương hiện tại |
| `Promotion` | ❌ | ✅ Bắt buộc | Position phải khác position hiện tại |
| `Promotion_With_Salary` | ✅ Bắt buộc | ✅ Bắt buộc | `newSalary` > lương hiện tại + position khác |
| `Resigned` | ❌ | ❌ | Nhân viên phải đang `Active` |
| `Rehired` | ✅ Bắt buộc (nếu chưa có) | ⚪ Tùy chọn | Nhân viên phải đang `Resigned` (nghỉ việc) |
| `other` | ⚪ Tùy chọn | ⚪ Tùy chọn | Linh hoạt, không ràng buộc |

> **Lưu ý:**
> - `old_salary` và `old_position` tự động lấy từ thông tin nhân viên hiện tại. Đối với `Rehired`, `old_salary` gán tạm = 0. Đối với `Resigned`, `new_salary` tự lấy = 0.
> - Không thể tạo loại `Hired` qua endpoint này (dùng API duyệt tuyển dụng riêng).

### Success Response (201 OK)
```json
{
    "success": true,
    "message": "Career change created successfully",
    "data": null
}
```

### Error Responses

| HTTP Status | Trường hợp |
| :--- | :--- |
| **400 Bad Request** | `changeType` không hợp lệ, thiếu field bắt buộc, `newSalary` ≤ lương hiện tại, position trùng, `effectiveDate` trong quá khứ hoặc sớm hơn đề xuất gần nhất |
| **403 Forbidden** | HM đề xuất cho nhân viên không phải WS/SS, hoặc user không có quyền |
| **404 Not Found** | Employee hoặc Position không tồn tại |
| **409 Conflict** | Nhân viên không Active, Position chưa được duyệt |

---

## 4. Duyệt Hoặc Từ Chối Đề Xuất Biến Động (Approve/Reject Career Change)
API dành cho ADMIN duyệt hoặc từ chối các đề xuất biến động nhân sự (không bao gồm loại `Hired`). Khi duyệt, lương và/hoặc chức vụ nhân viên sẽ được cập nhật theo đề xuất.

- **URL:** `/api/career-changes/{id}/action`
- **Method:** `PUT`
- **Authentication:** `Bearer Token` (Quyền: Yêu cầu `ROLE_ADMIN`)

### Path Variable
- `id` (Integer): ID của record `CareerChange`

### Request Body
```json
{
    "isApproved": true,
    "reason": "Đồng ý tăng lương theo đề xuất"
}
```

| Field | Type | Required | Mô tả |
| :--- | :--- | :--- | :--- |
| `isApproved` | boolean | ✅ | `true` để Duyệt, `false` để Từ chối |
| `reason` | string | ✅ | Lý do duyệt/từ chối (lưu vào `approval_reason`) |

### Logic xử lý

| Hành động | Status | Side Effect |
| :--- | :--- | :--- |
| Duyệt (`isApproved: true`) | `Approved` | Nếu có `newSalary` → cập nhật `currentSalary`, nếu có `newPosition` → cập nhật `currentPosition` |
| Từ chối (`isApproved: false`) | `Rejected` | Không thay đổi gì trên nhân viên |

### Success Response (200 OK)
```json
{
    "success": true,
    "message": "Career change approved successfully",
    "data": null
}
```

### Error Responses

| HTTP Status | Trường hợp |
| :--- | :--- |
| **400 Bad Request** | Thiếu `isApproved` hoặc `reason` |
| **403 Forbidden** | Không phải ADMIN |
| **404 Not Found** | Không tìm thấy CareerChange hoặc Employee |
| **409 Conflict** | Record đã được xử lý (không còn Pending), hoặc loại `Hired` (dùng endpoint riêng) |

