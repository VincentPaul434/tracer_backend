package cit.nurse.tracer.submission.model;

import cit.nurse.tracer.core.util.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;

@Entity
@Table(
        name = "admin_notification_reads",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_admin_notification_reads_notification_admin", columnNames = {"notification_id", "admin_username"})
        },
        indexes = {
                @Index(name = "idx_admin_notification_reads_admin_username", columnList = "admin_username"),
                @Index(name = "idx_admin_notification_reads_notification_id", columnList = "notification_id")
        }
)
public class AdminNotificationRead extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notification_id", nullable = false)
    private AdminNotification notification;

    @Column(name = "admin_username", nullable = false, length = 100)
    private String adminUsername;

    public UUID getId() {
        return id;
    }

    public AdminNotification getNotification() {
        return notification;
    }

    public void setNotification(AdminNotification notification) {
        this.notification = notification;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }
}