package cit.nurse.tracer.submission.model;

import cit.nurse.tracer.core.util.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import java.time.LocalDateTime;
import jakarta.persistence.PrePersist;

@Entity
@Table(name = "survey_submissions")
public class SurveySubmission extends BaseEntity {

    public enum Status {
        DRAFT,
        FINALIZED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "has_accepted_privacy", nullable = false)
    private boolean hasAcceptedPrivacy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.DRAFT;

    @Column(name = "ip_hash", length = 64)
    private String ipHash;

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isHasAcceptedPrivacy() {
        return hasAcceptedPrivacy;
    }

    public void setHasAcceptedPrivacy(boolean hasAcceptedPrivacy) {
        this.hasAcceptedPrivacy = hasAcceptedPrivacy;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getIpHash() {
        return ipHash;
    }

    public void setIpHash(String ipHash) {
        this.ipHash = ipHash;
    }

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    // This automatically sets the time right before saving to the DB
    @PrePersist
    protected void onSubmission() {
        if (this.submittedAt == null) {
            this.submittedAt = LocalDateTime.now();
        }
    }

    // Add the getter and setter
    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}