package cit.nurse.tracer.submission.service;

import cit.nurse.tracer.submission.dto.IdUploadResponse;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class IdUploadService {

    private final Cloudinary cloudinary;
    private final String uploadFolder;

    public IdUploadService(Cloudinary cloudinary, @Value("${app.cloudinary.folder:tracer/id-uploads}") String uploadFolder) {
        this.cloudinary = cloudinary;
        this.uploadFolder = uploadFolder;
    }

    public IdUploadResponse uploadId(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("ID file is required.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed for ID upload.");
        }

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", uploadFolder,
                            "resource_type", "auto"
                    )
            );

            String secureUrl = uploadResult.get("secure_url") == null ? null : uploadResult.get("secure_url").toString();
            String publicId = uploadResult.get("public_id") == null ? null : uploadResult.get("public_id").toString();

            if (secureUrl == null || secureUrl.isBlank()) {
                throw new IllegalStateException("Cloudinary did not return an uploaded file URL.");
            }

            return new IdUploadResponse(secureUrl, publicId, file.getOriginalFilename(), file.getSize());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to upload ID image. Please try again.", ex);
        }
    }
}
