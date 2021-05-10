package ca.bc.gov.educ.api.graduation.model.dto;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GraduationData {
    private GradSearchStudent gradStudent;
    private GradAlgorithmGraduationStatus gradStatus;
    private List<SpecialGradAlgorithmGraduationStatus> specialGradStatus;
    private School school;
    private StudentCourses studentCourses;    
    private StudentAssessments studentAssessments;    
    private StudentExams studentExams;
    private List<GradRequirement> nonGradReasons;
    private List<GradRequirement> requirementsMet;
    //Grad Message
    //Student Career Programs
    private boolean isGraduated;
    
}
