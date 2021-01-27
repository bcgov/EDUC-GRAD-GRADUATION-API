package ca.bc.gov.educ.api.graduation.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GraduationData {
    private GradStudent gradStudent;
    private GradAlgorithmGraduationStatus gradStatus;
    private School school;
    private StudentCourses studentCourses;
    private StudentAssessments studentAssessments;
    private StudentExams studentExams;
    private List<String> nonGradReasons;
    private List<String> requirementsMet;
    //Grad Message
    //Student Career Programs
    private boolean isGraduated;
}
