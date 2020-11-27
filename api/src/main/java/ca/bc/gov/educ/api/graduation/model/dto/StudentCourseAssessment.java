package ca.bc.gov.educ.api.graduation.model.dto;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class StudentCourseAssessment {

	private String name;
	private String code;
	private String requirementMetCode;
	private String equivalentCourseType;
	private String sessionDate;
	private String finalPercentageScore;
	private String finalLetterGrade;
	private String interimPercentageScore;
	private String interimLetterGrade;
	private String creditsEarned;
	private String creditsUsedForGrad;
	private String schoolPercentage;
	private String examPercentage;
	private String type;//possible values COURSE,ASSESSMENT,EXAM	
}
