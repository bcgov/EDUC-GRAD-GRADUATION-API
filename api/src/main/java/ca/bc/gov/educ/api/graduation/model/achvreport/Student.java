package ca.bc.gov.educ.api.graduation.model.achvreport;

import ca.bc.gov.educ.api.graduation.util.EducGraduationApiUtils;
import lombok.Data;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Data
public class Student {
    private String pen;
    private String firstName;
    private String middleName;
    private String lastName;
    private String grade;
    private String gender;
    private Date birthdate;
    private String localId;
    private String program;
    private List<OtherProgram> otherProgramParticipation;

    public void setBirthdate(String birthdate) {
        try {
            this.birthdate = EducGraduationApiUtils.parseDate(birthdate, "yyyy-MM-dd");
        }catch (ParseException e) {
            e.printStackTrace();
        }
    }
    private String hasOtherProgram;
}
