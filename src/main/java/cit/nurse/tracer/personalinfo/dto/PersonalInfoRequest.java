package cit.nurse.tracer.personalinfo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record PersonalInfoRequest(
    @NotBlank(message = "Full name is required")
    String fullName,

    @NotBlank(message = "Gender is required")
    String gender,

    String genderOther,

    @NotBlank(message = "Civil status is required")
    String civilStatus,

    String civilStatusOther,

    @NotNull(message = "Birthday is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate birthday,

    @NotBlank(message = "Residence is required")
    String residence,

    @NotBlank(message = "Contact information is required")
    String contactInformation
) {}