package ca.bc.gov.educ.api.graduation.model.dto;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class StudentDemographics {
	private String schoolName;
	private String minCode;
	private String pen;
	private String studentLocalId;
	private String studSurname;
    private String studGiven;
    private String studMiddle;	
    private String address1;
    private String address2;
    private String city;
    private String provinceCode;
    private String countryCode;
    private String postalCode;
    private String studBirth;
    private String studSex;
    private String studentGrade;
    private String studentCitizenship;
    private int gradRequirementYear;
    private String programCode;
    private String programCode2;
    private String programCode3;
    private String programCode4;
    private String programCode5;
}
