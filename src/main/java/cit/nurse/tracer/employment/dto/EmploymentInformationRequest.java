package cit.nurse.tracer.employment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record EmploymentInformationRequest(
    @NotBlank(message = "Job relevance answer is required")
    String jobRelatedToDegree,

    @NotBlank(message = "Employment sector is required")
    String employmentSector,

    String employmentSectorOther,

    @NotBlank(message = "Position designation is required")
    String positionDesignation,

    String positionDesignationOther,

    @NotBlank(message = "First job duration is required")
    String firstJobDuration,

    /** { "jobFairs": true, "onlineJobPortal": false, ... } — at least one must be true */
    @NotNull(message = "First job sources selection is required")
    Map<String, Boolean> firstJobSources,

    String firstJobSourceOtherText,

    @NotBlank(message = "Salary range is required")
    String estimatedMonthlySalary
) {}