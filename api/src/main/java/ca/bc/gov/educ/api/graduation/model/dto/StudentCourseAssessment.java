package ca.bc.gov.educ.api.graduation.model.dto;

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
public class StudentCourseAssessment {

	private String pen;
    private String courseCode;
    private String courseName;
    private String courseLevel;
    private String sessionDate;
	private String courseDescription;
	private String gradReqMet;
	private Double finalPercentage;
	private String finalLetterGrade;	
	private Integer credits;
	private boolean isNotCompleted;
	private boolean isFailed;
	private boolean isDuplicate;
	private boolean isUsed;

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
		return "StudentCourseAssessment [pen=" + pen + ", courseCode=" + courseCode + ", courseName=" + courseName
				+ ", courseLevel=" + courseLevel + ", sessionDate=" + sessionDate + ", courseDescription="
				+ courseDescription + ", gradReqMet=" + gradReqMet + ", finalPercentage=" + finalPercentage
				+ ", finalLetterGrade=" + finalLetterGrade + ", credits=" + credits + ", isNotCompleted="
				+ isNotCompleted + ", isFailed=" + isFailed + ", isDuplicate=" + isDuplicate + ", isUsed=" + isUsed
				+ "]";
	}
}
