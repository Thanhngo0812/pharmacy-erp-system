package com.ct08.PharmacyManagement.service;

import com.ct08.PharmacyManagement.entity.Roles;
import com.ct08.PharmacyManagement.entity.Users;
import com.ct08.PharmacyManagement.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.ct08.PharmacyManagement.dto.UserProfileResponse;
import com.ct08.PharmacyManagement.entity.Employees;
import com.ct08.PharmacyManagement.entity.Users;
import com.ct08.PharmacyManagement.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UsersRepository usersRepository;

    public void lockAccount(Integer userId) {
        // 1. Get current authenticated user's roles
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        Set<String> currentRoles = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toSet());

        // 2. Fetch the target user
        Optional<Users> targetUserOpt = usersRepository.findById(userId);
        if (targetUserOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        Users targetUser = targetUserOpt.get();

        // 3. Check permissions
        boolean isManager = currentRoles.contains("ROLE_MANAGER");
        boolean isHR = currentRoles.contains("ROLE_HR");

        if (isManager) {
            // Manager can lock anyone
            targetUser.setIsActive(false);
            usersRepository.save(targetUser);
        } else if (isHR) {
            // HR cannot lock MANAGER or WM
            boolean targetIsManagerOrWM = targetUser.getRoles().stream()
                    .anyMatch(role -> role.getRoleName().equals("ROLE_MANAGER") || role.getRoleName().equals("ROLE_WM"));

            if (targetIsManagerOrWM) {
                throw new RuntimeException("HR cannot lock Manager or Warehouse Manager accounts");
            }

            targetUser.setIsActive(false);
            usersRepository.save(targetUser);
        } else {
            // Other roles cannot lock
            throw new RuntimeException("You do not have permission to lock accounts");
        }
    public UserProfileResponse getMyProfile(String username) {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Employees employee = user.getEmployee();
        if (employee == null) {
            throw new RuntimeException("User is not linked to an employee profile");
        }

        return new UserProfileResponse(
                employee.getFullName(),
                employee.getEmail(),
                employee.getPhone(),
                employee.getImageUrl(),
                employee.getCurrentPosition() != null ? employee.getCurrentPosition().getPositionName() : null,
                employee.getCurrentSalary(),
                employee.getHireDate()
        );
    }
}
