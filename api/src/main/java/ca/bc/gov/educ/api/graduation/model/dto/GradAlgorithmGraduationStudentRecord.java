package ca.bc.gov.educ.api.graduation.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.UUID;

import static ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants.DEFAULT_DATE_FORMAT;

@Data
@Component
public class GradAlgorithmGraduationStudentRecord {

	private String studentGradData;
    private String pen;
    private String program;
    private String programName;
    private String programCompletionDate;
    private String gpa;
    private String honoursStanding;
    private String recalculateGradStatus;   
    private String schoolOfRecord;
    private String studentGrade;	
    private String studentStatus;
    private String studentCitizenship;
    private UUID studentID;
    private String schoolAtGrad;
    private String consumerEducationRequirementMet;
    private String schoolAtGradName;
    @JsonFormat(pattern=DEFAULT_DATE_FORMAT)
    private Date adultStartDate;
}
