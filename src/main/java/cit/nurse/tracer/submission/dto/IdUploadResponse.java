package cit.nurse.tracer.submission.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IdUploadResponse(
        String idImageUrl,
        String publicId,
        String originalFilename,
        long bytes
) {
        @JsonProperty("url")
        public String url() {
                return idImageUrl;
        }
}
