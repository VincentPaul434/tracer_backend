package cit.nurse.tracer.licensure.model;

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
@Table(name = "licensure_examination")
public class LicensureExamination extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private SurveySubmission submission;

    @Column(name = "has_taken_pnle")
    private Boolean hasTakenPnle;

    /** "Passed", "Failed", "Waiting for results" */
    @Column(name = "licensure_status")
    private String licensureStatus;

    @Column(name = "pnle_year_passed")
    private String pnleYearPassed;

    @Column(name = "pnle_year_passed_other")
    private String pnleYearPassedOther;

    /** "1", "2", "3 or more" — stored as String because "3 or more" is not numeric */
    @Column(name = "exam_take_count")
    private String examTakeCount;

    public UUID getId() {
        return id;
    }

    public SurveySubmission getSubmission() {
        return submission;
    }

    public void setSubmission(SurveySubmission submission) {
        this.submission = submission;
    }

    public Boolean getHasTakenPnle() {
        return hasTakenPnle;
    }

    public void setHasTakenPnle(Boolean hasTakenPnle) {
        this.hasTakenPnle = hasTakenPnle;
    }

    public String getLicensureStatus() {
        return licensureStatus;
    }

    public void setLicensureStatus(String licensureStatus) {
        this.licensureStatus = licensureStatus;
    }

    public String getPnleYearPassed() {
        return pnleYearPassed;
    }

    public void setPnleYearPassed(String pnleYearPassed) {
        this.pnleYearPassed = pnleYearPassed;
    }

    public String getPnleYearPassedOther() {
        return pnleYearPassedOther;
    }

    public void setPnleYearPassedOther(String pnleYearPassedOther) {
        this.pnleYearPassedOther = pnleYearPassedOther;
    }

    public String getExamTakeCount() {
        return examTakeCount;
    }

    public void setExamTakeCount(String examTakeCount) {
        this.examTakeCount = examTakeCount;
    }
}