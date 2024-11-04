package ca.bc.gov.educ.api.graduation.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants.DEFAULT_DATE_FORMAT;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
@JsonIgnoreProperties(ignoreUnknown = true)
public class GraduationStudentRecord extends BaseModel {

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
    private UUID schoolOfRecordId;
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
    @JsonFormat(pattern=DEFAULT_DATE_FORMAT)
    private LocalDate adultStartDate;

    private List<GradRequirement> nonGradReasons;
    private List<StudentCareerProgram> careerPrograms;
}
