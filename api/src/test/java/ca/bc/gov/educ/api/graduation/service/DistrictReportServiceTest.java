package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.model.dto.institute.District;
import ca.bc.gov.educ.api.graduation.model.dto.ReportGradStudentData;
import ca.bc.gov.educ.api.graduation.model.dto.institute.School;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;

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

  @Mock
  private WebClient webClient;

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
    when(restService.post(any(), any(), any())).thenReturn(new byte[0]);

    int result = districtReportService.createAndStoreDistrictYearEndReports();

    assertEquals(1, result);
  }
}
