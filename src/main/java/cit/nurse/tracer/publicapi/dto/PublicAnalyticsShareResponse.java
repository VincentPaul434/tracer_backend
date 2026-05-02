package cit.nurse.tracer.publicapi.dto;

import java.time.Instant;

public record PublicAnalyticsShareResponse(
        String token,
        String url,
        Instant expiresAt
) {
}
