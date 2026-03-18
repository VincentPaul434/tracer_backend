package cit.nurse.tracer.licensure.repository;

import cit.nurse.tracer.licensure.model.LicensureExamination;
import cit.nurse.tracer.submission.model.SurveySubmission;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LicensureExaminationRepository extends JpaRepository<LicensureExamination, UUID> {
    Optional<LicensureExamination> findBySubmission(SurveySubmission submission);

    List<LicensureExamination> findBySubmissionIn(Collection<SurveySubmission> submissions);
}