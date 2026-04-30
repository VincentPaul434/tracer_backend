package cit.nurse.tracer.submission.dto;

import java.util.Map;

public record SurveyAnalyticsResponse(
        long totalResponses,
        long finalizedResponses,
        long draftResponses,
        Map<String, Long> gender,
        Map<String, Long> civilStatus,
        Map<String, Long> degreeProgramCompleted,
        Map<String, Long> yearGraduated,
        Map<String, Long> academicHonors,
        Map<String, Long> pursuedFurtherStudies,
        Map<String, Long> furtherDegreeProgram,
        Map<String, Long> furtherStudiesReason,
        Map<String, Long> hasTakenPnle,
        Map<String, Long> licensureStatus,
        Map<String, Long> pnleYearPassed,
        Map<String, Long> examTakeCount,
        Map<String, Long> employmentStatus,
        Map<String, Long> jobRelatedToDegree,
        Map<String, Long> employmentSector,
        Map<String, Long> positionDesignation,
        Map<String, Long> firstJobDuration,
        Map<String, Long> firstJobSources,
        Map<String, Long> estimatedMonthlySalary,
        Map<String, Long> unemploymentReasons,
        Map<String, Long> relevanceSkills,
        Map<String, Long> careerPreparationLevel,
        Map<String, Long> nursingProgramAspect,
        Map<String, Long> invitationChannels,
        Map<String, Long> updateFrequency,
        Map<String, Long> alumniGroupWillingness,
        Map<String, Long> alumniPlatform,
        Map<String, Long> textResponsesFilled
) {
}
