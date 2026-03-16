package cit.nurse.tracer.personalinfo.model;

import cit.nurse.tracer.core.util.BaseEntity;
import cit.nurse.tracer.submission.model.SurveySubmission;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "personal_info")
public class PersonalInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private SurveySubmission submission;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "gender")
    private String gender;

    @Column(name = "gender_other")
    private String genderOther;

    @Column(name = "civil_status")
    private String civilStatus;

    @Column(name = "civil_status_other")
    private String civilStatusOther;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "residence")
    private String residence;

    @Column(name = "contact_information")
    private String contactInformation;

    public UUID getId() {
        return id;
    }

    public SurveySubmission getSubmission() {
        return submission;
    }

    public void setSubmission(SurveySubmission submission) {
        this.submission = submission;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGenderOther() {
        return genderOther;
    }

    public void setGenderOther(String genderOther) {
        this.genderOther = genderOther;
    }

    public String getCivilStatus() {
        return civilStatus;
    }

    public void setCivilStatus(String civilStatus) {
        this.civilStatus = civilStatus;
    }

    public String getCivilStatusOther() {
        return civilStatusOther;
    }

    public void setCivilStatusOther(String civilStatusOther) {
        this.civilStatusOther = civilStatusOther;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getResidence() {
        return residence;
    }

    public void setResidence(String residence) {
        this.residence = residence;
    }

    public String getContactInformation() {
        return contactInformation;
    }

    public void setContactInformation(String contactInformation) {
        this.contactInformation = contactInformation;
    }
}