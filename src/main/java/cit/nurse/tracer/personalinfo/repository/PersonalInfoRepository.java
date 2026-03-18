package cit.nurse.tracer.personalinfo.repository;

import cit.nurse.tracer.personalinfo.model.PersonalInfo;
import cit.nurse.tracer.submission.model.SurveySubmission;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalInfoRepository extends JpaRepository<PersonalInfo, UUID> {
    Optional<PersonalInfo> findBySubmission(SurveySubmission submission);

    List<PersonalInfo> findBySubmissionIn(Collection<SurveySubmission> submissions);
}