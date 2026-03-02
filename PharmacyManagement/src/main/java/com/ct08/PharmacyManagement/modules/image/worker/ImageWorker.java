package com.ct08.PharmacyManagement.modules.image.worker;

import com.ct08.PharmacyManagement.common.infra.cloudinary.CloudinaryService;
import com.ct08.PharmacyManagement.common.event.ImageUpdateEvent;
import com.ct08.PharmacyManagement.modules.hr.entity.Employees;
import com.ct08.PharmacyManagement.modules.hr.repository.EmployeesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class ImageWorker {

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private EmployeesRepository employeesRepository;

    @KafkaListener(topics = "employee-image-upload", groupId = "image-module-group")
    public void listen(ImageUpdateEvent event) {
        try {
            String localFilePath = event.getLocalFilePath();
            Integer employeeId = event.getEmployeeId();
            String oldImageUrl = event.getOldImageUrl();

            if (localFilePath != null && new File(localFilePath).exists()) {
                // Upload to Cloudinary
                String cloudinaryUrl = cloudinaryService.uploadImage(localFilePath);

                // Update Employee record
                Employees employee = employeesRepository.findById(employeeId).orElse(null);
                if (employee != null) {
                    employee.setImageUrl(cloudinaryUrl);
                    employeesRepository.save(employee);
                    
                    // Delete local file after successful upload
                    try {
                        new File(localFilePath).delete();
                    } catch (Exception e) {
                        System.err.println("Failed to delete local file: " + localFilePath);
                    }

                    // Delete old image from Cloudinary
                    if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                         cloudinaryService.deleteImage(oldImageUrl);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle failure
        }
    }
}
