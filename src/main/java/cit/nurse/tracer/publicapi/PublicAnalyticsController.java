package cit.nurse.tracer.publicapi;

import cit.nurse.tracer.submission.dto.AdminSurveyResponseFilter;
import cit.nurse.tracer.submission.dto.SurveyAnalyticsResponse;
import cit.nurse.tracer.submission.service.SurveyService;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/analytics")
public class PublicAnalyticsController {

    private final SurveyService surveyService;
    private final String publicAnalyticsToken;

    public PublicAnalyticsController(
            SurveyService surveyService,
            @Value("${app.security.public-analytics-token:}") String publicAnalyticsToken
    ) {
        this.surveyService = surveyService;
        this.publicAnalyticsToken = publicAnalyticsToken;
    }

    @GetMapping
    public ResponseEntity<SurveyAnalyticsResponse> getPublicAnalytics(
            @RequestParam("token") String token,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String employmentStatus,
            @RequestParam(required = false) String licensureStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate submittedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate submittedTo,
            @RequestParam(required = false) String yearGraduated
    ) {
        if (!isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

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

    private boolean isTokenValid(String token) {
        return StringUtils.hasText(publicAnalyticsToken)
                && StringUtils.hasText(token)
                && publicAnalyticsToken.equals(token.trim());
    }
}
