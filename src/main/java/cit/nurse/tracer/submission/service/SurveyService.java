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
import cit.nurse.tracer.submission.dto.SurveySubmissionResponse;
import cit.nurse.tracer.submission.model.SurveySubmission;
import cit.nurse.tracer.submission.repository.SurveySubmissionRepository;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SurveyService {

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