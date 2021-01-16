package ca.bc.gov.educ.api.graduation.model.dto;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class StudentExams {

	private String courseCode = "COM";
	private String courseName = "COMMUNICATIONS 12";
	private String sessionDate= "1987/06";
	private String gradReqMet=null;
	private String courseLevel = "12 ";
	private String courseType=null;
	private String completedCourseSchoolPercentag="66.0";
	private String completedCourseExamPercentage="76.0";
	private String completedCourseFinalPercentage="71.0";
	private String completedCourseLetterGrade="C+";
	private String interimPercent=null;
	private String interimLetterGrade=null;
	private String credits="4";
	private String creditsUsedForGrad=null;	
}
