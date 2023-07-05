package ca.bc.gov.educ.api.graduation.model.report;

import ca.bc.gov.educ.api.graduation.util.JSonNullStringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Objects;

public class TranscriptCourse {

    private String name = "";
    private String code = "";
    private String level = "";
    private String credits = "";
    private String sessionDate = "";
    private String type = "";
    private String relatedCourse = "";
    private String relatedLevel = "";
    //Grad2-1931
    private String specialCase = "";
    //Grad2-2182
    private Boolean isUsed;
    private Double proficiencyScore;
    private String customizedCourseName;
    private Integer originalCredits;
    private String genericCourseType;
    private Integer credit;



    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String value) {
        this.code = value;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String value) {
        this.level = value;
    }

    public String getCredits() {
        return credits;
    }

    public void setCredits(String value) {
        this.credits = value;
    }

    public String getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(String value) {
        this.sessionDate = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }

    @JsonSerialize(nullsUsing = JSonNullStringSerializer.class)
    public String getRelatedCourse() {
        return relatedCourse;
    }

    public void setRelatedCourse(String value) {
        this.relatedCourse = value;
    }

    @JsonSerialize(nullsUsing = JSonNullStringSerializer.class)
    public String getRelatedLevel() {
        return relatedLevel;
    }

    public void setRelatedLevel(String value) {
        this.relatedLevel = value;
    }

    @JsonSerialize(nullsUsing = JSonNullStringSerializer.class)
    public String getSpecialCase() { return specialCase; }

    public void setSpecialCase(String specialCase) { this.specialCase = specialCase; }

    @JsonSerialize(nullsUsing = JSonNullStringSerializer.class)
    public Boolean getUsed() { return isUsed; }

    public void setUsed(Boolean used) { isUsed = used; }


    //Grad2-2182 - mchintha
    @JsonSerialize(nullsUsing = JSonNullStringSerializer.class)
    public Double getProficiencyScore() {
        return proficiencyScore;
    }

    public void setProficiencyScore(Double proficiencyScore) {
        this.proficiencyScore = proficiencyScore;
    }

    @JsonSerialize(nullsUsing = JSonNullStringSerializer.class)
    public String getCustomizedCourseName() {
        return customizedCourseName;
    }

    public void setCustomizedCourseName(String customizedCourseName) {
        this.customizedCourseName = customizedCourseName;
    }

    @JsonSerialize(nullsUsing = JSonNullStringSerializer.class)
    public Integer getOriginalCredits() {
        return originalCredits;
    }

    public void setOriginalCredits(Integer originalCredits) {
        this.originalCredits = originalCredits;
    }

    @JsonSerialize(nullsUsing = JSonNullStringSerializer.class)
    public String getGenericCourseType() {
        return genericCourseType;
    }

    public void setGenericCourseType(String genericCourseType) {
        this.genericCourseType = genericCourseType;
    }

    @JsonSerialize(nullsUsing = JSonNullStringSerializer.class)
    public Integer getCredit() {
        return credit;
    }

    public void setCredit(Integer credits) {
        credit = credits;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TranscriptCourse that = (TranscriptCourse) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
