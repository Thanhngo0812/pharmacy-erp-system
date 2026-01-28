package com.ct08.PharmacyManagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class PharmacyManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(PharmacyManagementApplication.class, args);
        // 1. Khởi tạo đối tượng Encoder
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "123456";

        // 2. Mã hóa mật khẩu (Hashing)
        String hashedPassword = encoder.encode(password);

        System.out.println("Chuỗi gốc: " + password);
        System.out.println("Kết quả băm BCrypt: " + hashedPassword);

        // 3. Kiểm tra mật khẩu (Matching)
        // Lưu ý: Không dùng hàm equals() thông thường, phải dùng matches()
        String userInput = "123456";

        if (encoder.matches(userInput, hashedPassword)) {
            System.out.println("=> Mật khẩu chính xác!");
        } else {
            System.out.println("=> Mật khẩu sai!");
        }
	}

}
