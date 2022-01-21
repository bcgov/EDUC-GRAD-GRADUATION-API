package ca.bc.gov.educ.api.graduation.model.report;

import java.util.List;

public class GradRequirement {
    private String code;
    private String description;
    private List<AchievementCourse> courseDetails;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<AchievementCourse> getCourseDetails() {
        return courseDetails;
    }

    public void setCourseDetails(List<AchievementCourse> courseDetails) {
        this.courseDetails = courseDetails;
    }
}
