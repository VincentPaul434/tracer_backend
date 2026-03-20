package cit.nurse.tracer.education.model;

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
@Table(name = "educational_background")
public class EducationalBackground extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private SurveySubmission submission;

    @Column(name = "degree_program_completed")
    private String degreeProgramCompleted;

    /** Stored as String: frontend sends "2020", "2021", or "Other" */
    @Column(name = "year_graduated")
    private String yearGraduated;

    @Column(name = "year_graduated_other")
    private String yearGraduatedOther;

    /** Comma-separated selected keys, e.g. "cumLaude,magnaCumLaude" */
    @Column(name = "academic_honors")
    private String academicHonors;

    @Column(name = "academic_honors_other_text")
    private String academicHonorsOtherText;

    @Column(name = "pursued_further_studies")
    private Boolean pursuedFurtherStudies;

    @Column(name = "further_degree_program")
    private String furtherDegreeProgram;

    @Column(name = "further_studies_reason")
    private String furtherStudiesReason;

    @Column(name = "further_studies_reason_other_text")
    private String furtherStudiesReasonOtherText;

    public UUID getId() {
        return id;
    }

    public SurveySubmission getSubmission() {
        return submission;
    }

    public void setSubmission(SurveySubmission submission) {
        this.submission = submission;
    }

    public String getDegreeProgramCompleted() {
        return degreeProgramCompleted;
    }

    public void setDegreeProgramCompleted(String degreeProgramCompleted) {
        this.degreeProgramCompleted = degreeProgramCompleted;
    }

    public String getYearGraduated() {
        return yearGraduated;
    }

    public void setYearGraduated(String yearGraduated) {
        this.yearGraduated = yearGraduated;
    }

    public String getYearGraduatedOther() {
        return yearGraduatedOther;
    }

    public void setYearGraduatedOther(String yearGraduatedOther) {
        this.yearGraduatedOther = yearGraduatedOther;
    }

    public String getAcademicHonors() {
        return academicHonors;
    }

    public void setAcademicHonors(String academicHonors) {
        this.academicHonors = academicHonors;
    }

    public String getAcademicHonorsOtherText() {
        return academicHonorsOtherText;
    }

    public void setAcademicHonorsOtherText(String academicHonorsOtherText) {
        this.academicHonorsOtherText = academicHonorsOtherText;
    }

    public Boolean getPursuedFurtherStudies() {
        return pursuedFurtherStudies;
    }

    public void setPursuedFurtherStudies(Boolean pursuedFurtherStudies) {
        this.pursuedFurtherStudies = pursuedFurtherStudies;
    }

    public String getFurtherDegreeProgram() {
        return furtherDegreeProgram;
    }

    public void setFurtherDegreeProgram(String furtherDegreeProgram) {
        this.furtherDegreeProgram = furtherDegreeProgram;
    }

    public String getFurtherStudiesReason() {
        return furtherStudiesReason;
    }

    public void setFurtherStudiesReason(String furtherStudiesReason) {
        this.furtherStudiesReason = furtherStudiesReason;
    }

    public String getFurtherStudiesReasonOtherText() {
        return furtherStudiesReasonOtherText;
    }

    public void setFurtherStudiesReasonOtherText(String furtherStudiesReasonOtherText) {
        this.furtherStudiesReasonOtherText = furtherStudiesReasonOtherText;
    }
}