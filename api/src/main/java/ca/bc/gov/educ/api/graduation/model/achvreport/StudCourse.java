package ca.bc.gov.educ.api.graduation.model.achvreport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudCourse {

    private String courseCode;
    private String courseName;
    private String courseLevel;
    private String sessionDate;
    private String gradReqMet;
    private String completedCoursePercentage;
    private String completedCourseLetterGrade;
    private String interimPercent;
    private String equivOrChallenge;
    private String credits;
    private Integer creditsUsedForGrad;

    public Integer getCreditsUsedForGrad() {
        if (creditsUsedForGrad == null)
            return 0;
        else
            return creditsUsedForGrad;
    }

    public String getCourseCode() {
        if (courseCode != null)
            courseCode = courseCode.trim();
        return courseCode;
    }

    public String getCourseLevel() {
        if (courseLevel != null)
            courseLevel = courseLevel.trim();
        return courseLevel;
    }

    @Override
    public String toString() {
        return "StudentCourse{" +
                "courseCode='" + courseCode + '\'' +
                ", courseName='" + courseName + '\'' +
                ", courseLevel='" + courseLevel + '\'' +
                ", sessionDate='" + sessionDate + '\'' +
                ", gradReqMet='" + gradReqMet + '\'' +
                ", completedCoursePercentage=" + completedCoursePercentage +
                ", completedCourseLetterGrade='" + completedCourseLetterGrade + '\'' +
                ", interimPercent=" + interimPercent +
                ", equivOrChallenge='" + equivOrChallenge + '\'' +
                ", credits=" + credits +
                ", creditsUsedForGrad=" + creditsUsedForGrad +
                '}';
    }
}
