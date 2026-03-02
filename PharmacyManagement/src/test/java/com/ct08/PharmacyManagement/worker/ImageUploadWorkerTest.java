package com.ct08.PharmacyManagement.worker;

import com.ct08.PharmacyManagement.common.infra.cloudinary.CloudinaryService;
import com.ct08.PharmacyManagement.modules.hr.entity.Employees;
import com.ct08.PharmacyManagement.modules.hr.repository.EmployeesRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImageUploadWorkerTest {

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private EmployeesRepository employeesRepository;

    @InjectMocks
    private com.ct08.PharmacyManagement.modules.image.worker.ImageWorker imageWorker;

    @Test
    void listen_ValidMessage_ShouldUploadAndUpdate() throws IOException {
        // Arrange
        Integer empId = 1;
        // Create a temporary file to simulate local file
        File tempFile = File.createTempFile("test-image", ".jpg");
        String filePath = tempFile.getAbsolutePath();
        String oldImageUrl = "http://old.image";
        
        com.ct08.PharmacyManagement.common.event.ImageUpdateEvent event = 
            new com.ct08.PharmacyManagement.common.event.ImageUpdateEvent(empId, filePath, oldImageUrl);

        Employees employee = new Employees();
        employee.setId(empId);

        when(employeesRepository.findById(empId)).thenReturn(Optional.of(employee));
        when(cloudinaryService.uploadImage(filePath)).thenReturn("http://cloudinary.com/image.jpg");

        // Act
        imageWorker.listen(event);

        // Assert
        verify(cloudinaryService).uploadImage(filePath);
        verify(employeesRepository).save(employee);
        
        // Check if file is deleted (optional verify, hard to mock File class directly in this setup without PowerMock)
        // But we can check flow completes.
    }
}
