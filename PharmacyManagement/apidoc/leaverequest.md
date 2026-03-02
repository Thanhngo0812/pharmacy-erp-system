# Leave Request API Documentation

## Base URL
`/api/v1/hr/leave-requests`

---

## 1. Create Leave Request
Tạo một đơn xin nghỉ phép mới.

- **URL:** `/`
- **Method:** `POST`
- **Auth required:** Yes
- **Roles Allowed:** Tất cả roles. Đặc biệt nếu người tạo là `ROLE_ADMIN`, đơn sẽ tự động chuyển sang trạng thái `Approved` ngay lập tức.

### Request Body
```json
{
  "leaveType": "Sick Leave",
  "startDate": "2026-03-01T08:00:00",
  "endDate": "2026-03-02T17:00:00",
  "reason": "Bị ốm cần đi khám bệnh",
  "isPaidLeave": true 
}
```
*Lưu ý: `isPaidLeave` là tham số không bắt buộc (mặc định là `false`). Tham số này chỉ có tác dụng khi người gửi là `ROLE_ADMIN` để hệ thống tự động duyệt trạng thái thành `Approved_Salary` (nếu true) hoặc `Approved` (nếu false).*

### Business Rules validation:
- `startDate` phải là ngày tương lai (ít nhất là ngày hôm sau so với ngày hiện tại).
- `startDate` phải nhỏ hơn `endDate`.
- Khoảng thời gian `[startDate, endDate]` KHÔNG được phép trùng (overlap) với bất kỳ đơn xin nghỉ phép nào khác của chính nhân viên đó đang ở trạng thái `Pending`, `Approved` hoặc `Approved_Salary`.

### Responses
- **201 Created**: Đăng ký thành công.
- **400 Bad Request**: Nếu `startDate` không hợp lệ.
- **409 Conflict**: Nếu khoảng thời gian đăng ký bị trùng lịch (Overlap) với đơn khác.

---

## 2. Approve/Reject Leave Request
Duyệt hoặc Từ chối một đơn xin nghỉ phép (Dành cho Quản lý).

- **URL:** `/{id}/approve`
- **Method:** `PUT`
- **Auth required:** Yes
- **Roles Allowed:** `ROLE_ADMIN`, `ROLE_HM`

### Request Body
```json
{
  "status": "Approved_Salary", // Hoặc "Approved", "Rejected"
  "approvalReason": "Đồng ý cho nghỉ phép không lương do lý do cá nhân hợp lý" 
}
```
*Lưu ý: Trường `approvalReason` là BẮT BUỘC. Bỏ trống sẽ trả về lỗi 400 Bad Request.*

### Business Rules validation:
- `ROLE_ADMIN` được quyền duyệt tất cả phiếu nghỉ.
- `ROLE_HM` CHỈ được quyền duyệt phiếu nghỉ của các nhân viên có ROLE là `ROLE_WS` hoặc `ROLE_SS`. Nếu cố tình duyệt của người khác -> Báo lỗi 403 Forbidden.
- Đầu ra của danh sách (Get All / Get My Requests) sẽ trả về thêm trường `approvalReason` nếu đơn đã được duyệt/từ chối.

### Responses
- **200 OK**: Cập nhật trạng thái thành công.
- **403 Forbidden**: Người duyệt không có quyền duyệt đơn này.
- **404 Not Found**: Không tìm thấy LeaveRequest id.

---

## 3. Get All Leave Requests (For Managers)
Lấy danh sách các đơn xin nghỉ phép (Dành cho Quản lý quản trị viên).

- **URL:** `/`
- **Method:** `GET`
- **Auth required:** Yes
- **Roles Allowed:** `ROLE_ADMIN`, `ROLE_HM`

### Query Parameters
- `status` (Optional): Lọc theo trạng thái (`Pending`, `Approved`, `Approved_Salary`, `Rejected`).
- `employeeId` (Optional): Lọc theo ID nhân viên.
- `startDate` (Optional): Ngày lấy đơn tính từ ngày này trở đi, định dạng ISO `YYYY-MM-DD`.
- `endDate` (Optional): Ngày lấy đơn tính đến ngày này trở về trước, định dạng ISO `YYYY-MM-DD`.

*Lưu ý*: Có thể nhập `startDate` mà không cần `endDate` (tìm tất cả đơn từ thời điểm A trở đi) hoặc ngược lại (tìm tất cả đơn đến thời điểm B).

### Business Rules:
- `ROLE_ADMIN` nhìn thấy toàn bộ danh sách đơn từ hệ thống.
- `ROLE_HM` CHỈ nhìn thấy danh sách đơn của những nhân viên thuộc `ROLE_WS` hoặc `ROLE_SS`.

### Responses
- **200 OK**: Trả về danh sách đơn. Mỗi đơn (LeaveRequestResponseDTO) sẽ bao gồm trường `reason` (lý do xin nghỉ) và `approvalReason` (lý do duyệt/từ chối từ quản lý nếu có).
- **403 Forbidden**: Gọi khi không phải HM hoặc ADMIN.

---

## 4. Get My Leave Requests
Lấy danh sách các đơn xin nghỉ phép của chính nhân viên đang đăng nhập.

- **URL:** `/my-requests`
- **Method:** `GET`
- **Auth required:** Yes
- **Roles Allowed:** All roles

### Query Parameters
- `status` (Optional): Lọc theo trạng thái (`Pending`, `Approved`, `Approved_Salary`, `Rejected`).

### Responses
- **200 OK**: Trả về danh sách các phiếu nghỉ của chính tài khoản (dựa theo employee_id). Thông tin trả về bao gồm trường `reason` (lý do xin nghỉ) và `approvalReason` (lý do mà quản lý đã duyệt/từ chối).

---

## 5. Delete Leave Request
Xóa đơn xin nghỉ phép (Hủy đơn).

- **URL:** `/{id}`
- **Method:** `DELETE`
- **Auth required:** Yes
- **Roles Allowed:** All roles (Chỉ có thể xóa phiếu của chính mình)

### Business Rules validation:
- Chỉ người TẠO phiếu mới được phép xóa phiếu của mình.
- Chỉ được phép xóa khi phiếu đang ở trạng thái `Pending`. Không thể xóa nếu đã `Approved` hoặc `Rejected`.

### Responses
- **200 OK**: Xóa thành công.
- **400 Bad Request**: Nếu đơn xin nghỉ phép không ở trạng thái Pending.
- **403 Forbidden**: Cố gắng xóa đơn của người khác.
- **404 Not Found**: Không tìm thấy LeaveRequest id.
