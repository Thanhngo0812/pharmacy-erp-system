# Thiết lập lại Mật khẩu (Password Reset) - Auth Module

Các endpoints này phục vụ việc gửi OTP về email và nhận OTP để đổi lại mật khẩu trong trường hợp quên mật khẩu.

Hệ thống cho phép bất kỳ ai (public acesss) có thể yêu cầu Forgot Password, sau đó dùng OTP để đổi lại mật khẩu mới thông qua các RESTful endpoints sau.

---

## 1. Yêu cầu Cấp lại Mật khẩu (Forgot Password)

Endpoint này nhận email từ người dùng và sẽ tạo OTP sau đó gửi OTP đó thông qua email dưới định dạng HTML 6 chữ số. OTP sẽ hết hạn trong vòng 5 phút sau khi khởi tạo.

- **URL:** `/api/auth/forgot-password`
- **Method:** `POST`
- **Authentication:** None

### Request Body
```json
{
  "email": "user.name@example.com"
}
```

### Responses
- **200 OK**
  - Trả về thông báo thành công sau khi email Kafka Event được đưa vào hàng đợi.
  ```json
  {
      "status": "success",
      "message": "Mã OTP đã được gửi đến email của bạn",
      "data": null
  }
  ```
- **400 Bad Request**
  - Nếu email bị trống hoặc sai định dạng.
- **404 Not Found**
  - Nếu email không tồn tại trong hệ thống (UsernameNotFoundException).

---

## 2. Thiết lập lại Mật khẩu qua OTP (Reset Password)

Sau khi người dùng đã check email và có OTP 6 chữ số, họ gọi endpoint này và cấp mật khẩu mới để hệ thống đổi lại.

- **URL:** `/api/auth/reset-password`
- **Method:** `POST`
- **Authentication:** None

### Request Body
```json
{
  "email": "user.name@example.com",
  "otp": "260902",
  "newPassword": "newsecurepassword123"
}
```

### Responses
- **200 OK**
  - Trả về thông báo thành công và mật khẩu của người dùng tự động được cập nhật. OTP sau đó được xoá ngay lập tức để tránh tái sử dụng.
  ```json
  {
      "status": "success",
      "message": "Đổi mật khẩu thành công",
      "data": null
  }
  ```
- **400 Bad Request**
  - Nếu `otp` không khớp hoặc đã hết hạn: `Invalid OTP` hoặc `OTP has expired` trong message error.
  - Các Validation fields trống hoặc không thoả mãn kích thước (vd OTP != 6 ký tự).
- **404 Not Found**
  - Nếu email không tồn tại trong hệ thống.
