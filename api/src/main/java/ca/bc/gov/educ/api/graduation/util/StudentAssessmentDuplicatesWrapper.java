package ca.bc.gov.educ.api.graduation.util;

import ca.bc.gov.educ.api.graduation.model.dto.StudentAssessment;

import java.util.Objects;

public class StudentAssessmentDuplicatesWrapper {

    private StudentAssessment studentAssessment;

    public StudentAssessmentDuplicatesWrapper(StudentAssessment studentAssessment) {
        this.studentAssessment = studentAssessment;
    }

    public StudentAssessment getStudentAssessment() {
        return this.studentAssessment;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StudentAssessmentDuplicatesWrapper other = (StudentAssessmentDuplicatesWrapper) obj;
        if(!studentAssessment.isUsed() && !studentAssessment.isProjected()) {
            return true;
        }
        return Objects.equals(studentAssessment.getAssessmentCode(), other.studentAssessment.getAssessmentCode())
                && (
                        Objects.equals(studentAssessment.isUsed(), other.studentAssessment.isProjected())
                        || Objects.equals(studentAssessment.isProjected(), other.studentAssessment.isUsed())
                )
               ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentAssessment.getAssessmentCode());
    }

}
