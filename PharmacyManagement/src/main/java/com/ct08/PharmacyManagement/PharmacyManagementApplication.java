package com.ct08.PharmacyManagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class PharmacyManagementApplication {

	public static void main(String[] args) {

		SpringApplication.run(PharmacyManagementApplication.class, args);
	}

}
