package ca.bc.gov.educ.api.graduation.model.dto;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class GradAlgorithmGraduationStatus {

	private String pen;
    private String program;
    private String programCompletionDate;
    private String gpa;
    private String honoursFlag;
    private String certificateType1;
    private String certificateType2;
    private String certificateType1Date; 
    private String certificateType2Date; 
    private String recalculateFlag;   
    private String schoolOfRecord;
    private String studentGrade;
}
