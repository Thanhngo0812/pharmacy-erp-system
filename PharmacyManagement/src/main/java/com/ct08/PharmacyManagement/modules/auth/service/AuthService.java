package com.ct08.PharmacyManagement.modules.auth.service;

import com.ct08.PharmacyManagement.modules.auth.dto.LoginRequest;
import com.ct08.PharmacyManagement.modules.auth.dto.LoginResponse;
import com.ct08.PharmacyManagement.modules.auth.entity.Users;
import com.ct08.PharmacyManagement.modules.auth.repository.UsersRepository;
import com.ct08.PharmacyManagement.common.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.ct08.PharmacyManagement.modules.auth.dto.ForgotPasswordRequest;
import com.ct08.PharmacyManagement.modules.auth.dto.ResetPasswordRequest;
import com.ct08.PharmacyManagement.common.event.OtpEmailEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

        @Autowired
        private AuthenticationManager authenticationManager;

        @Autowired
        private UsersRepository usersRepository;

        @Autowired
        private JwtTokenProvider jwtTokenProvider;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Autowired
        private KafkaTemplate<String, Object> kafkaTemplate;

        @Value("${app.jwt.expiration-milliseconds}")
        private long jwtExpirationDate;

        public LoginResponse login(LoginRequest loginRequest) {
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                loginRequest.getUsername(),
                                                loginRequest.getPassword()));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                Users user = usersRepository.findByUsername(loginRequest.getUsername())
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                Integer employeeId = user.getEmployee() != null ? user.getEmployee().getId() : null;
                String fullName = user.getEmployee() != null
                                ? (user.getEmployee().getLastName() + " " + user.getEmployee().getFirstName())
                                : "System Admin";
                String imgUrl = user.getEmployee().getImageUrl();
                String token = jwtTokenProvider.generateToken(authentication, employeeId, fullName);

                List<String> roles = user.getRoles().stream()
                                .map(role -> role.getRoleName())
                                .collect(Collectors.toList());

                LoginResponse response = new LoginResponse();
                response.setAccessToken(token);
                response.setEmployeeId(employeeId);
                response.setFullName(fullName);
                response.setEmail(user.getUsername());
                response.setRoles(roles);
                response.setExpiresIn(jwtExpirationDate);
                response.setImgUrl(imgUrl);

                return response;
        }

        public void forgotPassword(ForgotPasswordRequest request) {
                Users user = usersRepository.findByEmployee_Email(request.getEmail())
                                .orElseThrow(() -> new UsernameNotFoundException("Email not found"));

                String otp = generateOtp();
                user.setResetOtp(otp);
                user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
                usersRepository.save(user);

                String fullName = user.getEmployee() != null
                                ? (user.getEmployee().getLastName() + " " + user.getEmployee().getFirstName())
                                : "User";

                OtpEmailEvent event = new OtpEmailEvent(request.getEmail(), otp, fullName);
                kafkaTemplate.send("user-otp-email", event);
        }

        public void resetPassword(ResetPasswordRequest request) {
                Users user = usersRepository.findByEmployee_Email(request.getEmail())
                                .orElseThrow(() -> new UsernameNotFoundException("Email not found"));

                if (user.getResetOtp() == null || !user.getResetOtp().equals(request.getOtp())) {
                        throw new IllegalArgumentException("Invalid OTP");
                }

                if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(LocalDateTime.now())) {
                        throw new IllegalArgumentException("OTP has expired");
                }

                user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
                user.setResetOtp(null);
                user.setOtpExpiry(null);
                usersRepository.save(user);
        }

        private String generateOtp() {
                SecureRandom random = new SecureRandom();
                int num = random.nextInt(1000000);
                return String.format("%06d", num);
        }
}
