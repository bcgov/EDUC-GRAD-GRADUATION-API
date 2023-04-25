package ca.bc.gov.educ.api.graduation.model.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Data
@Component
public class GradAlgorithmOptionalStudentProgram {

	private String pen;
    private UUID optionalProgramID;
    private String optionalProgramCode;
    private String studentOptionalProgramData;
    private String optionalProgramCompletionDate;
    private StudentCourses optionalStudentCourses;
    private StudentAssessments optionalStudentAssessments;
    private boolean isOptionalGraduated;
    private List<GradRequirement> optionalNonGradReasons;
    private List<GradRequirement> optionalRequirementsMet;
    private UUID studentID;
    private List<StudentCareerProgram> cpList;
}
