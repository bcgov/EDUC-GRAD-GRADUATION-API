package ca.bc.gov.educ.api.graduation.model.report;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Objects;

public class TranscriptResult {

    private TranscriptCourse course;
    private Mark mark;
    private String requirement;
    private String requirementName;
    private String equivalency;
    private String usedForGrad;

    @JsonDeserialize(as = TranscriptCourse.class)
    public TranscriptCourse getCourse() {
        return course;
    }

    public void setCourse(TranscriptCourse value) {
        this.course = value;
    }

    public Mark getMark() {
        return mark;
    }

    public void setMark(Mark value) {
        this.mark = value;
    }

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String value) {
        this.requirement = value;
    }

    public String getRequirementName() {
        return requirementName;
    }

    public void setRequirementName(String value) {
        this.requirementName = value;
    }

    public String getEquivalency() {
        return equivalency;
    }

    public void setEquivalency(String value) {
        this.equivalency = value;
    }

    public String getUsedForGrad() {
        return usedForGrad;
    }

    public void setUsedForGrad(String value) {
        this.usedForGrad = value;
    }

    public Double getCompletedPercentage() {
        return this.mark.getCompletedCoursePercentage();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TranscriptResult that = (TranscriptResult) o;
        return course.equals(that.course);
    }

    @Override
    public int hashCode() {
        return Objects.hash(course);
    }
}
