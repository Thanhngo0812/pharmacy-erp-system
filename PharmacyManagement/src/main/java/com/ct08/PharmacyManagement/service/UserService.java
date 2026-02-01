package com.ct08.PharmacyManagement.service;

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
