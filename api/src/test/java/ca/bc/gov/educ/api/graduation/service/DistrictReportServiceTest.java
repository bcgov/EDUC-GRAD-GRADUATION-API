package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.constants.ReportTypeCodes;
import ca.bc.gov.educ.api.graduation.model.dto.institute.District;
import ca.bc.gov.educ.api.graduation.model.dto.ReportGradStudentData;
import ca.bc.gov.educ.api.graduation.model.dto.institute.School;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DistrictReportServiceTest extends BaseServiceTest {

  @InjectMocks
  private DistrictReportService districtReportService;

  @Mock
  private EducGraduationApiConstants constants;

  @Mock
  private RESTService restService;

  @Mock
  private ReportService reportService;

  @Mock
  private SchoolService schoolService;

  @Mock
  private DistrictService districtService;

  @MockBean(name = "graduationApiClient")
  @Qualifier("graduationApiClient")
  WebClient graduationApiClient;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void createAndStoreDistrictYearEndReports() {
    List<ReportGradStudentData> studentData = createStudentSchoolYearEndData();
    District district = createDistrict();
    when(reportService.getStudentsForSchoolYearEndReport()).thenReturn(studentData);
    studentData.forEach(student -> {
          UUID schoolId = student.getSchoolAtGradId() == null ? UUID.fromString(student.getSchoolOfRecordId()) : UUID.fromString(student.getSchoolAtGradId());
          School school = createSchool(UUID.fromString(district.getDistrictId()), schoolId, student.getMincode());
          when(schoolService.getSchoolById(schoolId)).thenReturn(school);
        }
    );
    when(districtService.getDistrictDetails(any(UUID.class))).thenReturn(district);
    when(restService.post(any(), any(), any(), any())).thenReturn(new byte[0]);

    int result = districtReportService.createAndStoreDistrictYearEndReports();
    assertEquals(1, result);
  }

  @Test
  void createAndStoreDistrictReportMonth() {
    List<ReportGradStudentData> studentData = createStudentSchoolYearEndData();
    District district = createDistrict();
    when(reportService.getStudentsForSchoolReport()).thenReturn(studentData);
    studentData.forEach(student -> {
          UUID schoolId = student.getSchoolAtGradId() == null ? UUID.fromString(student.getSchoolOfRecordId()) : UUID.fromString(student.getSchoolAtGradId());
          School school = createSchool(UUID.fromString(district.getDistrictId()), schoolId, student.getMincode());
          when(schoolService.getSchoolById(schoolId)).thenReturn(school);
        }
    );
    when(districtService.getDistrictDetails(any(UUID.class))).thenReturn(district);
    when(restService.post(any(), any(), any(), any())).thenReturn(new byte[0]);

    int result = districtReportService.createAndStoreDistrictReportMonth();
    assertEquals(1, result);
  }

  @Test
  void createAndStoreDistrictNonGradYearEndReport() {
    List<ReportGradStudentData> studentData = createStudentSchoolYearEndData();
    District district = createDistrict();
    when(reportService.getStudentsForSchoolNonGradYearEndReport()).thenReturn(studentData);
    studentData.forEach(student -> {
          UUID schoolId = student.getSchoolAtGradId() == null ? UUID.fromString(student.getSchoolOfRecordId()) : UUID.fromString(student.getSchoolAtGradId());
          School school = createSchool(UUID.fromString(district.getDistrictId()), schoolId, student.getMincode());
          when(schoolService.getSchoolById(schoolId)).thenReturn(school);
        }
    );
    when(districtService.getDistrictDetails(any(UUID.class))).thenReturn(district);
    when(restService.post(any(), any(), any(), any())).thenReturn(new byte[0]);

    int result = districtReportService.createAndStoreDistrictNonGradYearEndReport();
    assertEquals(1, result);
  }

  @Test
  void createAndStoreDistrictLabelsReportsFromDistricts() {
    District district = createDistrict();
    district.setContacts(List.of(createDistrictContact(UUID.fromString(district.getDistrictId()))));
    district.setAddresses(List.of(createDistrictAddress(UUID.fromString(district.getDistrictId()))));
    when(restService.post(any(), any(), any(), any())).thenReturn(new byte[0]);

    int result = districtReportService.createAndStoreDistrictLabelsReportsFromDistricts(ReportTypeCodes.ADDRESS_LABEL_YE.getCode(), List.of(district), null);
    assertEquals(1, result);
  }

  @Test
  void createAndStoreDistrictSchoolLabelsReportsFromDistricts() throws IOException {
    District district = createDistrict();
    ca.bc.gov.educ.api.graduation.model.report.School school = new ca.bc.gov.educ.api.graduation.model.report.School();
    school.setMincode("005994567");
    school.setName("Test School Name");
    school.setSchoolId(UUID.randomUUID().toString());
    byte[] mockPdfBytes = Files.readAllBytes(Paths.get("src/test/resources/data/sample.pdf"));
    when(restService.post(any(), any(), any(), any())).thenReturn(mockPdfBytes);
    int result = districtReportService.createAndStoreDistrictLabelsReportsFromSchools(ReportTypeCodes.ADDRESS_LABEL_SCH_YE.getCode(), UUID.fromString(district.getDistrictId()), List.of(school));
    assertEquals(1, result);
  }

  @Test
  void getDistrictYearEndReports() throws IOException {
    List<ReportGradStudentData> studentData = createStudentSchoolYearEndData();
    District district = createDistrict();
    when(reportService.getStudentsForSchoolYearEndReport()).thenReturn(studentData);
    studentData.forEach(student -> {
          UUID schoolId = student.getSchoolAtGradId() == null ? UUID.fromString(student.getSchoolOfRecordId()) : UUID.fromString(student.getSchoolAtGradId());
          School school = createSchool(UUID.fromString(district.getDistrictId()), schoolId, student.getMincode());
          when(schoolService.getSchoolById(schoolId)).thenReturn(school);
        }
    );
    when(districtService.getDistrictDetails(any(UUID.class))).thenReturn(district);
    byte[] mockPdfBytes = Files.readAllBytes(Paths.get("src/test/resources/data/sample.pdf"));
    when(restService.post(any(), any(), any(), any())).thenReturn(mockPdfBytes);

    byte[] result = districtReportService.getDistrictYearEndReports();

    assertNotNull(result);
    verify(reportService, times(1)).getStudentsForSchoolYearEndReport();
    verify(restService, atLeastOnce()).post(any(), any(), any(), any());
  }

  @Test
  void getDistrictYearEndNonGradReports() throws IOException {
    List<ReportGradStudentData> studentData = createStudentSchoolYearEndData();
    District district = createDistrict();
    when(reportService.getStudentsForSchoolNonGradYearEndReport()).thenReturn(studentData);
    studentData.forEach(student -> {
          UUID schoolId = student.getSchoolAtGradId() == null ? UUID.fromString(student.getSchoolOfRecordId()) : UUID.fromString(student.getSchoolAtGradId());
          School school = createSchool(UUID.fromString(district.getDistrictId()), schoolId, student.getMincode());
          when(schoolService.getSchoolById(schoolId)).thenReturn(school);
        }
    );
    when(districtService.getDistrictDetails(any(UUID.class))).thenReturn(district);
    byte[] mockPdfBytes = Files.readAllBytes(Paths.get("src/test/resources/data/sample.pdf"));
    when(restService.post(any(), any(), any(), any())).thenReturn(mockPdfBytes);

    byte[] result = districtReportService.getDistrictYearEndNonGradReports();

    assertNotNull(result);
    verify(reportService, times(1)).getStudentsForSchoolNonGradYearEndReport();
    verify(restService, atLeastOnce()).post(any(), any(), any(), any());
  }

  @Test
  void getDistrictReports() throws IOException {
    List<ReportGradStudentData> studentData = createStudentSchoolYearEndData();
    District district = createDistrict();
    when(reportService.getStudentsForSchoolReport()).thenReturn(studentData);
    studentData.forEach(student -> {
          UUID schoolId = student.getSchoolAtGradId() == null ? UUID.fromString(student.getSchoolOfRecordId()) : UUID.fromString(student.getSchoolAtGradId());
          School school = createSchool(UUID.fromString(district.getDistrictId()), schoolId, student.getMincode());
          when(schoolService.getSchoolById(schoolId)).thenReturn(school);
        }
    );
    when(districtService.getDistrictDetails(any(UUID.class))).thenReturn(district);
    byte[] mockPdfBytes = Files.readAllBytes(Paths.get("src/test/resources/data/sample.pdf"));
    when(restService.post(any(), any(), any(), any())).thenReturn(mockPdfBytes);

    byte[] result = districtReportService.getDistrictReports();

    assertNotNull(result);
    verify(reportService, times(1)).getStudentsForSchoolReport();
    verify(restService, atLeastOnce()).post(any(), any(), any(), any());
  }
}
