package cit.nurse.tracer.communication.model;

import cit.nurse.tracer.core.util.BaseEntity;
import cit.nurse.tracer.submission.model.SurveySubmission;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "communication_preference")
public class CommunicationPreference extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private SurveySubmission submission;

    /** Comma-separated selected keys: "email,messenger,facebookPageGroup" */
    @Column(name = "invitation_channels")
    private String invitationChannels;

    @Column(name = "invitation_channel_other_text")
    private String invitationChannelOtherText;

    @Column(name = "update_frequency")
    private String updateFrequency;

    /** "Yes", "No", "Maybe" — ternary, stored as String not Boolean */
    @Column(name = "alumni_group_willingness")
    private String alumniGroupWillingness;

    @Column(name = "alumni_platform")
    private String alumniPlatform;

    public UUID getId() {
        return id;
    }

    public SurveySubmission getSubmission() {
        return submission;
    }

    public void setSubmission(SurveySubmission submission) {
        this.submission = submission;
    }

    public String getInvitationChannels() {
        return invitationChannels;
    }

    public void setInvitationChannels(String invitationChannels) {
        this.invitationChannels = invitationChannels;
    }

    public String getInvitationChannelOtherText() {
        return invitationChannelOtherText;
    }

    public void setInvitationChannelOtherText(String invitationChannelOtherText) {
        this.invitationChannelOtherText = invitationChannelOtherText;
    }

    public String getUpdateFrequency() {
        return updateFrequency;
    }

    public void setUpdateFrequency(String updateFrequency) {
        this.updateFrequency = updateFrequency;
    }

    public String getAlumniGroupWillingness() {
        return alumniGroupWillingness;
    }

    public void setAlumniGroupWillingness(String alumniGroupWillingness) {
        this.alumniGroupWillingness = alumniGroupWillingness;
    }

    public String getAlumniPlatform() {
        return alumniPlatform;
    }

    public void setAlumniPlatform(String alumniPlatform) {
        this.alumniPlatform = alumniPlatform;
    }
}