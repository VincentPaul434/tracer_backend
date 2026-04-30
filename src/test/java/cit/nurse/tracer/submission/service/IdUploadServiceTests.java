package cit.nurse.tracer.submission.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

import cit.nurse.tracer.submission.dto.IdUploadResponse;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class IdUploadServiceTests {

    @Test
    void uploadIdReturnsUploadedFileDetails() throws Exception {
        Cloudinary cloudinary = org.mockito.Mockito.mock(Cloudinary.class);
        Uploader uploader = org.mockito.Mockito.mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://res.cloudinary.com/demo/image/upload/sample.jpg");
        uploadResult.put("public_id", "tracer/id-uploads/sample");
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        IdUploadService service = new IdUploadService(cloudinary, "tracer/id-uploads");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "id-card.jpg",
                "image/jpeg",
                new byte[] {1, 2, 3}
        );

        IdUploadResponse response = service.uploadId(file);

        assertThat(response.idImageUrl()).isEqualTo("https://res.cloudinary.com/demo/image/upload/sample.jpg");
        assertThat(response.publicId()).isEqualTo("tracer/id-uploads/sample");
        assertThat(response.originalFilename()).isEqualTo("id-card.jpg");
        assertThat(response.bytes()).isEqualTo(3L);
    }

    @Test
    void uploadIdRejectsNonImageFiles() {
        Cloudinary cloudinary = org.mockito.Mockito.mock(Cloudinary.class);
        IdUploadService service = new IdUploadService(cloudinary, "tracer/id-uploads");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                new byte[] {1, 2, 3}
        );

        assertThatThrownBy(() -> service.uploadId(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only image files are allowed for ID upload.");
    }
}