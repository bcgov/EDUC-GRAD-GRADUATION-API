package ca.bc.gov.educ.api.graduation.model.dto;

import ca.bc.gov.educ.api.graduation.model.StudentCareerProgram;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class GraduationStudentRecord extends BaseModel{

    private String studentGradData;
    private String studentProjectedGradData;
    private String pen;
    private String program;
    private String programName;
    private String programCompletionDate;
    private String gpa;
    private String honoursStanding;
    private String recalculateGradStatus;   
    private String schoolOfRecord;
    private String schoolName;
    private String studentGrade;	
    private String studentStatus;
    private String studentStatusName;
    private UUID studentID;
    private String schoolAtGrad;
    private String schoolAtGradName;
    private String legalFirstName;
    private String legalMiddleNames;
    private String legalLastName;
    private String consumerEducationRequirementMet;
    private String studentCitizenship;
	private ExceptionMessage exception;
    private Date adultStartDate;

    private List<GradRequirement> nonGradReasons;
    private List<StudentCareerProgram> careerPrograms;
}
