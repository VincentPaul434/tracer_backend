package cit.nurse.tracer.submission.model;

import cit.nurse.tracer.core.util.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "admin_notifications",
        indexes = {
                @Index(name = "idx_admin_notifications_submitted_at", columnList = "submitted_at"),
                @Index(name = "idx_admin_notifications_submission_id", columnList = "submission_id")
        }
)
public class AdminNotification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "type", nullable = false, length = 64)
    private String type;

    @Column(name = "message", nullable = false, length = 512)
    private String message;

    @Column(name = "submission_id")
    private UUID submissionId;

    @Column(name = "respondent_name", nullable = false, length = 255)
    private String respondentName;

    @Column(name = "respondent_email", nullable = false, length = 255)
    private String respondentEmail;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    public UUID getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UUID getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(UUID submissionId) {
        this.submissionId = submissionId;
    }

    public String getRespondentName() {
        return respondentName;
    }

    public void setRespondentName(String respondentName) {
        this.respondentName = respondentName;
    }

    public String getRespondentEmail() {
        return respondentEmail;
    }

    public void setRespondentEmail(String respondentEmail) {
        this.respondentEmail = respondentEmail;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}