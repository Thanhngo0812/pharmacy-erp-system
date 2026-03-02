package com.ct08.PharmacyManagement.modules.mail.worker;

import com.ct08.PharmacyManagement.common.event.PasswordEmailEvent;
import com.ct08.PharmacyManagement.modules.mail.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MailWorker {

    @Autowired
    private MailService mailService;

    @Autowired
    private com.ct08.PharmacyManagement.modules.auth.repository.UsersRepository usersRepository;

    @KafkaListener(topics = "user-password-email", groupId = "mail-group")
    public void handlePasswordEmailEvent(PasswordEmailEvent event) {
        try {
            mailService.sendPasswordEmail(event.getEmail(), event.getFullName(), event.getNewPassword());
            if (event.getUserId() != null) {
                usersRepository.findById(event.getUserId()).ifPresent(u -> {
                    u.setMailStatus("sended");
                    usersRepository.save(u);
                });
            }
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            if (event.getUserId() != null) {
                usersRepository.findById(event.getUserId()).ifPresent(u -> {
                    u.setMailStatus("failed");
                    usersRepository.save(u);
                });
            }
        }
    }

    @KafkaListener(topics = "user-otp-email", groupId = "mail-group")
    public void handleOtpEmailEvent(com.ct08.PharmacyManagement.common.event.OtpEmailEvent event) {
        try {
            mailService.sendOtpEmail(event.getEmail(), event.getFullName(), event.getOtp());
        } catch (Exception e) {
            System.err.println("Error sending OTP email: " + e.getMessage());
        }
    }
}
