package ca.bc.gov.educ.api.graduation.model.report;

public class Mark {
    private String schoolPercent;
    private String examPercent;
    private String finalPercent;
    private String finalLetterGrade;
    private String interimPercent;
    private String interimLetterGrade;

    public String getSchoolPercent() {
        return schoolPercent;
    }

    public void setSchoolPercent(String value) {
        this.schoolPercent = value;
    }

    public String getExamPercent() {
        return examPercent;
    }

    public void setExamPercent(String value) {
        this.examPercent = value;
    }

    public String getFinalPercent() {
        return finalPercent;
    }

    public void setFinalPercent(String value) {
        this.finalPercent = value;
    }

    public String getFinalLetterGrade() {
        return finalLetterGrade;
    }

    public void setFinalLetterGrade(String value) {
        this.finalLetterGrade = value;
    }

    public String getInterimPercent() {
        return interimPercent;
    }

    public void setInterimPercent(String value) {
        this.interimPercent = value;
    }

    public String getInterimLetterGrade() {
        return interimLetterGrade;
    }

    public void setInterimLetterGrade(String value) {
        this.interimLetterGrade = value;
    }
}
