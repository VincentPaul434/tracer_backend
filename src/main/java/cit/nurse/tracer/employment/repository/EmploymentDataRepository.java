package cit.nurse.tracer.employment.repository;

import cit.nurse.tracer.employment.model.EmploymentData;
import cit.nurse.tracer.submission.model.SurveySubmission;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmploymentDataRepository extends JpaRepository<EmploymentData, UUID> {
    Optional<EmploymentData> findBySubmission(SurveySubmission submission);

    List<EmploymentData> findBySubmissionIn(Collection<SurveySubmission> submissions);
}