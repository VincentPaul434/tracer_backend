package cit.nurse.tracer.submission.repository;

import cit.nurse.tracer.submission.model.AdminNotificationRead;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminNotificationReadRepository extends JpaRepository<AdminNotificationRead, UUID> {

    boolean existsByNotificationIdAndAdminUsername(UUID notificationId, String adminUsername);
}