package ca.bc.gov.educ.api.graduation.model.dto;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class ReportData {

	private String isaDate;
	private String transcriptBanner;
	private String issueDate;
	private StudentDemographics demographics;
	private List<StudentCourse> studentCourse;
	private List<StudentAssessment> studentAssessment;
	private List<StudentExam> studentExam;
	private GraduationMessages graduationMessages;
	private List<StudentCourseAssessment> studentCourse1;
	private List<StudentCourseAssessment> studentCourse2;
	private List<StudentCourseAssessment> studentCourse3;
	private List<StudentCourseAssessment> studentCourse4;
	private List<StudentCourseAssessment> studentCourse5;
	private List<StudentCourseAssessment> studentCourse6;
	
}
