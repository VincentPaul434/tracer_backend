package cit.nurse.tracer.communication.repository;

import cit.nurse.tracer.communication.model.CommunicationPreference;
import cit.nurse.tracer.submission.model.SurveySubmission;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunicationPreferenceRepository extends JpaRepository<CommunicationPreference, UUID> {
    Optional<CommunicationPreference> findBySubmission(SurveySubmission submission);
}