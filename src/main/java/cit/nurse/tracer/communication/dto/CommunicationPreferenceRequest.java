package cit.nurse.tracer.communication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record CommunicationPreferenceRequest(
    /** { "email": true, "messenger": false, ... } — at least one must be true */
    @NotNull(message = "Invitation channels selection is required")
    Map<String, Boolean> invitationChannels,

    String invitationChannelOtherText,

    @NotBlank(message = "Update frequency is required")
    String updateFrequency,

    @NotBlank(message = "Alumni group willingness is required")
    String alumniGroupWillingness,

    /** Required when alumniGroupWillingness == "Yes" — validated in service layer */
    String alumniPlatform
) {}