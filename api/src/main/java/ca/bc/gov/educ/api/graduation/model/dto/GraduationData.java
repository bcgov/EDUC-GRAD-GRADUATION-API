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
    private GradSearchStudent gradStudent;
    private GradAlgorithmGraduationStudentRecord gradStatus;
    private List<GradAlgorithmOptionalStudentProgram> optionalGradStatus;
    private SchoolClob school;
    private StudentCourses studentCourses;    
    private StudentAssessments studentAssessments;    
    private StudentExams studentExams;
    private List<GradRequirement> nonGradReasons;
    private List<GradRequirement> requirementsMet;
    private StudentCertificatesTranscript studentCertificatesTranscript;
    private String gradMessage;
    //Student Career Programs
    private boolean dualDogwood;
    private boolean isGraduated;
    private ExceptionMessage exception;
    private GraduationProgramCode gradProgram;
    private String latestSessionDate;

    public StudentCertificatesTranscript getStudentCertificatesTranscript() {
        if(this.studentCertificatesTranscript == null) {
            this.studentCertificatesTranscript = new StudentCertificatesTranscript();
        }
        return this.studentCertificatesTranscript;
    }
}
