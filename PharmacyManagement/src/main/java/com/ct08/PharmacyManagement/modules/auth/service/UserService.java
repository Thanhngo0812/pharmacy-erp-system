package com.ct08.PharmacyManagement.modules.auth.service;

import com.ct08.PharmacyManagement.modules.auth.dto.UserProfileResponse;
import com.ct08.PharmacyManagement.modules.hr.entity.Employees;
import com.ct08.PharmacyManagement.modules.auth.entity.Roles;
import com.ct08.PharmacyManagement.modules.auth.entity.Users;
import com.ct08.PharmacyManagement.common.exception.ResourceNotFoundException;
import com.ct08.PharmacyManagement.modules.auth.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UsersRepository usersRepository;

    public void lockAccount(Integer userId) {
        // 1. Get current authenticated user's roles
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        Set<String> currentRoles = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toSet());
        // 2. Fetch the target user
        Optional<Users> targetUserOpt = usersRepository.findById(userId);
        if (targetUserOpt.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }
        Users targetUser = targetUserOpt.get();

        // 3. Check permissions
        boolean isManager = currentRoles.contains("ROLE_MANAGER") || currentRoles.contains("ROLE_ADMIN");
        boolean isHR = currentRoles.contains("ROLE_HM");

        if (isManager) {
            // Manager can lock anyone
            targetUser.setIsActive(false);
            usersRepository.save(targetUser);
        } else if (isHR) {
            // HR cannot lock MANAGER or WM
            boolean targetIsManagerOrWM = targetUser.getRoles().stream()
                    .anyMatch(
                            role -> role.getRoleName().equals("ROLE_MANAGER") || role.getRoleName().equals("ROLE_WM"));

            if (targetIsManagerOrWM) {
                throw new AccessDeniedException("HR cannot lock Manager or Warehouse Manager accounts");
            }

            targetUser.setIsActive(false);
            usersRepository.save(targetUser);
        } else {
            // Other roles cannot lock
            throw new AccessDeniedException("You do not have permission to lock accounts");
        }
    }

    public void unlockAccount(Integer userId) {
        // 1. Get current authenticated user's roles
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        Set<String> currentRoles = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toSet());
        // 2. Fetch the target user
        Optional<Users> targetUserOpt = usersRepository.findById(userId);
        if (targetUserOpt.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }
        Users targetUser = targetUserOpt.get();

        // 3. Check permissions
        boolean isManager = currentRoles.contains("ROLE_MANAGER") || currentRoles.contains("ROLE_ADMIN");
        boolean isHR = currentRoles.contains("ROLE_HM");

        if (isManager) {
            // Manager can lock anyone
            targetUser.setIsActive(true);
            usersRepository.save(targetUser);
        } else if (isHR) {
            // HR cannot lock MANAGER or WM
            boolean targetIsManagerOrWM = targetUser.getRoles().stream()
                    .anyMatch(
                            role -> role.getRoleName().equals("ROLE_MANAGER") || role.getRoleName().equals("ROLE_WM"));

            if (targetIsManagerOrWM) {
                throw new AccessDeniedException("HR cannot unlock Manager or Warehouse Manager accounts");
            }

            targetUser.setIsActive(true);
            usersRepository.save(targetUser);
        } else {
            // Other roles cannot lock
            throw new AccessDeniedException("You do not have permission to unlock accounts");
        }
    }

    public UserProfileResponse getMyProfile(String username) {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Employees employee = user.getEmployee();
        String fullName = employee.getLastName() + " " + employee.getFirstName();
        if (employee == null) {
            throw new ResourceNotFoundException("User is not linked to an employee profile");
        }

        return new UserProfileResponse(
                fullName,
                employee.getEmail(),
                employee.getPhone(),
                employee.getImageUrl(),
                employee.getCurrentPosition() != null ? employee.getCurrentPosition().getPositionName() : null,
                employee.getCurrentSalary(),
                employee.getHireDate());
    }
}
