package ca.bc.gov.educ.api.graduation.model.dto;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class GradAlgorithmOptionalStudentProgram {

	private String pen;
    private UUID optionalProgramID;
    private String studentSpecialProgramData;
    private String optionalProgramCompletionDate;
    private StudentCourses optionalStudentCourses;
    private StudentAssessments optionalStudentAssessments;
    private boolean isSpecialGraduated;
    private List<GradRequirement> optionalNonGradReasons;
    private List<GradRequirement> optionalRequirementsMet;
    private UUID studentID;
}
