package cit.nurse.tracer.education.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * academicHonors arrives from the frontend as:
 *   { "cumLaude": true, "magnaCumLaude": false, "none": false, "other": false }
 *
 * The service layer extracts the truthy keys into a comma-separated String
 * before persisting (e.g., "cumLaude,magnaCumLaude").
 */
public record EducationalBackgroundRequest(
    @NotBlank(message = "Degree program is required")
    String degreeProgramCompleted,

    @NotBlank(message = "Year graduated is required")
    String yearGraduated,

    String yearGraduatedOther,

    @NotNull(message = "Academic honors selection is required")
    Map<String, Boolean> academicHonors,

    String academicHonorsOtherText,

    @NotBlank(message = "Further studies answer is required")
    String pursuedFurtherStudies,

    String furtherDegreeProgram
) {}