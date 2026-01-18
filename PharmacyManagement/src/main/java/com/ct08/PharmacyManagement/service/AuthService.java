package com.ct08.PharmacyManagement.service;

import com.ct08.PharmacyManagement.dto.LoginRequest;
import com.ct08.PharmacyManagement.dto.LoginResponse;
import com.ct08.PharmacyManagement.entity.Users;
import com.ct08.PharmacyManagement.repository.UsersRepository;
import com.ct08.PharmacyManagement.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
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

    @Value("${app.jwt.expiration-milliseconds}")
    private long jwtExpirationDate;

    public LoginResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        Users user = usersRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Integer employeeId = user.getEmployee() != null ? user.getEmployee().getId() : null;
        String fullName = user.getEmployee() != null ? user.getEmployee().getFullName() : "System Admin";

        String token = jwtTokenProvider.generateToken(authentication, employeeId, fullName);

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getRoleName())
                .collect(Collectors.toList());

        LoginResponse response = new LoginResponse();
        response.setAccessToken(token);
        response.setEmployeeId(employeeId);
        response.setFullName(fullName);
        response.setRoles(roles);
        response.setExpiresIn(jwtExpirationDate);

        return response;
    }
}
