package ca.bc.gov.educ.api.graduation.util;

import ca.bc.gov.educ.api.graduation.model.dto.StudentCourse;

import java.util.Comparator;

public class BestSchoolPercentageComparator implements Comparator<StudentCourse> {

    @Override
    public int compare(final StudentCourse e1, final StudentCourse e2) {
        if (e1.getBestSchoolPercent() == null && e2.getBestSchoolPercent() == null) {
            return 0;
        } else if(e1.getBestSchoolPercent() == null) {
            return -1;
        } else if(e2.getBestSchoolPercent() == null) {
            return 1;
        } else {
            return e1.getBestSchoolPercent().compareTo(e2.getBestSchoolPercent());
        }
    }
}