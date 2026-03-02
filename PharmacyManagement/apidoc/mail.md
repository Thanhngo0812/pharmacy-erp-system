# Mail System Documentation

Hệ thống gửi Email sử dụng thư viện `spring-boot-starter-mail` và dịch vụ SMTP của **SendGrid**. Các thao tác gửi mail trong hệ thống đều chạy bất đồng bộ qua mô hình Event-Driven nhằm tránh ảnh hưởng tốc độ phản hồi của API.

## 1. Luồng hoạt động (Event-Driven với Kafka)

Mỗi khi một email cần được gửi đi, hệ thống sẽ thực hiện theo các bước:

1. **Trigger Component** (Ví dụ: `EmployeeService`): Sinh ra mật khẩu ngẫu nhiên an toàn (8 ký tự) mới và thông báo sự kiện sử dụng `KafkaProducerService`. 
2. **Event Topic**: Đẩy bản ghi JSON của object `PasswordEmailEvent(email, fullName, newPassword)` vào topic `user-password-email`.
3. **Worker**: `MailWorker` chứa hàm chạy bất đồng bộ bằng Kafka Listener (`@KafkaListener`) sẽ lắng nghe thông điệp trên topic `user-password-email`.
4. **Service**: `MailService` thực hiện tạo Mime Message với Template HTML tùy chỉnh và gửi qua `JavaMailSender` đến SendGrid.

## 2. Các Action Kích Hoạt Gửi Mail

### 2.1. Tại thời điểm Tạo Mới Nhân Viên (Create Employee)
- **Logic**: Khi Admin hoặc HR Manager tạo mới một Employee qua API `/api/employees`.
- **Hành vi**:
  - Hệ thống tự động thiết lập một tài khoản đăng nhập (User) cho Email tương ứng.
  - Sinh mật khẩu ngẫu nhiên gồm 8 ký tự.
  - Gửi thư thông báo với template HTML chứa Tên, Email Đăng Nhập, và Mật khẩu.

### 2.2. Tại thời điểm Cập Nhật Hồ Sơ Nhân Viên (Update Employee / Profile)
- **Logic**: Khi nhân viên cập nhật thông tin cá nhân hoặc Quản trị viên cập nhật thông tin nhân viên, và **Email bị thay đổi** (`request.getEmail()` khác với email hiện tại).
- **Hành vi**: 
  - Khởi tạo mật khẩu mới ngẫu nhiên (8 ký tự).
  - Gắn mật khẩu mới này cho User.
  - Gửi thư thông báo vào Email MỚI vừa đổi, cung cấp thông tin đăng nhập và mật khẩu mới để đảm bảo tính xác thực chủ sở hữu địa chỉ email.

## 3. Giao diện (Email Template)

Email được gửi đi dạng HTML (MimeMessage) với thiết kế:
- **Logo**: Hiển thị trên đầu bảng tin.
- **Màu chủ đạo**: `#65A7E3` được sử dụng làm màu nền phần tiêu đề logo và làm màu viền, điểm nhấn.
- **Nội dung**: Thông báo về việc thiết lập tài khoản và cấp mật khẩu rành mạch, đi kèm lưu ý đề nghị đổi mật khẩu sau khi đăng nhập phần mềm.

## 4. Troubleshooting (Xử lý sự cố)

Nếu email không được gửi thành công:
1. **Kiểm tra thông tin Application.yaml**: Xác minh config API key SendGrid tại `spring.mail.password`.
2. **Verified Sender SendGrid**: Địa chỉ email tại hàm config `helper.setFrom("nhquang.bkhn@gmail.com")` bên trong `MailService` **BẮT BUỘC** phải được xác thực "Sender Identity" trên trang quản trị Dashboard của SendGrid, nếu không thì SendGrid sẽ từ chối gửi email.
