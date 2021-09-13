package ca.bc.gov.educ.api.graduation.model.report;

public class TranscriptResult {
    private Course course;
    private Mark mark;
    private String requirement;
    private String requirementName;
    private String equivalency;
    private String usedForGrad;

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course value) {
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
}
