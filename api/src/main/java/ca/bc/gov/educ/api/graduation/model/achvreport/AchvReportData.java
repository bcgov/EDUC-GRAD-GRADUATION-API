package ca.bc.gov.educ.api.graduation.model.achvreport;

import lombok.Data;

import java.util.List;

@Data
public class AchvReportData {
    private List<StudExam> studentExams;
    private List<StudCourse> studentCourses;
    private List<StudAssessment> studentAssessments;
    private List<NonGraduationReason> nonGradReasons;
    private List<OptionalProgram> optionalPrograms;
    private School school;
    private Student student;
    private GradStatus graduationStatus;
    private String orgCode;

}
