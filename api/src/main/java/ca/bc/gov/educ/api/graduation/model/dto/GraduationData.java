package ca.bc.gov.educ.api.graduation.model.dto;

import java.util.List;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

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
    //Student Assessments
    //Student Exams
    private List<String> nonGradReasons;
    private List<String> requirementsMet;
    //Grad Message
    //Student Career Programs
    private boolean isGraduated;
}
