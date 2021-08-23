package ca.bc.gov.educ.api.graduation.model.report;

import java.util.List;

public class ReportData {
    private Student student;
    private School school;
    private String logo;
    private List<NonGradReason> nonGradReasons;
    private Transcript transcript;
    private GradProgram gradProgram;
    private GraduationData graduationData;
    private String gradMessage;
    private String updateDate;
    private Certificate certificate;

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student value) {
        this.student = value;
    }

    public School getSchool() {
        return school;
    }

    public void setSchool(School value) {
        this.school = value;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String value) {
        this.logo = value;
    }

    public List<NonGradReason> getNonGradReasons() {
        return nonGradReasons;
    }

    public void setNonGradReasons(List<NonGradReason> value) {
        this.nonGradReasons = value;
    }

    public Transcript getTranscript() {
        return transcript;
    }

    public void setTranscript(Transcript value) {
        this.transcript = value;
    }

    public GradProgram getGradProgram() {
        return gradProgram;
    }

    public void setGradProgram(GradProgram value) {
        this.gradProgram = value;
    }

    public GraduationData getGraduationData() {
        return graduationData;
    }

    public void setGraduationData(GraduationData value) {
        this.graduationData = value;
    }

    public String getGradMessage() {
        return gradMessage;
    }

    public void setGradMessage(String value) {
        this.gradMessage = value;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String value) {
        this.updateDate = value;
    }

    public Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(Certificate value) {
        this.certificate = value;
    }
}
