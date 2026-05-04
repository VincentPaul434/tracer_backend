package cit.nurse.tracer.submission.controller;

import cit.nurse.tracer.submission.dto.MasterSurveyRequest;
import cit.nurse.tracer.submission.dto.IdUploadResponse;
import cit.nurse.tracer.submission.dto.SurveyResponseDetail;
import cit.nurse.tracer.submission.dto.SurveySubmissionResponse;
import cit.nurse.tracer.submission.service.IdUploadService;
import cit.nurse.tracer.submission.service.SurveyService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/submissions")
public class SubmissionController {

    private final SurveyService surveyService;
    private final IdUploadService idUploadService;

    public SubmissionController(SurveyService surveyService, IdUploadService idUploadService) {
        this.surveyService = surveyService;
        this.idUploadService = idUploadService;
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

    @PutMapping("/{submissionId}")
    public ResponseEntity<SurveySubmissionResponse> updateSurvey(
            @PathVariable UUID submissionId,
            @Valid @RequestBody MasterSurveyRequest request
    ) {
        SurveySubmissionResponse response = surveyService.updateSurvey(submissionId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/edit/{editToken}")
    public ResponseEntity<SurveySubmissionResponse> updateSurveyByEditToken(
            @PathVariable String editToken,
            @Valid @RequestBody MasterSurveyRequest request
    ) {
        SurveySubmissionResponse response = surveyService.updateSurveyByEditToken(editToken, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/edit/{editToken}")
    public ResponseEntity<SurveyResponseDetail> getSurveyByEditToken(
            @PathVariable String editToken
    ) {
        return ResponseEntity.ok(surveyService.getSurveyResponseByEditToken(editToken));
    }

    @PostMapping(value = "/id-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<IdUploadResponse> uploadId(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(idUploadService.uploadId(file));
    }
}
