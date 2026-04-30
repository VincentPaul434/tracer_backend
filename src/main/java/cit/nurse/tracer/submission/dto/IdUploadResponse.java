package cit.nurse.tracer.submission.dto;

public record IdUploadResponse(
        String idImageUrl,
        String publicId,
        String originalFilename,
        long bytes
) {
}
