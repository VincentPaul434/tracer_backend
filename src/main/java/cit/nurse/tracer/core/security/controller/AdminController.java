package cit.nurse.tracer.core.security.controller;

import cit.nurse.tracer.submission.dto.SurveyResponseDetail;
import cit.nurse.tracer.submission.service.SurveyService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

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

    public AdminController(SurveyService surveyService) {
        this.surveyService = surveyService;
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
            @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = "submittedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(surveyService.getSurveyResponses(toSafePageable(pageable)));
    }

    @GetMapping(value = "/export-csv", produces = "text/csv")
    public ResponseEntity<StreamingResponseBody> exportCsv() {
        StreamingResponseBody responseBody = outputStream -> surveyService.exportSurveyResponsesCsv(outputStream);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=alumni_survey_results.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(responseBody);
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
}
