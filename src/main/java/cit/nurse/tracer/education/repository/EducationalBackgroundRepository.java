package cit.nurse.tracer.education.repository;

import cit.nurse.tracer.education.model.EducationalBackground;
import cit.nurse.tracer.submission.model.SurveySubmission;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EducationalBackgroundRepository extends JpaRepository<EducationalBackground, UUID> {
    Optional<EducationalBackground> findBySubmission(SurveySubmission submission);
}