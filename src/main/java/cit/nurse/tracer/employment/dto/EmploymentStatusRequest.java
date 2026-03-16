package cit.nurse.tracer.employment.dto;

import jakarta.validation.constraints.NotBlank;

public record EmploymentStatusRequest(
    @NotBlank(message = "Employment status is required")
    String employmentStatus
) {}