package cit.nurse.tracer.submission.repository;

import cit.nurse.tracer.submission.model.AdminNotification;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminNotificationRepository extends JpaRepository<AdminNotification, UUID> {

    List<AdminNotification> findTop50ByOrderBySubmittedAtDesc();

    @Query(
            """
            SELECT n
            FROM AdminNotification n
            WHERE NOT EXISTS (
                SELECT 1
                FROM AdminNotificationRead r
                WHERE r.notification = n
                  AND r.adminUsername = :adminUsername
            )
            ORDER BY n.submittedAt DESC
            """
    )
    List<AdminNotification> findUnreadByAdminUsername(@Param("adminUsername") String adminUsername);
}