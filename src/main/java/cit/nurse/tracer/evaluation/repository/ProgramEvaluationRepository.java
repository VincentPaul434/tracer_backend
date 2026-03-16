package cit.nurse.tracer.evaluation.repository;

import cit.nurse.tracer.evaluation.model.ProgramEvaluation;
import cit.nurse.tracer.submission.model.SurveySubmission;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramEvaluationRepository extends JpaRepository<ProgramEvaluation, UUID> {
    Optional<ProgramEvaluation> findBySubmission(SurveySubmission submission);
}