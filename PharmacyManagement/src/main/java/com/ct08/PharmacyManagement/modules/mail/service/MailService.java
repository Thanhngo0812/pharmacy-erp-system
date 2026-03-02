package com.ct08.PharmacyManagement.modules.mail.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MailService {

    @Value("${resend.api-key}")
    private String resendApiKey;

    private Resend resend;

    @PostConstruct
    public void init() {
        this.resend = new Resend(resendApiKey);
    }

    public void sendPasswordEmail(String toEmail, String fullName, String newPassword) {
        String htmlContent = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden;\">"
                +
                "<div style=\"background-color: #65A7E3; padding: 20px; text-align: center;\">" +
                "<img src=\"https://res.cloudinary.com/dfcb3zzw9/image/upload/v1771660017/logo_qjedds.png\" alt=\"Pharmacy ERP Logo\" style=\"max-height: 80px;\"/>"
                +
                "</div>" +
                "<div style=\"padding: 30px; background-color: #ffffff;\">" +
                "<h2 style=\"color: #333333; margin-top: 0;\">Xin chào " + fullName + ",</h2>" +
                "<p style=\"color: #555555; font-size: 16px; line-height: 1.5;\">Tài khoản của bạn trên hệ thống Pharmacy ERP vừa được tạo mới hoặc cập nhật email. Dưới đây là thông tin đăng nhập của bạn:</p>"
                +
                "<div style=\"background-color: #f9f9f9; padding: 15px; border-left: 4px solid #65A7E3; margin: 20px 0;\">"
                +
                "<p style=\"margin: 5px 0; font-size: 16px;\"><strong>Email đăng nhập:</strong> " + toEmail + "</p>"
                +
                "<p style=\"margin: 5px 0; font-size: 16px;\"><strong>Mật khẩu:</strong> <span style=\"color: #65A7E3; font-weight: bold;\">"
                + newPassword + "</span></p>" +
                "</div>" +
                "<p style=\"color: #555555; font-size: 16px; line-height: 1.5;\">Vui lòng đăng nhập và thay đổi mật khẩu của bạn ngay để đảm bảo bảo mật.</p>"
                +
                "<p style=\"color: #555555; font-size: 16px; line-height: 1.5; margin-bottom: 0;\">Trân trọng,<br/>Đội ngũ Quản trị Hệ thống</p>"
                +
                "</div>" +
                "<div style=\"background-color: #f0f0f0; padding: 15px; text-align: center; color: #888888; font-size: 12px;\">"
                +
                "<p style=\"margin: 0;\">Đây là email tự động, vui lòng không phản hồi.</p>" +
                "</div>" +
                "</div>";

        // IMPORTANT: We use onboarding@resend.dev as default since it's the
        // fallback test domain. The recipient must be the account creator's email
        // address
        // until a domain is verified.
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("no-reply@pharmacyerpsystem.me") // You must change this to your verified domain (e.g.
                // info@yourdomain.com) for production
                .to(toEmail)
                .subject("Thông tin tài khoản và Mật khẩu mới")
                .html(htmlContent)
                .build();

        try {
            CreateEmailResponse data = resend.emails().send(params);
            log.info("Email sent successfully via Resend. Response ID: {}", data.getId(),
                    StructuredArguments.kv("TYPE_LOG", "EMAIL"));
        } catch (ResendException e) {
            log.error("Failed to send email via Resend to {}", toEmail,
                    StructuredArguments.kv("TYPE_LOG", "EMAIL"), e);
        }
    }

    public void sendOtpEmail(String toEmail, String fullName, String otp) {
        String htmlContent = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden;\">"
                +
                "<div style=\"background-color: #65A7E3; padding: 20px; text-align: center;\">" +
                "<img src=\"https://res.cloudinary.com/dfcb3zzw9/image/upload/v1771660017/logo_qjedds.png\" alt=\"Pharmacy ERP Logo\" style=\"max-height: 80px;\"/>"
                +
                "</div>" +
                "<div style=\"padding: 30px; background-color: #ffffff;\">" +
                "<h2 style=\"color: #333333; margin-top: 0;\">Xin chào " + fullName + ",</h2>" +
                "<p style=\"color: #555555; font-size: 16px; line-height: 1.5;\">Bạn vừa yêu cầu cấp lại mật khẩu. Mã xác thực (OTP) của bạn là:</p>"
                +
                "<div style=\"background-color: #f9f9f9; padding: 20px; border-radius: 4px; text-align: center; margin: 20px 0;\">"
                +
                "<span style=\"font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #65A7E3;\">" + otp
                + "</span>"
                +
                "</div>" +
                "<p style=\"color: #555555; font-size: 16px; line-height: 1.5;\">Mã OTP này có hiệu lực trong vòng 5 phút. Nếu bạn không yêu cầu cấp lại mật khẩu, xin vui lòng bỏ qua email này.</p>"
                +
                "<p style=\"color: #555555; font-size: 16px; line-height: 1.5; margin-bottom: 0;\">Trân trọng,<br/>Đội ngũ Quản trị Hệ thống</p>"
                +
                "</div>" +
                "<div style=\"background-color: #f0f0f0; padding: 15px; text-align: center; color: #888888; font-size: 12px;\">"
                +
                "<p style=\"margin: 0;\">Đây là email tự động, vui lòng không phản hồi.</p>" +
                "</div>" +
                "</div>";

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("no-reply@pharmacyerpsystem.me")
                .to(toEmail)
                .subject("Mã xác thực cấp lại mật khẩu (OTP)")
                .html(htmlContent)
                .build();

        try {
            CreateEmailResponse data = resend.emails().send(params);
            log.info("OTP Email sent successfully via Resend. Response ID: {}", data.getId(),
                    StructuredArguments.kv("TYPE_LOG", "EMAIL"));
        } catch (ResendException e) {
            log.error("Failed to send OTP email via Resend to {}", toEmail,
                    StructuredArguments.kv("TYPE_LOG", "EMAIL"), e);
        }
    }
}
