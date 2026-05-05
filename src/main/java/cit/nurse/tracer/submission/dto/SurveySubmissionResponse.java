package cit.nurse.tracer.submission.dto;

import java.time.Instant;
import java.util.UUID;

public record SurveySubmissionResponse(
    UUID submissionId,
    String message,
    String editToken,
    String editUrl,
    Instant editUrlExpiresAt
) {}