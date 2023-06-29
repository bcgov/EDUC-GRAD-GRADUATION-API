package ca.bc.gov.educ.api.graduation.util;

import ca.bc.gov.educ.api.graduation.model.dto.StudentAssessment;

import java.util.Objects;

public class StudentAssessmentDuplicatesWrapper {

    private StudentAssessment studentAssessment;
    private boolean xml;

    public StudentAssessmentDuplicatesWrapper(StudentAssessment studentAssessment, boolean xml) {
        this.studentAssessment = studentAssessment;
        this.xml = xml;
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
        return Objects.equals(other.studentAssessment.getAssessmentCode(), studentAssessment.getAssessmentCode())
                && (
                        !xml && (
                            Objects.equals(studentAssessment.isUsed(), other.studentAssessment.isUsed())
                                    ||
                            Objects.equals(studentAssessment.isProjected(), other.studentAssessment.isProjected())
                        )
                )
               ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentAssessment.getAssessmentCode());
    }

}
