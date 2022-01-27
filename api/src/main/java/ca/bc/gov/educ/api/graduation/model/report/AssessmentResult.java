package ca.bc.gov.educ.api.graduation.model.report;

import java.io.Serializable;

public class AssessmentResult implements Serializable {

    private static final long serialVersionUID = 2L;

    private String assessmentName;
    private String assessmentCode;
    private Double proficiencyScore;
    private String sessionDate;
    private String gradReqMet;
    private String specialCase;
    private String exceededWriteFlag;

    public String getAssessmentName() {
        return assessmentName;
    }

    public void setAssessmentName(String assessmentName) {
        this.assessmentName = assessmentName;
    }

    public String getAssessmentCode() {
        return assessmentCode;
    }

    public void setAssessmentCode(String assessmentCode) {
        this.assessmentCode = assessmentCode;
    }

    public Double getProficiencyScore() {
        return proficiencyScore;
    }

    public void setProficiencyScore(Double proficiencyScore) {
        this.proficiencyScore = proficiencyScore;
    }

    public String getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(String sessionDate) {
        this.sessionDate = sessionDate;
    }

    public String getGradReqMet() {
        return gradReqMet;
    }

    public void setGradReqMet(String gradReqMet) {
        this.gradReqMet = gradReqMet;
    }

    public String getSpecialCase() {
        return specialCase;
    }

    public void setSpecialCase(String specialCase) {
        this.specialCase = specialCase;
    }

    public String getExceededWriteFlag() {
        return exceededWriteFlag;
    }

    public void setExceededWriteFlag(String exceededWriteFlag) {
        this.exceededWriteFlag = exceededWriteFlag;
    }
}
