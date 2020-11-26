package ca.bc.gov.educ.api.graduation.model.dto;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class GradStudent {
    private String pen;
    private String archiveFlag;
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
    private String studentCitizenship;
    private String studentGrade;
    private String mincode;
    private String studentLocalId;
    private String studTrueNo;
    private String studSin;
    private String programCode;
    private String programCode2;
    private String programCode3;
    private String studPsiPermit;
    private String studResearchPermit;
    private String studStatus;
    private String studConsedFlag;
    private String yrEnter11;
    private String gradDate;
    private String dogwoodFlag;
    private String honourFlag;
    private String mincode_grad;
    private String frenchDogwood;
    private String programCode4;
    private String programCode5;
    private String sccDate;
    private int gradRequirementYear;
    private String slpDate;
    private String mergedFromPen;
    private String gradReqtYearAtGrad;
    private String studGradeAtGrad;
    private String xcriptActvDate;
    private String allowedAdult;
    private String ssaNominationDate;
    private String adjTestYear;
    private String graduatedAdult;
    private String supplierNo;
    private String siteNo;
    private String emailAddress;
    private String englishCert;
    private String frenchCert;
    private String englishCertDate;
    private String frenchCertDate;

    private String schoolName;
    private String countryName;
    private String provinceName;
}
