package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.constants.AddressTypeCodes;
import ca.bc.gov.educ.api.graduation.constants.DistrictContactTypeCodes;
import ca.bc.gov.educ.api.graduation.model.dto.institute.District;
import ca.bc.gov.educ.api.graduation.model.dto.GradCertificateType;
import ca.bc.gov.educ.api.graduation.model.dto.ReportGradStudentData;
import ca.bc.gov.educ.api.graduation.model.dto.institute.DistrictAddress;
import ca.bc.gov.educ.api.graduation.model.dto.institute.DistrictContact;
import ca.bc.gov.educ.api.graduation.model.dto.institute.School;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class BaseServiceTest {

  public List<ReportGradStudentData> createStudentSchoolYearEndData() {
    List<ReportGradStudentData> reportGradStudentData = new ArrayList<>();
    UUID school1 = UUID.randomUUID();
    UUID school2 = UUID.randomUUID();
    reportGradStudentData.add(ReportGradStudentData.builder()
        .graduationStudentRecordId(UUID.randomUUID())
        .mincode("02396738")
        .mincodeAtGrad("12345678")
        .schoolOfRecordId(school1.toString())
        .schoolAtGradId(UUID.randomUUID().toString())
        .pen("118701325")
        .firstName("ROBERT")
        .middleName("LYNNEA")
        .lastName("BERGERON")
        .districtName("CENTRAL OKANAGAN PUBLIC SCHOOLS SD 23")
        .schoolName("HERITAGE CHRISTIAN ONLINE SCHOOL")
        .programCode("SCCP")
        .programName("Student Completion Certificate Program")
        .programCompletionDate("2012/02")
        .graduated("true")
        .transcriptTypeCode("SCCP-EN")
        .certificateTypeCode("SCI")
        .certificateTypes(List.of(new GradCertificateType("SCI", null, "Evergreen (Independent)")))
        .build());
    reportGradStudentData.add(ReportGradStudentData.builder()
        .graduationStudentRecordId(UUID.randomUUID())
        .mincode("02396738")
        .mincodeAtGrad("02396738")
        .schoolOfRecordId(school1.toString())
        .schoolAtGradId(school1.toString())
        .pen("128320223")
        .firstName("MARVIN")
        .middleName("RICHARD")
        .lastName("SMITH")
        .districtName("CENTRAL OKANAGAN PUBLIC SCHOOLS SD 23")
        .schoolName("HERITAGE CHRISTIAN ONLINE SCHOOL")
        .programCode("2018-EN")
        .programName("Graduation Program 2018")
        .programCompletionDate("2020-06-01")
        .graduated("true")
        .transcriptTypeCode("BC2018-IND")
        .certificateTypeCode("EI")
        .certificateTypes(List.of(createGradCertificateType("EI", null, "Dogwood (Independent)")))
        .build());
    reportGradStudentData.add(ReportGradStudentData.builder()
        .graduationStudentRecordId(UUID.randomUUID())
        .mincode("00501007")
        .schoolOfRecordId(UUID.randomUUID().toString())
        .pen("133802710")
        .firstName("EVA")
        .middleName("DOUGLAS")
        .lastName("TOOR")
        .districtName("SOUTHEAST KOOTENAY")
        .schoolName("JAFFRAY ELEM-JR SECONDARY")
        .programCode("SCCP")
        .programName("Student Completion Certificate Program")
        .programCompletionDate("2010/03")
        .graduated("true")
        .transcriptTypeCode("SCCP-EN")
        .certificateTypeCode("SC")
        .certificateTypes(List.of(createGradCertificateType("SC", null, "Evergreen")))
        .build());
    reportGradStudentData.add(ReportGradStudentData.builder()
        .graduationStudentRecordId(UUID.randomUUID())
        .mincode("00502001")
        .schoolOfRecordId(school2.toString())
        .pen("130521487")
        .firstName("YUN HSUAN")
        .middleName("PETER")
        .lastName("ABBOTT")
        .districtName("SOUTHEAST KOOTENAY")
        .schoolName("ROBERT EDGELL")
        .programCode("SCCP")
        .programName("Student Completion Certificate Program")
        .programCompletionDate("2022/06")
        .graduated("true")
        .transcriptTypeCode("SCCP-EN")
        .certificateTypes(null)
        .build());
    reportGradStudentData.add(ReportGradStudentData.builder()
        .graduationStudentRecordId(UUID.randomUUID())
        .mincode("00502001")
        .schoolOfRecordId(school2.toString())
        .pen("128329158")
        .firstName("DYLAN")
        .middleName("MICHAEL")
        .lastName("FISHER")
        .districtName("SOUTHEAST KOOTENAY")
        .schoolName("ROBERT EDGELL")
        .programCode("2018-EN")
        .programName("Graduation Program 2018")
        .programCompletionDate("2022/01")
        .graduated("true")
        .transcriptTypeCode("BC2018-PUB")
        .certificateTypes(null)
        .build());
    reportGradStudentData.add(ReportGradStudentData.builder()
        .graduationStudentRecordId(UUID.randomUUID())
        .mincode("00502001")
        .schoolOfRecordId(school2.toString())
        .pen("130490329")
        .firstName("LES")
        .middleName("HENRY")
        .lastName("OGLEND")
        .districtName("SOUTHEAST KOOTENAY")
        .schoolName("ROBERT EDGELL")
        .programCode("SCCP")
        .programName("Student Completion Certificate Program")
        .programCompletionDate("2022/06")
        .graduated("true")
        .transcriptTypeCode("SCCP-EN")
        .certificateTypes(null)
        .build());
    reportGradStudentData.add(ReportGradStudentData.builder()
        .graduationStudentRecordId(UUID.randomUUID())
        .mincode("07897034")
        .schoolOfRecordId(UUID.randomUUID().toString())
        .pen("124781733")
        .firstName("BRIAN")
        .middleName("LEIGH")
        .lastName("OVERTON")
        .districtName("FRASER-CASCADE")
        .schoolName("STS'AILES COMMUNITY SCHOOL")
        .programCode("2018-EN")
        .programName("Graduation Program 2018")
        .programCompletionDate("2020-06-01")
        .graduated("true")
        .transcriptTypeCode("BC2018-PUB")
        .certificateTypeCode("FN")
        .certificateTypes(List.of(createGradCertificateType("FN", null, "First Nations")))
        .build());

    return reportGradStudentData;
  }

  public GradCertificateType createGradCertificateType(String code, String label, String description) {
    return GradCertificateType.builder()
        .code(code)
        .label(label)
        .description(description)
        .build();
  }

  public School createSchool(UUID districtId, UUID schoolId, String mincode) {
    return School.builder()
        .districtId(districtId.toString())
        .schoolId(schoolId.toString())
        .mincode(mincode)
        .displayName("Generic School Name")
        .build();
  }

  public District createDistrict() {
    return District.builder()
        .districtId(UUID.randomUUID().toString())
        .displayName("Generic District Name")
        .districtNumber("005")
        .build();
  }

  public DistrictContact createDistrictContact(UUID districtId) {
    return DistrictContact.builder()
        .districtId(districtId.toString())
        .districtContactId(UUID.randomUUID().toString())
        .districtContactTypeCode(DistrictContactTypeCodes.SUPER.getCode())
        .firstName("Super")
        .lastName("Man")
        .build();
  }

  public DistrictAddress createDistrictAddress(UUID districtId) {
    return DistrictAddress.builder()
        .districtId(districtId.toString())
        .districtAddressId(UUID.randomUUID().toString())
        .addressTypeCode(AddressTypeCodes.MAILING.getCode())
        .addressLine1("1234 Generic Street")
        .city("Generic City")
        .postal("V1V1V1")
        .build();
  }
}
