package cit.nurse.tracer.employment.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record UnemploymentInformationRequest(
    /** { "healthReasons": true, "familyResponsibility": false, ... } — at least one must be true */
    @NotNull(message = "Unemployment reasons selection is required")
    Map<String, Boolean> unemploymentReasons,

    String unemploymentReasonOtherText
) {}