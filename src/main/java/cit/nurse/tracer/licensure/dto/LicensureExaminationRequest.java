package cit.nurse.tracer.licensure.dto;

import jakarta.validation.constraints.NotBlank;

public record LicensureExaminationRequest(
    @NotBlank(message = "PNLE answer is required")
    String hasTakenPnle,

    /** Required when hasTakenPnle == "Yes" — validated in service layer */
    String licensureStatus,

    String pnleYearPassed,
    String pnleYearPassedOther,

    /** "1", "2", "3 or more" — not castable to int */
    String examTakeCount
) {}