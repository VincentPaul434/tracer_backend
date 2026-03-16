package cit.nurse.tracer.submission.repository;

import cit.nurse.tracer.submission.model.SurveySubmission;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveySubmissionRepository extends JpaRepository<SurveySubmission, UUID> {

    Optional<SurveySubmission> findByEmail(String email);
}
