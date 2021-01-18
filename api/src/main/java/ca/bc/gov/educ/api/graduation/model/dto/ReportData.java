package ca.bc.gov.educ.api.graduation.model.dto;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class ReportData {

	private StudentDemographics demographics;
	private List<StudentCourse> studentCourse;
	private List<StudentAssessments> studentAssessment;
	private List<StudentExams> studentExam;
	private GraduationMessages graduationMessages;
	
}
