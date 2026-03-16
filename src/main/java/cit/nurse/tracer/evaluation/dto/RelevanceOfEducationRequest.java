package cit.nurse.tracer.evaluation.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record RelevanceOfEducationRequest(
    @NotNull(message = "Skills selection is required")
    Map<String, Boolean> relevanceSkills
) {}