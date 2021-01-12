package ca.bc.gov.educ.api.graduation.model.dto;

import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class GraduationStatus extends BaseModel{

	public GraduationStatus() {
        studentGradData = new StringBuffer();
    }

    private StringBuffer studentGradData;
    private String pen;
    private String gradProgram;
    private String graduationDate; 
    private String sccpGraduationDate;
    private String studentGradeAtGraduation;
    private String gpa;
    private String honoursFlag;
    private String certificateType1;
    private String certificateType2;
    private String certificateType1Date; 
    private String certificateType2Date; 
    private String frenchProgramParticipation;
    private String advancePlacementParticipation;    
    private String careerProgramParticipation;
    private String recalculateFlag;
    private String ibParticipationFlag;    
    private String transcriptDate;  
    private String schoolOfRecord;
    private String studentGrade;	
				
}
