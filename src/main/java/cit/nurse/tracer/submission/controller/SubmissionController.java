package cit.nurse.tracer.submission.controller;

import cit.nurse.tracer.submission.dto.MasterSurveyRequest;
import cit.nurse.tracer.submission.dto.SurveySubmissionResponse;
import cit.nurse.tracer.submission.service.SurveyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/submissions")
public class SubmissionController {

    private final SurveyService surveyService;

    public SubmissionController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    /**
     * Single endpoint for the entire alumni tracer survey.
     * Accepts the full form payload and saves all sections transactionally.
     *
     * POST /api/v1/submissions
     * Body: MasterSurveyRequest (JSON)
     * Returns: 201 Created with submissionId
     */
    @PostMapping
    public ResponseEntity<SurveySubmissionResponse> submitSurvey(
            @Valid @RequestBody MasterSurveyRequest request
    ) {
        SurveySubmissionResponse response = surveyService.submitSurvey(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
