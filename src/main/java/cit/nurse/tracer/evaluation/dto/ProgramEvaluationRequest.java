package cit.nurse.tracer.evaluation.dto;

import jakarta.validation.constraints.NotBlank;

public record ProgramEvaluationRequest(
    @NotBlank(message = "Career preparation level is required")
    String careerPreparationLevel,

    @NotBlank(message = "Nursing program aspect is required")
    String nursingProgramAspect,

    @NotBlank(message = "Nursing program suggestion is required")
    String nursingProgramSuggestion
) {}