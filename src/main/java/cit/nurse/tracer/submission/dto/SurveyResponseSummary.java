package cit.nurse.tracer.submission.dto;

public record SurveyResponseSummary(
        long totalResponses,
        long finalizedResponses,
        long draftResponses,
        long employedResponses,
        long pnlePassedResponses
) {
}