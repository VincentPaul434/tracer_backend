package cit.nurse.tracer.submission.repository;

import cit.nurse.tracer.submission.model.SurveySubmission;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SurveySubmissionRepository extends JpaRepository<SurveySubmission, UUID> {

    Optional<SurveySubmission> findByEmail(String email);

        @Query(
                        value = """
                                        SELECT s
                                        FROM SurveySubmission s
                                        LEFT JOIN cit.nurse.tracer.personalinfo.model.PersonalInfo p ON p.submission = s
                                        LEFT JOIN cit.nurse.tracer.employment.model.EmploymentData e ON e.submission = s
                                        LEFT JOIN cit.nurse.tracer.licensure.model.LicensureExamination l ON l.submission = s
                                        WHERE (:query IS NULL
                                                OR LOWER(s.email) LIKE LOWER(CONCAT('%', :query, '%'))
                                                OR LOWER(COALESCE(p.fullName, '')) LIKE LOWER(CONCAT('%', :query, '%')))
                                            AND (:status IS NULL OR s.status = :status)
                                            AND (:employmentStatus IS NULL OR LOWER(COALESCE(e.employmentStatus, '')) = LOWER(:employmentStatus))
                                            AND (:licensureStatus IS NULL OR LOWER(COALESCE(l.licensureStatus, '')) = LOWER(:licensureStatus))
                                            AND (:submittedFrom IS NULL OR s.submittedAt >= :submittedFrom)
                                            AND (:submittedToExclusive IS NULL OR s.submittedAt < :submittedToExclusive)
                                        """,
                        countQuery = """
                                        SELECT COUNT(s)
                                        FROM SurveySubmission s
                                        LEFT JOIN cit.nurse.tracer.personalinfo.model.PersonalInfo p ON p.submission = s
                                        LEFT JOIN cit.nurse.tracer.employment.model.EmploymentData e ON e.submission = s
                                        LEFT JOIN cit.nurse.tracer.licensure.model.LicensureExamination l ON l.submission = s
                                        WHERE (:query IS NULL
                                                OR LOWER(s.email) LIKE LOWER(CONCAT('%', :query, '%'))
                                                OR LOWER(COALESCE(p.fullName, '')) LIKE LOWER(CONCAT('%', :query, '%')))
                                            AND (:status IS NULL OR s.status = :status)
                                            AND (:employmentStatus IS NULL OR LOWER(COALESCE(e.employmentStatus, '')) = LOWER(:employmentStatus))
                                            AND (:licensureStatus IS NULL OR LOWER(COALESCE(l.licensureStatus, '')) = LOWER(:licensureStatus))
                                            AND (:submittedFrom IS NULL OR s.submittedAt >= :submittedFrom)
                                            AND (:submittedToExclusive IS NULL OR s.submittedAt < :submittedToExclusive)
                                        """
        )
        Page<SurveySubmission> findAllAdminFiltered(
                        @Param("query") String query,
                        @Param("status") SurveySubmission.Status status,
                        @Param("employmentStatus") String employmentStatus,
                        @Param("licensureStatus") String licensureStatus,
                        @Param("submittedFrom") LocalDateTime submittedFrom,
                        @Param("submittedToExclusive") LocalDateTime submittedToExclusive,
                        Pageable pageable
        );
}
