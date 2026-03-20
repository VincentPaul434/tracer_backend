package cit.nurse.tracer.submission.service;

import cit.nurse.tracer.communication.model.CommunicationPreference;
import cit.nurse.tracer.communication.repository.CommunicationPreferenceRepository;
import cit.nurse.tracer.education.model.EducationalBackground;
import cit.nurse.tracer.education.repository.EducationalBackgroundRepository;
import cit.nurse.tracer.employment.model.EmploymentData;
import cit.nurse.tracer.employment.repository.EmploymentDataRepository;
import cit.nurse.tracer.evaluation.model.ProgramEvaluation;
import cit.nurse.tracer.evaluation.repository.ProgramEvaluationRepository;
import cit.nurse.tracer.licensure.model.LicensureExamination;
import cit.nurse.tracer.licensure.repository.LicensureExaminationRepository;
import cit.nurse.tracer.personalinfo.model.PersonalInfo;
import cit.nurse.tracer.personalinfo.repository.PersonalInfoRepository;
import cit.nurse.tracer.submission.dto.MasterSurveyRequest;
import cit.nurse.tracer.submission.dto.SurveyResponseDetail;
import cit.nurse.tracer.submission.dto.SurveySubmissionResponse;
import cit.nurse.tracer.submission.model.SurveySubmission;
import cit.nurse.tracer.submission.repository.SurveySubmissionRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SurveyService {

    private static final String SURVEY_RESPONSES_CACHE = "surveyResponsesPage";

    private final SurveySubmissionRepository submissionRepo;
    private final PersonalInfoRepository personalInfoRepo;
    private final EducationalBackgroundRepository educationRepo;
    private final LicensureExaminationRepository licensureRepo;
    private final EmploymentDataRepository employmentRepo;
    private final ProgramEvaluationRepository evaluationRepo;
    private final CommunicationPreferenceRepository communicationRepo;

    public SurveyService(
            SurveySubmissionRepository submissionRepo,
            PersonalInfoRepository personalInfoRepo,
            EducationalBackgroundRepository educationRepo,
            LicensureExaminationRepository licensureRepo,
            EmploymentDataRepository employmentRepo,
            ProgramEvaluationRepository evaluationRepo,
            CommunicationPreferenceRepository communicationRepo
    ) {
        this.submissionRepo = submissionRepo;
        this.personalInfoRepo = personalInfoRepo;
        this.educationRepo = educationRepo;
        this.licensureRepo = licensureRepo;
        this.employmentRepo = employmentRepo;
        this.evaluationRepo = evaluationRepo;
        this.communicationRepo = communicationRepo;
    }

    /**
     * Saves the entire survey in a single transaction.
     * If any part fails, the whole submission rolls back.
     */
    @CacheEvict(value = SURVEY_RESPONSES_CACHE, allEntries = true)
    @Transactional
    public SurveySubmissionResponse submitSurvey(MasterSurveyRequest request) {
        if (!"yes".equals(request.consent())) {
            throw new IllegalArgumentException("Privacy consent must be accepted to submit the survey.");
        }

        SurveySubmission submission = new SurveySubmission();
        submission.setEmail(request.email().trim().toLowerCase());
        submission.setHasAcceptedPrivacy(true);
        submission.setStatus(SurveySubmission.Status.FINALIZED);
        submission = submissionRepo.save(submission);

        savePersonalInfo(submission, request.personalInfo());
        saveEducationalBackground(submission, request.educationalBackground());
        saveLicensureExamination(submission, request.licensureExamination());
        saveEmployment(submission, request.employment());
        saveProgramEvaluation(submission, request.programEvaluation());
        saveCommunicationPreference(submission, request.communicationPreference());

        return new SurveySubmissionResponse(submission.getId(), "Survey submitted successfully.");
    }

    @Transactional(readOnly = true)
    @Cacheable(
        value = SURVEY_RESPONSES_CACHE,
        key = "#pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()"
    )
    public Page<SurveyResponseDetail> getSurveyResponses(Pageable pageable) {
    Page<SurveySubmission> submissionsPage = submissionRepo.findAll(pageable);
    if (submissionsPage.isEmpty()) {
        return Page.empty(pageable);
    }

    List<SurveySubmission> submissions = submissionsPage.getContent();

    Map<UUID, PersonalInfo> personalInfoBySubmissionId = personalInfoRepo.findBySubmissionIn(submissions).stream()
        .collect(Collectors.toMap(item -> item.getSubmission().getId(), Function.identity(), (left, right) -> left));

    Map<UUID, EducationalBackground> educationBySubmissionId = educationRepo.findBySubmissionIn(submissions).stream()
        .collect(Collectors.toMap(item -> item.getSubmission().getId(), Function.identity(), (left, right) -> left));

    Map<UUID, LicensureExamination> licensureBySubmissionId = licensureRepo.findBySubmissionIn(submissions).stream()
        .collect(Collectors.toMap(item -> item.getSubmission().getId(), Function.identity(), (left, right) -> left));

    Map<UUID, EmploymentData> employmentBySubmissionId = employmentRepo.findBySubmissionIn(submissions).stream()
        .collect(Collectors.toMap(item -> item.getSubmission().getId(), Function.identity(), (left, right) -> left));

    Map<UUID, ProgramEvaluation> evaluationBySubmissionId = evaluationRepo.findBySubmissionIn(submissions).stream()
        .collect(Collectors.toMap(item -> item.getSubmission().getId(), Function.identity(), (left, right) -> left));

    Map<UUID, CommunicationPreference> communicationBySubmissionId = communicationRepo.findBySubmissionIn(submissions).stream()
        .collect(Collectors.toMap(item -> item.getSubmission().getId(), Function.identity(), (left, right) -> left));

    List<SurveyResponseDetail> responses = submissions.stream()
        .map(submission -> toSurveyResponseDetail(
            submission,
            personalInfoBySubmissionId.get(submission.getId()),
            educationBySubmissionId.get(submission.getId()),
            licensureBySubmissionId.get(submission.getId()),
            employmentBySubmissionId.get(submission.getId()),
            evaluationBySubmissionId.get(submission.getId()),
            communicationBySubmissionId.get(submission.getId())
        ))
        .toList();

    return new PageImpl<>(responses, pageable, submissionsPage.getTotalElements());
    }

    private SurveyResponseDetail toSurveyResponseDetail(
        SurveySubmission submission,
        PersonalInfo personalInfo,
        EducationalBackground educationalBackground,
        LicensureExamination licensureExamination,
        EmploymentData employmentData,
        ProgramEvaluation programEvaluation,
        CommunicationPreference communicationPreference
    ) {
    return new SurveyResponseDetail(
        submission.getId(),
        submission.getEmail(),
        submission.isHasAcceptedPrivacy(),
        submission.getStatus() == null ? null : submission.getStatus().name(),
        submission.getSubmittedAt(),
        submission.getCreatedAt(),
        submission.getUpdatedAt(),
        toPersonalInfoSection(personalInfo),
        toEducationalBackgroundSection(educationalBackground),
        toLicensureExaminationSection(licensureExamination),
        toEmploymentSection(employmentData),
        toProgramEvaluationSection(programEvaluation),
        toCommunicationPreferenceSection(communicationPreference)
    );
    }

    private SurveyResponseDetail.PersonalInfoSection toPersonalInfoSection(PersonalInfo personalInfo) {
    if (personalInfo == null) {
        return null;
    }
    return new SurveyResponseDetail.PersonalInfoSection(
        personalInfo.getFullName(),
        personalInfo.getGender(),
        personalInfo.getGenderOther(),
        personalInfo.getCivilStatus(),
        personalInfo.getCivilStatusOther(),
        personalInfo.getBirthday(),
        personalInfo.getResidence(),
        personalInfo.getContactInformation()
    );
    }

    private SurveyResponseDetail.EducationalBackgroundSection toEducationalBackgroundSection(EducationalBackground educationalBackground) {
    if (educationalBackground == null) {
        return null;
    }
    return new SurveyResponseDetail.EducationalBackgroundSection(
        educationalBackground.getDegreeProgramCompleted(),
        educationalBackground.getYearGraduated(),
        educationalBackground.getYearGraduatedOther(),
        educationalBackground.getAcademicHonors(),
        educationalBackground.getAcademicHonorsOtherText(),
        educationalBackground.getPursuedFurtherStudies(),
        educationalBackground.getFurtherDegreeProgram(),
        educationalBackground.getFurtherStudiesReason(),
        educationalBackground.getFurtherStudiesReasonOtherText()
    );
    }

    private SurveyResponseDetail.LicensureExaminationSection toLicensureExaminationSection(LicensureExamination licensureExamination) {
    if (licensureExamination == null) {
        return null;
    }
    return new SurveyResponseDetail.LicensureExaminationSection(
        licensureExamination.getHasTakenPnle(),
        licensureExamination.getLicensureStatus(),
        licensureExamination.getPnleYearPassed(),
        licensureExamination.getPnleYearPassedOther(),
        licensureExamination.getExamTakeCount()
    );
    }

    private SurveyResponseDetail.EmploymentSection toEmploymentSection(EmploymentData employmentData) {
    if (employmentData == null) {
        return null;
    }
    return new SurveyResponseDetail.EmploymentSection(
        employmentData.getEmploymentStatus(),
        employmentData.getJobRelatedToDegree(),
        employmentData.getEmploymentSector(),
        employmentData.getEmploymentSectorOther(),
        employmentData.getPositionDesignation(),
        employmentData.getPositionDesignationOther(),
        employmentData.getFirstJobDuration(),
        employmentData.getFirstJobSources(),
        employmentData.getFirstJobSourceOtherText(),
        employmentData.getEstimatedMonthlySalary(),
        employmentData.getUnemploymentReasons(),
        employmentData.getUnemploymentReasonOtherText()
    );
    }

    private SurveyResponseDetail.ProgramEvaluationSection toProgramEvaluationSection(ProgramEvaluation programEvaluation) {
    if (programEvaluation == null) {
        return null;
    }
    return new SurveyResponseDetail.ProgramEvaluationSection(
        programEvaluation.getRelevanceSkills(),
        programEvaluation.getCareerPreparationLevel(),
        programEvaluation.getNursingProgramAspect(),
        programEvaluation.getNursingProgramSuggestion()
    );
    }

    private SurveyResponseDetail.CommunicationPreferenceSection toCommunicationPreferenceSection(CommunicationPreference communicationPreference) {
    if (communicationPreference == null) {
        return null;
    }
    return new SurveyResponseDetail.CommunicationPreferenceSection(
        communicationPreference.getInvitationChannels(),
        communicationPreference.getInvitationChannelOtherText(),
        communicationPreference.getUpdateFrequency(),
        communicationPreference.getAlumniGroupWillingness(),
        communicationPreference.getAlumniPlatform()
    );
    }

    private void savePersonalInfo(SurveySubmission submission, MasterSurveyRequest.PersonalInfoSection data) {
        PersonalInfo entity = new PersonalInfo();
        entity.setSubmission(submission);
        entity.setFullName(data.fullName());
        entity.setGender(data.gender());
        entity.setGenderOther(data.genderOther());
        entity.setCivilStatus(data.civilStatus());
        entity.setCivilStatusOther(data.civilStatusOther());
        entity.setBirthday(data.birthday());
        entity.setResidence(data.residence());
        entity.setContactInformation(data.contactInformation());
        personalInfoRepo.save(entity);
    }

    private void saveEducationalBackground(SurveySubmission submission, MasterSurveyRequest.EducationalBackgroundSection data) {
        EducationalBackground entity = new EducationalBackground();
        entity.setSubmission(submission);
        entity.setDegreeProgramCompleted(data.degreeProgramCompleted());
        entity.setYearGraduated(data.yearGraduated());
        entity.setYearGraduatedOther(data.yearGraduatedOther());
        entity.setAcademicHonors(extractSelectedKeys(data.academicHonors()));
        entity.setAcademicHonorsOtherText(data.academicHonorsOtherText());

        boolean pursuedFurtherStudies = isYes(data.pursuedFurtherStudies());
        entity.setPursuedFurtherStudies(pursuedFurtherStudies);

        entity.setFurtherDegreeProgram(pursuedFurtherStudies ? data.furtherDegreeProgram() : null);
        entity.setFurtherStudiesReason(pursuedFurtherStudies ? data.furtherStudiesReason() : null);
        entity.setFurtherStudiesReasonOtherText(
            pursuedFurtherStudies && isOther(data.furtherStudiesReason())
                ? data.furtherStudiesReasonOtherText()
                : null
        );

        educationRepo.save(entity);
    }

    private void saveLicensureExamination(SurveySubmission submission, MasterSurveyRequest.LicensureExaminationSection data) {
        LicensureExamination entity = new LicensureExamination();
        entity.setSubmission(submission);
        boolean taken = "Yes".equals(data.hasTakenPnle());
        entity.setHasTakenPnle(taken);
        if (taken) {
            entity.setLicensureStatus(data.licensureStatus());
            entity.setPnleYearPassed(data.pnleYearPassed());
            entity.setPnleYearPassedOther(data.pnleYearPassedOther());
            entity.setExamTakeCount(data.examTakeCount());
        }
        licensureRepo.save(entity);
    }

    private void saveEmployment(SurveySubmission submission, MasterSurveyRequest.EmploymentSection data) {
        EmploymentData entity = new EmploymentData();
        entity.setSubmission(submission);
        entity.setEmploymentStatus(data.employmentStatus());

        boolean isEmployed = "Employed".equals(data.employmentStatus())
                || "Self employed".equals(data.employmentStatus());
        boolean isUnemployed = "Unemployed".equals(data.employmentStatus())
                || "Currently studying".equals(data.employmentStatus());

        if (isEmployed) {
            entity.setJobRelatedToDegree(data.jobRelatedToDegree());
            entity.setEmploymentSector(data.employmentSector());
            entity.setEmploymentSectorOther(data.employmentSectorOther());
            entity.setPositionDesignation(data.positionDesignation());
            entity.setPositionDesignationOther(data.positionDesignationOther());
            entity.setFirstJobDuration(data.firstJobDuration());
            entity.setFirstJobSources(extractSelectedKeys(data.firstJobSources()));
            entity.setFirstJobSourceOtherText(data.firstJobSourceOtherText());
            entity.setEstimatedMonthlySalary(data.estimatedMonthlySalary());
        }

        if (isUnemployed) {
            entity.setUnemploymentReasons(extractSelectedKeys(data.unemploymentReasons()));
            entity.setUnemploymentReasonOtherText(data.unemploymentReasonOtherText());
        }

        employmentRepo.save(entity);
    }

    private void saveProgramEvaluation(SurveySubmission submission, MasterSurveyRequest.ProgramEvaluationSection data) {
        ProgramEvaluation entity = new ProgramEvaluation();
        entity.setSubmission(submission);
        entity.setRelevanceSkills(extractSelectedKeys(data.relevanceSkills()));
        entity.setCareerPreparationLevel(data.careerPreparationLevel());
        entity.setNursingProgramAspect(data.nursingProgramAspect());
        entity.setNursingProgramSuggestion(data.nursingProgramSuggestion());
        evaluationRepo.save(entity);
    }

    private void saveCommunicationPreference(SurveySubmission submission, MasterSurveyRequest.CommunicationPreferenceSection data) {
        CommunicationPreference entity = new CommunicationPreference();
        entity.setSubmission(submission);
        entity.setInvitationChannels(extractSelectedKeys(data.invitationChannels()));
        entity.setInvitationChannelOtherText(data.invitationChannelOtherText());
        entity.setUpdateFrequency(data.updateFrequency());
        entity.setAlumniGroupWillingness(data.alumniGroupWillingness());
        if ("Yes".equals(data.alumniGroupWillingness())) {
            entity.setAlumniPlatform(data.alumniPlatform());
        }
        communicationRepo.save(entity);
    }

    /**
     * Converts a frontend boolean-flag map to a comma-separated string of selected keys.
     * Input:  { "clinicalSkills": true, "criticalThinking": false, "teamwork": true }
     * Output: "clinicalSkills,teamwork"
     */
    private String extractSelectedKeys(Map<String, Boolean> flags) {
        if (flags == null || flags.isEmpty()) {
            return null;
        }
        String result = flags.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.joining(","));
        return result.isEmpty() ? null : result;
    }

    private boolean isYes(String value) {
        return value != null && "yes".equalsIgnoreCase(value.trim());
    }

    private boolean isOther(String value) {
        return value != null && "other".equalsIgnoreCase(value.trim());
    }
}