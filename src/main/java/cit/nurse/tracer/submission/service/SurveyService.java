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
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SurveyService {

    private static final String SURVEY_RESPONSES_CACHE = "surveyResponsesPage";
    private static final int EXPORT_BATCH_SIZE = 500;
    private static final String[] CSV_HEADERS = {
            "submission_id",
            "email",
            "has_accepted_privacy",
            "status",
            "submitted_at",
            "created_at",
            "updated_at",
            "personal_full_name",
            "personal_gender",
            "personal_gender_other",
            "personal_civil_status",
            "personal_civil_status_other",
            "personal_birthday",
            "personal_residence",
            "personal_contact_information",
            "education_degree_program_completed",
            "education_year_graduated",
            "education_year_graduated_other",
            "education_academic_honors",
            "education_academic_honors_other_text",
            "education_pursued_further_studies",
            "education_further_degree_program",
            "licensure_has_taken_pnle",
            "licensure_status",
            "licensure_pnle_year_passed",
            "licensure_pnle_year_passed_other",
            "licensure_exam_take_count",
            "employment_status",
            "employment_job_related_to_degree",
            "employment_sector",
            "employment_sector_other",
            "employment_position_designation",
            "employment_position_designation_other",
            "employment_first_job_duration",
            "employment_first_job_sources",
            "employment_first_job_source_other_text",
            "employment_estimated_monthly_salary",
            "employment_unemployment_reasons",
            "employment_unemployment_reason_other_text",
            "evaluation_relevance_skills",
            "evaluation_career_preparation_level",
            "evaluation_nursing_program_aspect",
            "evaluation_nursing_program_suggestion",
            "communication_invitation_channels",
            "communication_invitation_channel_other_text",
            "communication_update_frequency",
            "communication_alumni_group_willingness",
            "communication_alumni_platform"
    };

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
        SurveySections sections = loadSurveySections(submissions);

        List<SurveyResponseDetail> responses = submissions.stream()
                .map(submission -> toSurveyResponseDetail(
                        submission,
                        sections.personalInfoBySubmissionId().get(submission.getId()),
                        sections.educationBySubmissionId().get(submission.getId()),
                        sections.licensureBySubmissionId().get(submission.getId()),
                        sections.employmentBySubmissionId().get(submission.getId()),
                        sections.evaluationBySubmissionId().get(submission.getId()),
                        sections.communicationBySubmissionId().get(submission.getId())
                ))
                .toList();

        return new PageImpl<>(responses, pageable, submissionsPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public void exportSurveyResponsesCsv(OutputStream outputStream) throws IOException {
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(CSV_HEADERS)
                .setSkipHeaderRecord(false)
                .build();

        try (
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)
        ) {
            Pageable pageable = PageRequest.of(
                    0,
                    EXPORT_BATCH_SIZE,
                    Sort.by(Sort.Direction.ASC, "submittedAt").and(Sort.by(Sort.Direction.ASC, "id"))
            );

            Page<SurveySubmission> submissionsPage;
            do {
                submissionsPage = submissionRepo.findAll(pageable);
                if (submissionsPage.isEmpty()) {
                    break;
                }

                List<SurveySubmission> submissions = submissionsPage.getContent();
                SurveySections sections = loadSurveySections(submissions);

                for (SurveySubmission submission : submissions) {
                    writeCsvRecord(
                            csvPrinter,
                            submission,
                            sections.personalInfoBySubmissionId().get(submission.getId()),
                            sections.educationBySubmissionId().get(submission.getId()),
                            sections.licensureBySubmissionId().get(submission.getId()),
                            sections.employmentBySubmissionId().get(submission.getId()),
                            sections.evaluationBySubmissionId().get(submission.getId()),
                            sections.communicationBySubmissionId().get(submission.getId())
                    );
                }

                csvPrinter.flush();
                pageable = submissionsPage.nextPageable();
            } while (submissionsPage.hasNext());
        }
    }

    private SurveySections loadSurveySections(List<SurveySubmission> submissions) {
        Map<UUID, PersonalInfo> personalInfoBySubmissionId = toSubmissionIdMap(
                personalInfoRepo.findBySubmissionIn(submissions),
                PersonalInfo::getSubmission
        );

        Map<UUID, EducationalBackground> educationBySubmissionId = toSubmissionIdMap(
                educationRepo.findBySubmissionIn(submissions),
                EducationalBackground::getSubmission
        );

        Map<UUID, LicensureExamination> licensureBySubmissionId = toSubmissionIdMap(
                licensureRepo.findBySubmissionIn(submissions),
                LicensureExamination::getSubmission
        );

        Map<UUID, EmploymentData> employmentBySubmissionId = toSubmissionIdMap(
                employmentRepo.findBySubmissionIn(submissions),
                EmploymentData::getSubmission
        );

        Map<UUID, ProgramEvaluation> evaluationBySubmissionId = toSubmissionIdMap(
                evaluationRepo.findBySubmissionIn(submissions),
                ProgramEvaluation::getSubmission
        );

        Map<UUID, CommunicationPreference> communicationBySubmissionId = toSubmissionIdMap(
                communicationRepo.findBySubmissionIn(submissions),
                CommunicationPreference::getSubmission
        );

        return new SurveySections(
                personalInfoBySubmissionId,
                educationBySubmissionId,
                licensureBySubmissionId,
                employmentBySubmissionId,
                evaluationBySubmissionId,
                communicationBySubmissionId
        );
    }

    private <T> Map<UUID, T> toSubmissionIdMap(List<T> entities, Function<T, SurveySubmission> submissionExtractor) {
        return entities.stream()
                .collect(Collectors.toMap(
                        entity -> submissionExtractor.apply(entity).getId(),
                        Function.identity(),
                        (left, right) -> left
                ));
    }

    private void writeCsvRecord(
            CSVPrinter csvPrinter,
            SurveySubmission submission,
            PersonalInfo personalInfo,
            EducationalBackground educationalBackground,
            LicensureExamination licensureExamination,
            EmploymentData employmentData,
            ProgramEvaluation programEvaluation,
            CommunicationPreference communicationPreference
    ) throws IOException {
        csvPrinter.printRecord(
                toCsvValue(submission.getId()),
                toCsvValue(submission.getEmail()),
                toCsvValue(submission.isHasAcceptedPrivacy()),
                toCsvValue(submission.getStatus() == null ? null : submission.getStatus().name()),
                toCsvValue(submission.getSubmittedAt()),
                toCsvValue(submission.getCreatedAt()),
                toCsvValue(submission.getUpdatedAt()),
                toCsvValue(personalInfo, PersonalInfo::getFullName),
                toCsvValue(personalInfo, PersonalInfo::getGender),
                toCsvValue(personalInfo, PersonalInfo::getGenderOther),
                toCsvValue(personalInfo, PersonalInfo::getCivilStatus),
                toCsvValue(personalInfo, PersonalInfo::getCivilStatusOther),
                toCsvValue(personalInfo, PersonalInfo::getBirthday),
                toCsvValue(personalInfo, PersonalInfo::getResidence),
                toCsvValue(personalInfo, PersonalInfo::getContactInformation),
                toCsvValue(educationalBackground, EducationalBackground::getDegreeProgramCompleted),
                toCsvValue(educationalBackground, EducationalBackground::getYearGraduated),
                toCsvValue(educationalBackground, EducationalBackground::getYearGraduatedOther),
                toCsvValue(educationalBackground, EducationalBackground::getAcademicHonors),
                toCsvValue(educationalBackground, EducationalBackground::getAcademicHonorsOtherText),
                toCsvValue(educationalBackground, EducationalBackground::getPursuedFurtherStudies),
                toCsvValue(educationalBackground, EducationalBackground::getFurtherDegreeProgram),
                toCsvValue(licensureExamination, LicensureExamination::getHasTakenPnle),
                toCsvValue(licensureExamination, LicensureExamination::getLicensureStatus),
                toCsvValue(licensureExamination, LicensureExamination::getPnleYearPassed),
                toCsvValue(licensureExamination, LicensureExamination::getPnleYearPassedOther),
                toCsvValue(licensureExamination, LicensureExamination::getExamTakeCount),
                toCsvValue(employmentData, EmploymentData::getEmploymentStatus),
                toCsvValue(employmentData, EmploymentData::getJobRelatedToDegree),
                toCsvValue(employmentData, EmploymentData::getEmploymentSector),
                toCsvValue(employmentData, EmploymentData::getEmploymentSectorOther),
                toCsvValue(employmentData, EmploymentData::getPositionDesignation),
                toCsvValue(employmentData, EmploymentData::getPositionDesignationOther),
                toCsvValue(employmentData, EmploymentData::getFirstJobDuration),
                toCsvValue(employmentData, EmploymentData::getFirstJobSources),
                toCsvValue(employmentData, EmploymentData::getFirstJobSourceOtherText),
                toCsvValue(employmentData, EmploymentData::getEstimatedMonthlySalary),
                toCsvValue(employmentData, EmploymentData::getUnemploymentReasons),
                toCsvValue(employmentData, EmploymentData::getUnemploymentReasonOtherText),
                toCsvValue(programEvaluation, ProgramEvaluation::getRelevanceSkills),
                toCsvValue(programEvaluation, ProgramEvaluation::getCareerPreparationLevel),
                toCsvValue(programEvaluation, ProgramEvaluation::getNursingProgramAspect),
                toCsvValue(programEvaluation, ProgramEvaluation::getNursingProgramSuggestion),
                toCsvValue(communicationPreference, CommunicationPreference::getInvitationChannels),
                toCsvValue(communicationPreference, CommunicationPreference::getInvitationChannelOtherText),
                toCsvValue(communicationPreference, CommunicationPreference::getUpdateFrequency),
                toCsvValue(communicationPreference, CommunicationPreference::getAlumniGroupWillingness),
                toCsvValue(communicationPreference, CommunicationPreference::getAlumniPlatform)
        );
    }

    private String toCsvValue(Object value) {
        return value == null ? "" : value.toString();
    }

    private <T> String toCsvValue(T source, Function<T, ?> extractor) {
        if (source == null) {
            return "";
        }
        return toCsvValue(extractor.apply(source));
    }

    private record SurveySections(
            Map<UUID, PersonalInfo> personalInfoBySubmissionId,
            Map<UUID, EducationalBackground> educationBySubmissionId,
            Map<UUID, LicensureExamination> licensureBySubmissionId,
            Map<UUID, EmploymentData> employmentBySubmissionId,
            Map<UUID, ProgramEvaluation> evaluationBySubmissionId,
            Map<UUID, CommunicationPreference> communicationBySubmissionId
    ) {
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
        educationalBackground.getFurtherDegreeProgram()
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
        entity.setPursuedFurtherStudies("Yes".equals(data.pursuedFurtherStudies()));
        entity.setFurtherDegreeProgram(data.furtherDegreeProgram());
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
}
