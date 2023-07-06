package ca.bc.gov.educ.api.graduation.model.report;

import ca.bc.gov.educ.api.graduation.util.JSonNullStringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;

public class Mark implements Serializable {
    private static final long serialVersionUID = 2L;

    private String schoolPercent;
    private String examPercent;
    private String finalPercent;
    private String finalLetterGrade;
    private String interimPercent;
    private String interimLetterGrade;
    private Double completedCoursePercentage;

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

    public Double getCompletedCoursePercentage() {
        return completedCoursePercentage;
    }

    public void setCompletedCoursePercentage(Double completedCoursePercentage) {
        this.completedCoursePercentage = completedCoursePercentage;
    }
}
