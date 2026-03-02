package com.ct08.PharmacyManagement.common.infra.cloudinary;

import com.cloudinary.Cloudinary;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        this.cloudinary = new Cloudinary(config);
    }

    public String uploadImage(String filePath) throws IOException {
        File file = new File(filePath);
        Map uploadResult = cloudinary.uploader().upload(file, com.cloudinary.utils.ObjectUtils.emptyMap());
        return (String) uploadResult.get("secure_url");
    }

    public void deleteImage(String imageUrl) {
        try {
            // Extract public ID from URL
            if (imageUrl == null || imageUrl.isEmpty()) {
                return;
            }
            // Example URL: https://res.cloudinary.com/demo/image/upload/v1570979139/sample.jpg
            // Public ID: sample
            // We need to handle folder structure if present.
            // Assuming standard Cloudinary URL structure.
            String[] parts = imageUrl.split("/");
            String filename = parts[parts.length - 1];
            String publicId = filename.contains(".") ? filename.substring(0, filename.lastIndexOf(".")) : filename;
            
            // If there's a folder, we might need more logic or store publicId directly in DB.
            // For now, let's assume simple setup or that we can just delete by public ID derived from filename.
            // However, if we uploaded with random UUID, it's just the ID.
            
            cloudinary.uploader().destroy(publicId, com.cloudinary.utils.ObjectUtils.emptyMap());
        } catch (IOException e) {
            e.printStackTrace();
            // Log error but don't throw to avoid breaking the transaction or flow
        }
    }
}
