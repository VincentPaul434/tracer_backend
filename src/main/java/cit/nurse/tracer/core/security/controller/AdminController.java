package cit.nurse.tracer.core.security.controller;

import cit.nurse.tracer.core.security.JwtUtils;
import cit.nurse.tracer.publicapi.dto.PublicAnalyticsShareResponse;
import cit.nurse.tracer.submission.dto.AdminSurveyResponseFilter;
import cit.nurse.tracer.submission.dto.AdminNotificationEvent;
import cit.nurse.tracer.submission.dto.SurveyAnalyticsResponse;
import cit.nurse.tracer.submission.dto.SurveyResponseDetail;
import cit.nurse.tracer.submission.dto.SurveyResponseSummary;
import cit.nurse.tracer.submission.service.AdminNotificationService;
import cit.nurse.tracer.submission.service.SurveyService;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "submittedAt",
            "email",
            "status",
            "createdAt",
            "updatedAt"
    );

    private final SurveyService surveyService;
    private final AdminNotificationService adminNotificationService;
    private final JwtUtils jwtUtils;
    private final String publicAnalyticsBaseUrl;

    public AdminController(
            SurveyService surveyService,
            AdminNotificationService adminNotificationService,
            JwtUtils jwtUtils,
            @Value("${app.render.url:}") String publicAnalyticsBaseUrl
    ) {
        this.surveyService = surveyService;
        this.adminNotificationService = adminNotificationService;
        this.jwtUtils = jwtUtils;
        this.publicAnalyticsBaseUrl = publicAnalyticsBaseUrl;
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(Authentication authentication) {
        return ResponseEntity.ok(
            Map.of(
                "username", authentication.getName(),
                "roles", authentication.getAuthorities().stream()
                    .map(grantedAuthority -> grantedAuthority.getAuthority())
                    .collect(Collectors.toList())
            )
        );
    }

    @GetMapping("/survey-responses")
    public ResponseEntity<Page<SurveyResponseDetail>> getSurveyResponses(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String employmentStatus,
            @RequestParam(required = false) String licensureStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate submittedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate submittedTo,
            @RequestParam(required = false) String yearGraduated,
            @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = "submittedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        AdminSurveyResponseFilter filter = new AdminSurveyResponseFilter(
            query,
            status,
            employmentStatus,
            licensureStatus,
            submittedFrom,
            submittedTo,
            yearGraduated
        );
        return ResponseEntity.ok(surveyService.getSurveyResponses(toSafePageable(pageable), filter));
    }

        @GetMapping("/survey-responses/summary")
        public ResponseEntity<SurveyResponseSummary> getSurveyResponsesSummary(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String employmentStatus,
            @RequestParam(required = false) String licensureStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate submittedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate submittedTo,
            @RequestParam(required = false) String yearGraduated
        ) {
            AdminSurveyResponseFilter filter = new AdminSurveyResponseFilter(
                query,
                status,
                employmentStatus,
                licensureStatus,
                submittedFrom,
                submittedTo,
                yearGraduated
            );
            return ResponseEntity.ok(surveyService.getSurveyResponseSummary(filter));
        }

            @GetMapping("/survey-responses/analytics")
            public ResponseEntity<SurveyAnalyticsResponse> getSurveyResponsesAnalytics(
                @RequestParam(required = false) String query,
                @RequestParam(required = false) String status,
                @RequestParam(required = false) String employmentStatus,
                @RequestParam(required = false) String licensureStatus,
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate submittedFrom,
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate submittedTo,
                @RequestParam(required = false) String yearGraduated
            ) {
            AdminSurveyResponseFilter filter = new AdminSurveyResponseFilter(
                query,
                status,
                employmentStatus,
                licensureStatus,
                submittedFrom,
                submittedTo,
                yearGraduated
            );
            return ResponseEntity.ok(surveyService.getSurveyAnalytics(filter));
            }

    @PostMapping("/public-analytics/share")
    public ResponseEntity<PublicAnalyticsShareResponse> createPublicAnalyticsShare(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String employmentStatus,
            @RequestParam(required = false) String licensureStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate submittedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate submittedTo,
            @RequestParam(required = false) String yearGraduated,
            @RequestParam(required = false, defaultValue = "168") long ttlHours
    ) {
        long safeTtlHours = Math.min(Math.max(ttlHours, 1), 720);
        Duration ttl = Duration.ofHours(safeTtlHours);

        AdminSurveyResponseFilter filter = new AdminSurveyResponseFilter(
            query,
            status,
            employmentStatus,
            licensureStatus,
            submittedFrom,
            submittedTo,
            yearGraduated
        );

        String token = jwtUtils.generatePublicAnalyticsToken(filter, ttl);
        Instant expiresAt = Instant.now().plus(ttl);
        String url = buildPublicAnalyticsUrl(token);

        return ResponseEntity.ok(new PublicAnalyticsShareResponse(token, url, expiresAt));
    }

    @GetMapping(value = "/export-csv", produces = "text/csv")
    public ResponseEntity<StreamingResponseBody> exportCsv() {
        StreamingResponseBody responseBody = outputStream -> surveyService.exportSurveyResponsesCsv(outputStream);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=alumni_survey_results.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(responseBody);
    }

    @GetMapping(value = "/notifications/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications() {
        return adminNotificationService.subscribe();
    }

    @GetMapping("/notifications/recent")
    public ResponseEntity<List<AdminNotificationEvent>> getRecentNotifications() {
        return ResponseEntity.ok(adminNotificationService.getRecentNotifications());
    }

    @GetMapping("/notifications/unread")
    public ResponseEntity<List<AdminNotificationEvent>> getUnreadNotifications(Authentication authentication) {
        return ResponseEntity.ok(adminNotificationService.getUnreadNotifications(authentication.getName()));
    }

    @PostMapping("/notifications/{notificationId}/read")
    public ResponseEntity<Map<String, Object>> markNotificationAsRead(
            @PathVariable java.util.UUID notificationId,
            Authentication authentication
    ) {
        adminNotificationService.markAsRead(notificationId, authentication.getName());
        return ResponseEntity.ok(Map.of("message", "Notification marked as read."));
    }

    @PostMapping("/notifications/read-all")
    public ResponseEntity<Map<String, Object>> markAllNotificationsAsRead(Authentication authentication) {
        int markedCount = adminNotificationService.markAllAsRead(authentication.getName());
        return ResponseEntity.ok(Map.of(
                "message", "All notifications marked as read.",
                "markedCount", markedCount
        ));
    }

    private Pageable toSafePageable(Pageable pageable) {
        int page = Math.max(pageable.getPageNumber(), 0);
        int size = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);
        Sort safeSort = toSafeSort(pageable.getSort());
        return PageRequest.of(page, size, safeSort);
    }

    private Sort toSafeSort(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return Sort.by(Sort.Direction.DESC, "submittedAt");
        }

        List<Sort.Order> safeOrders = sort.stream()
                .filter(order -> ALLOWED_SORT_FIELDS.contains(order.getProperty()))
                .map(order -> new Sort.Order(order.getDirection(), order.getProperty()))
                .toList();

        if (safeOrders.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "submittedAt");
        }

        return Sort.by(safeOrders);
    }

    private String buildPublicAnalyticsUrl(String token) {
        if (!StringUtils.hasText(publicAnalyticsBaseUrl)) {
            return null;
        }

        String base = publicAnalyticsBaseUrl.trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }

        return base + "/api/v1/public/analytics?token=" + token;
    }
}