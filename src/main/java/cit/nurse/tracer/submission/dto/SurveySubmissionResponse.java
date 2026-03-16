package cit.nurse.tracer.submission.dto;

import java.util.UUID;

public record SurveySubmissionResponse(
    UUID submissionId,
    String message
) {}