package ca.bc.gov.educ.api.graduation.model.dto;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Component
@AllArgsConstructor
@Builder
public class GraduationData {
    private GradStudent gradStudent;
    private GradAlgorithmGraduationStatus gradStatus;
    private School school;
    private StudentCourses studentCourses;
    //Student Assessments
    //Student Exams
    List<String> nonGradReasons;
    List<String> requirementsMet;
    //Grad Message
    //Student Career Programs
    boolean isGraduated;
}
