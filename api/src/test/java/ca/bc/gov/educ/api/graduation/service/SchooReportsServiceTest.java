package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.model.dto.institute.District;
import ca.bc.gov.educ.api.graduation.model.dto.institute.YearEndReportRequest;
import ca.bc.gov.educ.api.graduation.model.report.School;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SchooReportsServiceTest extends BaseServiceTest {

	private static final String ADDRESS_LABEL_YE = "ADDRESS_LABEL_YE";
	private static final String DISTREP_YE_SD = "DISTREP_YE_SD";
	private static final String DISTREP_YE_SC = "DISTREP_YE_SC";
	private static final String ADDRESS_LABEL_SCHL = "ADDRESS_LABEL_SCHL";
	private static final String ADDRESS_LABEL_PSI = "ADDRESS_LABEL_PSI";
	private static final String DISTREP_SD = "DISTREP_SD";
	private static final String DISTREP_SC = "DISTREP_SC";

	@InjectMocks
	private SchoolReportsService schoolReportsService;

	@Mock
	private ReportService reportService;

	@Mock
	private RESTService restService;

	@Mock
	private SchoolService schoolService;

	@Mock
	private DistrictReportService districtReportService;

	@Mock
	private EducGraduationApiConstants constants;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testCreateAndStoreSchoolReports() throws Exception {
		List<ReportGradStudentData> gradStudentDataList = createStudentSchoolYearEndData();
		Mockito.when(reportService.getStudentsForSchoolYearEndReport()).thenReturn(gradStudentDataList);
		Mockito.when(reportService.getStudentsForSchoolReport()).thenReturn(gradStudentDataList);
		byte[] bytesSAR1 = readBinaryFile("data/sample.pdf");
		Mockito.when(restService.post(any(), any(), any())).thenReturn(bytesSAR1);
		ca.bc.gov.educ.api.graduation.model.dto.SchoolClob schoolClob = new ca.bc.gov.educ.api.graduation.model.dto.SchoolClob();
		schoolClob.setMinCode("12345678");
		schoolClob.setAddress1("1231");
		when(this.schoolService.getSchoolClob(any(String.class))).thenReturn(schoolClob);

		Integer reportsCount = schoolReportsService.createAndStoreSchoolReports(DISTREP_YE_SC);
		Mockito.verify(reportService, Mockito.times(1)).getStudentsForSchoolYearEndReport();
		assertEquals(4, reportsCount);

		reportsCount = schoolReportsService.createAndStoreSchoolReports(DISTREP_SC);
		Mockito.verify(reportService, Mockito.times(1)).getStudentsForSchoolReport();
		assertEquals(4, reportsCount);
	}

	@Test
	void testCreateAndStoreSchoolDistrictYearEndReports() throws Exception {
		List<ReportGradStudentData> gradStudentDataList = createStudentSchoolYearEndData();
		Mockito.when(reportService.getStudentsForSchoolYearEndReport()).thenReturn(gradStudentDataList);
		Mockito.when(reportService.getStudentsForSchoolYearEndReport(any())).thenReturn(gradStudentDataList);
		Mockito.when(reportService.getStudentsForSchoolReport()).thenReturn(gradStudentDataList);

		byte[] bytesSAR1 = readBinaryFile("data/sample.pdf");
		Mockito.when(restService.post(any(), any(), any())).thenReturn(bytesSAR1);

		District disttrax = new District();
		disttrax.setDistrictNumber("005");
		disttrax.setDisplayName("My District");
		disttrax.setDistrictId(String.valueOf(UUID.randomUUID()));

		ca.bc.gov.educ.api.graduation.model.dto.institute.School traxSchool = ca.bc.gov.educ.api.graduation.model.dto.institute.School.builder().mincode("12345678").schoolId(UUID.randomUUID().toString()).districtId(disttrax.getDistrictId()).build();

		ca.bc.gov.educ.api.graduation.model.dto.SchoolClob schoolClob = new ca.bc.gov.educ.api.graduation.model.dto.SchoolClob();
		schoolClob.setMinCode("12345678");
		schoolClob.setAddress1("1231");

		when(this.restService.get(any(String.class), any(), any())).thenReturn(traxSchool);
		when(this.schoolService.getSchoolClob(any(String.class))).thenReturn(schoolClob);
		when(this.schoolService.getSchoolById(any())).thenReturn(traxSchool);
		when(this.districtReportService.createAndStoreDistrictReports(any(), any(), any())).thenReturn(1);

		Integer reportsCount = schoolReportsService.createAndStoreSchoolDistrictYearEndReports(ADDRESS_LABEL_YE, DISTREP_YE_SD, DISTREP_YE_SC);
		assertTrue(reportsCount > 0);
	}

	@Test
	void testCreateAndStoreSchoolDistrictYearEndReportsWithRequest() throws Exception {
		YearEndReportRequest yearEndReportRequest = YearEndReportRequest.builder().schoolIds(List.of(UUID.randomUUID())).districtIds(List.of(UUID.randomUUID())).build();
		byte[] bytesSAR1 = readBinaryFile("data/sample.pdf");
		Mockito.when(restService.post(any(), any(), any())).thenReturn(bytesSAR1);

		Integer reportsCount = schoolReportsService.createAndStoreSchoolDistrictYearEndReports(ADDRESS_LABEL_YE, DISTREP_YE_SD, DISTREP_YE_SC, yearEndReportRequest);
		assertTrue(reportsCount > 0);
	}

	@Test
	void testCreateAndStoreSchoolDistrictReports() throws Exception {
		List<ReportGradStudentData> gradStudentDataList = createStudentSchoolYearEndData();
		Mockito.when(reportService.getStudentsForSchoolYearEndReport()).thenReturn(gradStudentDataList);
		Mockito.when(reportService.getStudentsForSchoolYearEndReport(any())).thenReturn(gradStudentDataList);
		Mockito.when(reportService.getStudentsForSchoolReport()).thenReturn(gradStudentDataList);
		byte[] bytesSAR1 = readBinaryFile("data/sample.pdf");
		Mockito.when(restService.post(any(), any(), any())).thenReturn(bytesSAR1);
		ca.bc.gov.educ.api.graduation.model.dto.SchoolClob schoolClob = new ca.bc.gov.educ.api.graduation.model.dto.SchoolClob();
		schoolClob.setMinCode("12345678");
		schoolClob.setAddress1("1231");
		when(this.schoolService.getSchoolClob(any(String.class))).thenReturn(schoolClob);

		Integer reportsCount = schoolReportsService.createAndStoreSchoolDistrictReports(ADDRESS_LABEL_SCHL, DISTREP_SD, DISTREP_SC);
		assertTrue(reportsCount > 0);

		reportsCount = schoolReportsService.createAndStoreSchoolDistrictReports(gradStudentDataList, ADDRESS_LABEL_SCHL, DISTREP_SD, DISTREP_SC);
		assertTrue(reportsCount > 0);
	}

	@Test
	void testCreateAndStoreSchoolLabelsReportsFromSchools() throws Exception {
		School school = new School();
		school.setMincode("005994567");
		school.setName("Test School Name");
		school.setSchoolId(UUID.randomUUID().toString());
		byte[] bytesSAR1 = readBinaryFile("data/sample.pdf");
		Mockito.when(restService.post(any(), any(), any())).thenReturn(bytesSAR1);

		Integer reportsCount = schoolReportsService.createAndStoreSchoolLabelsReportsFromSchools(ADDRESS_LABEL_PSI, List.of(school), null);
		assertTrue(reportsCount > 0);
	}

	@Test
	void testGetSchoolDistrictYearEndReports() throws IOException {
		byte[] result = schoolReportsService.getSchoolDistrictYearEndReports(ADDRESS_LABEL_YE, DISTREP_YE_SD, DISTREP_YE_SC);
		assertNotNull(result);
	}

	@Test
	void testGetSchoolDistrictReports() throws IOException {
		byte[] result = schoolReportsService.getSchoolDistrictReports(ADDRESS_LABEL_SCHL, DISTREP_SD, DISTREP_SC);
		assertNotNull(result);
	}

	@Test
	void testGetSchoolYearEndReports() throws IOException {
		byte[] result = schoolReportsService.getSchoolYearEndReports();
		assertNotNull(result);
	}

	@Test
	void testGetSchoolReports() throws IOException {
		byte[] result = schoolReportsService.getSchoolReports();
		assertNotNull(result);
	}

	@Test
	void testGetSchoolLabelsReportsFromSchools() throws IOException {
		School school = new School();
		school.setMincode("005994567");
		school.setName("Test School Name");
		school.setSchoolId(UUID.randomUUID().toString());

		byte[] result = schoolReportsService.getSchoolLabelsReportsFromSchools("ADDRESS_LABEL_PSI", List.of(school));
		assertNotNull(result);
	}

	private byte[] readBinaryFile(String path) throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(path);
		return inputStream.readAllBytes();
	}
}

