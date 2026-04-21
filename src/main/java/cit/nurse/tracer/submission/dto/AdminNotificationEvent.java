package cit.nurse.tracer.submission.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminNotificationEvent(
        String type,
        String message,
        UUID submissionId,
        String respondentName,
        String respondentEmail,
        LocalDateTime submittedAt
) {
}