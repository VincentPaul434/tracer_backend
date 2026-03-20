package cit.nurse.tracer.submission.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record SurveyResponseDetail(
    UUID submissionId,
    String email,
    boolean hasAcceptedPrivacy,
    String status,
    LocalDateTime submittedAt,
    Instant createdAt,
    Instant updatedAt,
    PersonalInfoSection personalInfo,
    EducationalBackgroundSection educationalBackground,
    LicensureExaminationSection licensureExamination,
    EmploymentSection employment,
    ProgramEvaluationSection programEvaluation,
    CommunicationPreferenceSection communicationPreference
) {

    public record PersonalInfoSection(
        String fullName,
        String gender,
        String genderOther,
        String civilStatus,
        String civilStatusOther,
        LocalDate birthday,
        String residence,
        String contactInformation
    ) {}

    public record EducationalBackgroundSection(
        String degreeProgramCompleted,
        String yearGraduated,
        String yearGraduatedOther,
        String academicHonors,
        String academicHonorsOtherText,
        Boolean pursuedFurtherStudies,
        String furtherDegreeProgram,
        String furtherStudiesReason,
        String furtherStudiesReasonOtherText
    ) {}

    public record LicensureExaminationSection(
        Boolean hasTakenPnle,
        String licensureStatus,
        String pnleYearPassed,
        String pnleYearPassedOther,
        String examTakeCount
    ) {}

    public record EmploymentSection(
        String employmentStatus,
        String jobRelatedToDegree,
        String employmentSector,
        String employmentSectorOther,
        String positionDesignation,
        String positionDesignationOther,
        String firstJobDuration,
        String firstJobSources,
        String firstJobSourceOtherText,
        String estimatedMonthlySalary,
        String unemploymentReasons,
        String unemploymentReasonOtherText
    ) {}

    public record ProgramEvaluationSection(
        String relevanceSkills,
        String careerPreparationLevel,
        String nursingProgramAspect,
        String nursingProgramSuggestion
    ) {}

    public record CommunicationPreferenceSection(
        String invitationChannels,
        String invitationChannelOtherText,
        String updateFrequency,
        String alumniGroupWillingness,
        String alumniPlatform
    ) {}
}
