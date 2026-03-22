package cit.nurse.tracer.submission.dto;

import java.time.LocalDate;

public record AdminSurveyResponseFilter(
        String query,
        String status,
        String employmentStatus,
        String licensureStatus,
        LocalDate submittedFrom,
        LocalDate submittedTo
) {
}