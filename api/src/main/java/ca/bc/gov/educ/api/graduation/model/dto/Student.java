package ca.bc.gov.educ.api.graduation.model.dto;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class Student {
	
    private String pen;
    private String school;
    private String firstName;
    private String middleName;
    private String lastName;
    private StudentAddress address;
    private String gender;
    private String studentLocalID;
    private String dateOfBirth;
    private String graduationProgram;
    private String grade;
    private String citizenship;
    private String careerProgramCodes;    
    
    private String graduationMessage;
    private List<StudentCourseAssessment> studentCourseAssessmentList;
    private List<String> messages;
    private List<String> requirementsMet;
    private List<String> requirementsNotMet;
    
    
}
