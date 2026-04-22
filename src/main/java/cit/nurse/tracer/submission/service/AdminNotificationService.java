package cit.nurse.tracer.submission.service;

import cit.nurse.tracer.submission.dto.AdminNotificationEvent;
import cit.nurse.tracer.submission.model.AdminNotification;
import cit.nurse.tracer.submission.model.AdminNotificationRead;
import cit.nurse.tracer.submission.repository.AdminNotificationReadRepository;
import cit.nurse.tracer.submission.repository.AdminNotificationRepository;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class AdminNotificationService {

    private static final long SSE_TIMEOUT_MS = 0L;
    private static final int MAX_UNREAD_NOTIFICATIONS = 100;
    private static final String EVENT_NAME_SURVEY_COMPLETED = "survey-completed";

    private final AdminNotificationRepository adminNotificationRepository;
    private final AdminNotificationReadRepository adminNotificationReadRepository;
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public AdminNotificationService(
            AdminNotificationRepository adminNotificationRepository,
            AdminNotificationReadRepository adminNotificationReadRepository
    ) {
        this.adminNotificationRepository = adminNotificationRepository;
        this.adminNotificationReadRepository = adminNotificationReadRepository;
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            emitters.remove(emitter);
        });
        emitter.onError(error -> emitters.remove(emitter));

        try {
            emitter.send(SseEmitter.event().name("connected").data("admin-notification-stream-ready"));
        } catch (IOException ex) {
            emitter.completeWithError(ex);
            emitters.remove(emitter);
        }

        return emitter;
    }

    @Transactional(readOnly = true)
    public List<AdminNotificationEvent> getRecentNotifications() {
        return adminNotificationRepository.findTop50ByOrderBySubmittedAtDesc().stream()
                .map(this::toEvent)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminNotificationEvent> getUnreadNotifications(String adminUsername) {
        String normalizedAdminUsername = normalizeAdminUsername(adminUsername);
        return adminNotificationRepository.findUnreadByAdminUsername(normalizedAdminUsername).stream()
                .limit(MAX_UNREAD_NOTIFICATIONS)
                .map(this::toEvent)
                .toList();
    }

    @Transactional
    public void markAsRead(UUID notificationId, String adminUsername) {
        String normalizedAdminUsername = normalizeAdminUsername(adminUsername);
        if (adminNotificationReadRepository.existsByNotificationIdAndAdminUsername(notificationId, normalizedAdminUsername)) {
            return;
        }

        AdminNotification notification = adminNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new NoSuchElementException("Notification not found."));

        AdminNotificationRead read = new AdminNotificationRead();
        read.setNotification(notification);
        read.setAdminUsername(normalizedAdminUsername);
        adminNotificationReadRepository.save(read);
    }

    @Transactional
    public int markAllAsRead(String adminUsername) {
        String normalizedAdminUsername = normalizeAdminUsername(adminUsername);
        List<AdminNotification> unreadNotifications = adminNotificationRepository.findUnreadByAdminUsername(normalizedAdminUsername);
        if (unreadNotifications.isEmpty()) {
            return 0;
        }

        List<AdminNotificationRead> reads = new ArrayList<>();
        for (AdminNotification unreadNotification : unreadNotifications) {
            AdminNotificationRead read = new AdminNotificationRead();
            read.setNotification(unreadNotification);
            read.setAdminUsername(normalizedAdminUsername);
            reads.add(read);
        }
        adminNotificationReadRepository.saveAll(reads);
        return reads.size();
    }

    @Transactional
    public void publishSurveyCompleted(UUID submissionId, String respondentName, String respondentEmail, LocalDateTime submittedAt) {
        String safeName = respondentName == null || respondentName.isBlank() ? "Unknown respondent" : respondentName.trim();
        String safeEmail = respondentEmail == null ? "" : respondentEmail.trim().toLowerCase();
        LocalDateTime safeSubmittedAt = submittedAt == null ? LocalDateTime.now() : submittedAt;

        AdminNotification notification = new AdminNotification();
        notification.setType(EVENT_NAME_SURVEY_COMPLETED);
        notification.setMessage(safeName + " finished answering the survey.");
        notification.setSubmissionId(submissionId);
        notification.setRespondentName(safeName);
        notification.setRespondentEmail(safeEmail);
        notification.setSubmittedAt(safeSubmittedAt);

        AdminNotification savedNotification = adminNotificationRepository.save(notification);

        AdminNotificationEvent event = toEvent(savedNotification);

        broadcast(event);
    }

    private String normalizeAdminUsername(String adminUsername) {
        if (adminUsername == null || adminUsername.isBlank()) {
            throw new IllegalArgumentException("Admin username is required.");
        }
        return adminUsername.trim().toLowerCase(Locale.ROOT);
    }

    private AdminNotificationEvent toEvent(AdminNotification notification) {
        return new AdminNotificationEvent(
                notification.getType(),
                notification.getMessage(),
                notification.getSubmissionId(),
                notification.getRespondentName(),
                notification.getRespondentEmail(),
                notification.getSubmittedAt()
        );
    }

    private void broadcast(AdminNotificationEvent event) {
        List<SseEmitter> staleEmitters = new ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(
                        SseEmitter.event()
                                .id(event.submissionId() == null ? UUID.randomUUID().toString() : event.submissionId().toString())
                                .name(EVENT_NAME_SURVEY_COMPLETED)
                                .data(event)
                );
            } catch (IOException ex) {
                emitter.completeWithError(ex);
                staleEmitters.add(emitter);
            }
        }

        if (!staleEmitters.isEmpty()) {
            emitters.removeAll(staleEmitters);
        }
    }
}