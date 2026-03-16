package cit.nurse.tracer.evaluation.model;

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
@Table(name = "program_evaluation")
public class ProgramEvaluation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private SurveySubmission submission;

    /** Comma-separated selected keys: "clinicalSkills,criticalThinking,teamwork" */
    @Column(name = "relevance_skills")
    private String relevanceSkills;

    /** "Very well prepared", "Well prepared", etc. — stored as the exact frontend label */
    @Column(name = "career_preparation_level")
    private String careerPreparationLevel;

    @Column(name = "nursing_program_aspect", columnDefinition = "TEXT")
    private String nursingProgramAspect;

    @Column(name = "nursing_program_suggestion", columnDefinition = "TEXT")
    private String nursingProgramSuggestion;

    public UUID getId() {
        return id;
    }

    public SurveySubmission getSubmission() {
        return submission;
    }

    public void setSubmission(SurveySubmission submission) {
        this.submission = submission;
    }

    public String getRelevanceSkills() {
        return relevanceSkills;
    }

    public void setRelevanceSkills(String relevanceSkills) {
        this.relevanceSkills = relevanceSkills;
    }

    public String getCareerPreparationLevel() {
        return careerPreparationLevel;
    }

    public void setCareerPreparationLevel(String careerPreparationLevel) {
        this.careerPreparationLevel = careerPreparationLevel;
    }

    public String getNursingProgramAspect() {
        return nursingProgramAspect;
    }

    public void setNursingProgramAspect(String nursingProgramAspect) {
        this.nursingProgramAspect = nursingProgramAspect;
    }

    public String getNursingProgramSuggestion() {
        return nursingProgramSuggestion;
    }

    public void setNursingProgramSuggestion(String nursingProgramSuggestion) {
        this.nursingProgramSuggestion = nursingProgramSuggestion;
    }
}