package cit.nurse.tracer.employment.model;

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
@Table(name = "employment_data")
public class EmploymentData extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private SurveySubmission submission;

    /** "Employed", "Self employed", "Unemployed", "Currently studying" */
    @Column(name = "employment_status")
    private String employmentStatus;

    // ── Employment-specific fields (when Employed/Self employed) ────

    @Column(name = "job_related_to_degree")
    private String jobRelatedToDegree;

    @Column(name = "employment_sector")
    private String employmentSector;

    @Column(name = "employment_sector_other")
    private String employmentSectorOther;

    @Column(name = "position_designation")
    private String positionDesignation;

    @Column(name = "position_designation_other")
    private String positionDesignationOther;

    @Column(name = "first_job_duration")
    private String firstJobDuration;

    /** Comma-separated selected keys: "jobFairs,onlineJobPortal,other" */
    @Column(name = "first_job_sources")
    private String firstJobSources;

    @Column(name = "first_job_source_other_text")
    private String firstJobSourceOtherText;

    /** Contains ₱ and en-dash — stored as-is in UTF-8 VARCHAR */
    @Column(name = "estimated_monthly_salary")
    private String estimatedMonthlySalary;

    // ── Unemployment-specific fields (when Unemployed/Currently studying) ──

    /** Comma-separated selected keys */
    @Column(name = "unemployment_reasons")
    private String unemploymentReasons;

    @Column(name = "unemployment_reason_other_text")
    private String unemploymentReasonOtherText;

    public UUID getId() {
        return id;
    }

    public SurveySubmission getSubmission() {
        return submission;
    }

    public void setSubmission(SurveySubmission submission) {
        this.submission = submission;
    }

    public String getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(String employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public String getJobRelatedToDegree() {
        return jobRelatedToDegree;
    }

    public void setJobRelatedToDegree(String jobRelatedToDegree) {
        this.jobRelatedToDegree = jobRelatedToDegree;
    }

    public String getEmploymentSector() {
        return employmentSector;
    }

    public void setEmploymentSector(String employmentSector) {
        this.employmentSector = employmentSector;
    }

    public String getEmploymentSectorOther() {
        return employmentSectorOther;
    }

    public void setEmploymentSectorOther(String employmentSectorOther) {
        this.employmentSectorOther = employmentSectorOther;
    }

    public String getPositionDesignation() {
        return positionDesignation;
    }

    public void setPositionDesignation(String positionDesignation) {
        this.positionDesignation = positionDesignation;
    }

    public String getPositionDesignationOther() {
        return positionDesignationOther;
    }

    public void setPositionDesignationOther(String positionDesignationOther) {
        this.positionDesignationOther = positionDesignationOther;
    }

    public String getFirstJobDuration() {
        return firstJobDuration;
    }

    public void setFirstJobDuration(String firstJobDuration) {
        this.firstJobDuration = firstJobDuration;
    }

    public String getFirstJobSources() {
        return firstJobSources;
    }

    public void setFirstJobSources(String firstJobSources) {
        this.firstJobSources = firstJobSources;
    }

    public String getFirstJobSourceOtherText() {
        return firstJobSourceOtherText;
    }

    public void setFirstJobSourceOtherText(String firstJobSourceOtherText) {
        this.firstJobSourceOtherText = firstJobSourceOtherText;
    }

    public String getEstimatedMonthlySalary() {
        return estimatedMonthlySalary;
    }

    public void setEstimatedMonthlySalary(String estimatedMonthlySalary) {
        this.estimatedMonthlySalary = estimatedMonthlySalary;
    }

    public String getUnemploymentReasons() {
        return unemploymentReasons;
    }

    public void setUnemploymentReasons(String unemploymentReasons) {
        this.unemploymentReasons = unemploymentReasons;
    }

    public String getUnemploymentReasonOtherText() {
        return unemploymentReasonOtherText;
    }

    public void setUnemploymentReasonOtherText(String unemploymentReasonOtherText) {
        this.unemploymentReasonOtherText = unemploymentReasonOtherText;
    }
}