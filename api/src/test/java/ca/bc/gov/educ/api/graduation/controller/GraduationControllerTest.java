package ca.bc.gov.educ.api.graduation.controller;

import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.model.dto.institute.District;
import ca.bc.gov.educ.api.graduation.model.dto.institute.YearEndReportRequest;
import ca.bc.gov.educ.api.graduation.model.report.GradProgram;
import ca.bc.gov.educ.api.graduation.model.report.ReportData;
import ca.bc.gov.educ.api.graduation.model.report.School;
import ca.bc.gov.educ.api.graduation.service.DistrictReportService;
import ca.bc.gov.educ.api.graduation.service.GraduationService;
import ca.bc.gov.educ.api.graduation.service.ReportService;
import ca.bc.gov.educ.api.graduation.service.SchoolReportsService;
import ca.bc.gov.educ.api.graduation.util.GradValidation;
import ca.bc.gov.educ.api.graduation.util.MessageHelper;
import ca.bc.gov.educ.api.graduation.util.ResponseHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static ca.bc.gov.educ.api.graduation.constants.ReportTypeCodes.ADDRESS_LABEL_SCH_YE;
import static ca.bc.gov.educ.api.graduation.service.SchoolReportsService.*;


@ExtendWith(MockitoExtension.class)
class GraduationControllerTest {

	@Mock
	private GraduationService graduationService;

	@Mock
	private SchoolReportsService schoolReportsService;

	@Mock
	private DistrictReportService districtReportService;

	@Mock
	private ReportService reportService;
	
	@Mock
	ResponseHelper response;
	
	@InjectMocks
	private GraduationController graduationController;
	
	@Mock
	GradValidation validation;
	
	@Mock
	MessageHelper messagesHelper;
	
	@Mock
	SecurityContextHolder securityContextHolder;
	
	@Test
	void testGraduateStudentNew() {
		String studentID = new UUID(1, 1).toString();
		String projectedType = "REGFM";
		
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("A");
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<StudentOptionalProgram>();
		list.add(spgm);
		
		AlgorithmResponse alRes = new AlgorithmResponse();
		alRes.setGraduationStudentRecord(gradResponse);
		alRes.setStudentOptionalProgram(list);
		Mockito.when(graduationService.graduateStudent(studentID,null,projectedType)).thenReturn(alRes);
		graduationController.graduateStudentNew(studentID,projectedType,null);
		Mockito.verify(graduationService).graduateStudent(studentID,null,projectedType);
	}

	@Test
	void testReportDataByPen() {
		ReportData data = new ReportData();
		data.setGradProgram(new GradProgram());
		Mockito.when(graduationService.prepareReportData("12312312312","XML")).thenReturn(data);
		graduationController.reportDataByPen("12312312312","XML");
		Mockito.verify(graduationService).prepareReportData("12312312312","XML");
	}

	@Test
	void testReportTranscriptByPen() {
		byte[] bytesSAR = "Any String you want".getBytes();
		Mockito.when(graduationService.prepareTranscriptReport("12312312312","Interim", null)).thenReturn(bytesSAR);
		graduationController.reportTranscriptByPen("12312312312","Interim",null);
		Mockito.verify(graduationService).prepareTranscriptReport("12312312312","Interim",null);
	}

	@Test
	void testReportTranscriptByPen_empty() {
		byte[] bytesSAR = new byte[0];
		Mockito.when(graduationService.prepareTranscriptReport("12312312312","Interim",null)).thenReturn(bytesSAR);
		graduationController.reportTranscriptByPen("12312312312","Interim",null);
		Mockito.verify(graduationService).prepareTranscriptReport("12312312312","Interim",null);
	}

	@Test
	void testReportTranscriptByPen_null() {
		byte[] bytesSAR = null;
		Mockito.when(graduationService.prepareTranscriptReport("12312312312","Interim",null)).thenReturn(bytesSAR);
		graduationController.reportTranscriptByPen("12312312312","Interim",null);
		Mockito.verify(graduationService).prepareTranscriptReport("12312312312","Interim",null);
	}

	@Test
	void testReportDataFromGraduation() {
		GraduationData graduationData = new GraduationData();
		graduationData.setGradMessage("asdasd");
		GradSearchStudent gsr = new GradSearchStudent();
		gsr.setStudentID(UUID.randomUUID().toString());
		graduationData.setGradStudent(gsr);

		ReportData data = new ReportData();
		data.setGradProgram(new GradProgram());
		Mockito.when(graduationService.prepareReportData(graduationData,"XML")).thenReturn(data);
		graduationController.reportDataFromGraduation(graduationData,"XML");
		Mockito.verify(graduationService).prepareReportData(graduationData,"XML");
	}

	@Test
	void testCreateAndStoreSchoolReports() {
		UUID schoolId = UUID.randomUUID();
		Mockito.when(graduationService.createAndStoreSchoolReports(List.of(schoolId),"NONGRAD")).thenReturn(1);
		graduationController.createAndStoreSchoolReports(List.of(schoolId),"NONGRAD");
		Mockito.verify(graduationService).createAndStoreSchoolReports(List.of(schoolId),"NONGRAD");
	}

	@Test
	void testCreateAndStoreSchoolYearEndReports() {
		Mockito.when(schoolReportsService.createAndStoreSchoolReports(DISTREP_YE_SC)).thenReturn(1);
		graduationController.createAndStoreSchoolYearEndReports();
		Mockito.verify(schoolReportsService).createAndStoreSchoolReports(DISTREP_YE_SC);
	}

	@Test
	void testCreateAndStoreSchoolsReports() {
		Mockito.when(schoolReportsService.createAndStoreSchoolReports(DISTREP_SC)).thenReturn(1);
		graduationController.createAndStoreSchoolReports();
		Mockito.verify(schoolReportsService).createAndStoreSchoolReports(DISTREP_SC);
	}

	@Test
	void testCreateAndStoreDistrictYearEndReports() {
		Mockito.when(districtReportService.createAndStoreDistrictYearEndReports()).thenReturn(1);
		graduationController.createAndStoreDistrictYearEndReports();
		Mockito.verify(districtReportService).createAndStoreDistrictYearEndReports();
	}

	@Test
	void testCreateAndStoreDistrictYearEndNonGradReports() {
		Mockito.when(districtReportService.createAndStoreDistrictNonGradYearEndReport()).thenReturn(1);
		graduationController.createAndStoreDistrictYearEndNonGradReports();
		Mockito.verify(districtReportService).createAndStoreDistrictNonGradYearEndReport();
	}

	@Test
	void testCreateAndStoreDistrictReports() {
		Mockito.when(districtReportService.createAndStoreDistrictReportMonth()).thenReturn(1);
		graduationController.createAndStoreDistrictReports();
		Mockito.verify(districtReportService).createAndStoreDistrictReportMonth();
	}

	@Test
	void testCreateAndStoreDistrictSchoolYearEndReports() {
		Mockito.when(schoolReportsService.createAndStoreSchoolDistrictYearEndReports(ADDRESS_LABEL_YE, DISTREP_YE_SD, DISTREP_YE_SC)).thenReturn(1);
		graduationController.createAndStoreSchoolDistrictYearEndReports(ADDRESS_LABEL_YE, DISTREP_YE_SD, DISTREP_YE_SC);
		Mockito.verify(schoolReportsService).createAndStoreSchoolDistrictYearEndReports(ADDRESS_LABEL_YE, DISTREP_YE_SD, DISTREP_YE_SC);
	}

	@Test
	void testCreateAndStoreDistrictSchoolYearEndReportsBySchools() {
		YearEndReportRequest yearEndReportRequest = YearEndReportRequest.builder().schoolIds(List.of(UUID.randomUUID())).build();

		Mockito.when(schoolReportsService.createAndStoreSchoolDistrictYearEndReports(ADDRESS_LABEL_YE, DISTREP_YE_SD, DISTREP_YE_SC, yearEndReportRequest)).thenReturn(1);
		graduationController.createAndStoreSchoolDistrictYearEndReports(ADDRESS_LABEL_YE, DISTREP_YE_SD, DISTREP_YE_SC, yearEndReportRequest);
		Mockito.verify(schoolReportsService).createAndStoreSchoolDistrictYearEndReports(ADDRESS_LABEL_YE, DISTREP_YE_SD, DISTREP_YE_SC, yearEndReportRequest);
	}

	@Test
	void testCreateAndStoreDistrictSchoolPsiReports() {
		Mockito.when(schoolReportsService.createAndStoreSchoolDistrictReports(ADDRESS_LABEL_PSI, null, null)).thenReturn(1);
		graduationController.createAndStoreSchoolDistrictReports(ADDRESS_LABEL_PSI, null, null);
		Mockito.verify(schoolReportsService).createAndStoreSchoolDistrictReports(ADDRESS_LABEL_PSI, null, null);
	}

	@Test
	void testCreateAndStoreDistrictSchoolReports() {
		Mockito.when(schoolReportsService.createAndStoreSchoolDistrictReports(ADDRESS_LABEL_SCHL, DISTREP_SD, DISTREP_SC)).thenReturn(1);
		graduationController.createAndStoreSchoolDistrictReports(ADDRESS_LABEL_SCHL, DISTREP_SD, DISTREP_SC);
		Mockito.verify(schoolReportsService).createAndStoreSchoolDistrictReports(ADDRESS_LABEL_SCHL, DISTREP_SD, DISTREP_SC);
	}

	@Test
	void testCreateAndStoreDistrictSchoolYearEndPdfReports() throws IOException {
		Mockito.when(schoolReportsService.getSchoolDistrictYearEndReports(ADDRESS_LABEL_YE, DISTREP_YE_SD, DISTREP_YE_SC)).thenReturn(new byte[0]);
		graduationController.getSchoolDistrictYearEndReports(ADDRESS_LABEL_YE, DISTREP_YE_SD, DISTREP_YE_SC);
		Mockito.verify(schoolReportsService).getSchoolDistrictYearEndReports(ADDRESS_LABEL_YE, DISTREP_YE_SD, DISTREP_YE_SC);
	}

	@Test
	void testCreateAndStoreDistrictLabelsReportsBySchools() {
		UUID districtId = UUID.randomUUID();
		ca.bc.gov.educ.api.graduation.model.report.School school = new ca.bc.gov.educ.api.graduation.model.report.School();
		school.setMincode("005994567");
		school.setName("Test School Name");
		school.setSchoolId(UUID.randomUUID().toString());
		Mockito.when(districtReportService.createAndStoreDistrictLabelsReportsFromSchools(ADDRESS_LABEL_SCH_YE.name(), districtId, List.of(school), null)).thenReturn(1);
		graduationController.createAndStoreDistrictLabelsReportsBySchools(List.of(school), districtId, ADDRESS_LABEL_SCH_YE.name());
		Mockito.verify(districtReportService).createAndStoreDistrictLabelsReportsFromSchools(ADDRESS_LABEL_SCH_YE.name(), districtId, List.of(school), null);
	}

	@Test
	void testCreateAndStoreDistrictSchoolSuppReports() {
		List<ReportGradStudentData> reportGradStudentData = new ArrayList<>();
		ReportGradStudentData data = new ReportGradStudentData();
		reportGradStudentData.add(data);
		Mockito.when(reportService.getStudentsForSchoolYearEndReport()).thenReturn(reportGradStudentData);
		Mockito.when(schoolReportsService.createAndStoreSchoolDistrictReports(reportGradStudentData, SchoolReportsService.ADDRESS_LABEL_SCHL, null, DISTREP_SC)).thenReturn(1);
		graduationController.createAndStoreSchoolDistrictSuppReports(ADDRESS_LABEL_SCHL, null, DISTREP_SC);
		Mockito.verify(schoolReportsService).createAndStoreSchoolDistrictReports(reportGradStudentData, ADDRESS_LABEL_SCHL, null, DISTREP_SC);
	}

	@Test
	void testStudentsForYearEndReports() {
		List<ReportGradStudentData> reportGradStudentData = new ArrayList<>();
		ReportGradStudentData data = new ReportGradStudentData();
		reportGradStudentData.add(data);
		Mockito.when(reportService.getStudentsForSchoolYearEndReport()).thenReturn(reportGradStudentData);
		graduationController.getStudentsForYearEndReports();
		Mockito.verify(reportService).getStudentsForSchoolYearEndReport();
	}

	@Test
	void testCreateAndStoreDistrictSchoolYearEndNonGradReports() {
		List<ReportGradStudentData> reportGradStudentData = new ArrayList<>();
		ReportGradStudentData data = new ReportGradStudentData();
		reportGradStudentData.add(data);
		Mockito.when(reportService.getStudentsForSchoolNonGradYearEndReport()).thenReturn(reportGradStudentData);
		Mockito.when(schoolReportsService.createAndStoreSchoolDistrictReports(reportGradStudentData, SchoolReportsService.ADDRESS_LABEL_SCHL, null, DISTREP_SC)).thenReturn(1);
		graduationController.createAndStoreSchoolDistrictYearEndNonGradReports(ADDRESS_LABEL_SCHL, null, DISTREP_SC);
		Mockito.verify(schoolReportsService).createAndStoreSchoolDistrictReports(reportGradStudentData, ADDRESS_LABEL_SCHL, null, DISTREP_SC);
	}

	@Test
	void testCreateAndStoreDistrictSchoolYearEndNonGradReportsWithSchools() {
		List<ReportGradStudentData> reportGradStudentData = new ArrayList<>();
		ReportGradStudentData data = new ReportGradStudentData();
		UUID schoolId = UUID.randomUUID();
		data.setSchoolOfRecordId(schoolId.toString());
		reportGradStudentData.add(data);
		Mockito.when(reportService.getStudentsForSchoolNonGradYearEndReport(schoolId)).thenReturn(reportGradStudentData);
		Mockito.when(schoolReportsService.createAndStoreSchoolDistrictReports(reportGradStudentData, SchoolReportsService.ADDRESS_LABEL_SCHL, null, DISTREP_SC)).thenReturn(1);
		graduationController.createAndStoreSchoolDistrictYearEndNonGradReports(ADDRESS_LABEL_SCHL, null, DISTREP_SC, List.of(schoolId));
		Mockito.verify(schoolReportsService).createAndStoreSchoolDistrictReports(reportGradStudentData, ADDRESS_LABEL_SCHL, null, DISTREP_SC);
	}

	@Test
	void testGetDistrictSchoolYearEndNonGradPdfReports() throws IOException {
		List<ReportGradStudentData> reportGradStudentData = new ArrayList<>();
		ReportGradStudentData data = new ReportGradStudentData();
		reportGradStudentData.add(data);
		Mockito.when(reportService.getStudentsForSchoolNonGradYearEndReport()).thenReturn(reportGradStudentData);
		Mockito.when(schoolReportsService.getSchoolDistrictReports(reportGradStudentData, ADDRESS_LABEL_SCHL, null, DISTREP_SC)).thenReturn(new byte[0]);
		graduationController.getSchoolDistrictYearEndNonGradReports(ADDRESS_LABEL_SCHL, null, DISTREP_SC);
		Mockito.verify(schoolReportsService).getSchoolDistrictReports(reportGradStudentData, ADDRESS_LABEL_SCHL, null, DISTREP_SC);
	}

	@Test
	void testGetDistrictSchoolSuppPdfReports() throws IOException {
		List<ReportGradStudentData> reportGradStudentData = new ArrayList<>();
		ReportGradStudentData data = new ReportGradStudentData();
		reportGradStudentData.add(data);
		Mockito.when(reportService.getStudentsForSchoolYearEndReport()).thenReturn(reportGradStudentData);
		Mockito.when(schoolReportsService.getSchoolDistrictReports(reportGradStudentData, ADDRESS_LABEL_SCHL, null, DISTREP_SC)).thenReturn(new byte[0]);
		graduationController.getSchoolDistrictSuppReports(ADDRESS_LABEL_SCHL, null, DISTREP_SC);
		Mockito.verify(schoolReportsService).getSchoolDistrictReports(reportGradStudentData, ADDRESS_LABEL_SCHL, null, DISTREP_SC);
	}

	@Test
	void testCreateAndStoreDistrictSchoolPdfReports() throws IOException {
		Mockito.when(schoolReportsService.getSchoolDistrictReports(ADDRESS_LABEL_SCHL, DISTREP_SD, DISTREP_SC)).thenReturn(new byte[0]);
		graduationController.getSchoolDistrictReports(ADDRESS_LABEL_SCHL, DISTREP_SD, DISTREP_SC);
		Mockito.verify(schoolReportsService).getSchoolDistrictReports(ADDRESS_LABEL_SCHL, DISTREP_SD, DISTREP_SC);
	}

	@Test
	void testCreateAndStoreSchoolYearEndPdfReports() throws IOException {
		Mockito.when(schoolReportsService.getSchoolYearEndReports()).thenReturn(new byte[0]);
		graduationController.getSchoolYearEndReports();
		Mockito.verify(schoolReportsService).getSchoolYearEndReports();
	}

	@Test
	void testCreateAndStoreSchoolPdfReports() throws IOException {
		Mockito.when(schoolReportsService.getSchoolReports()).thenReturn(new byte[0]);
		graduationController.getSchoolReports();
		Mockito.verify(schoolReportsService).getSchoolReports();
	}

	@Test
	void testCreateAndStoreSchoolLabelsReports() {
		ca.bc.gov.educ.api.graduation.model.report.School school = new ca.bc.gov.educ.api.graduation.model.report.School();
		school.setMincode("005994567");
		school.setName("Test School Name");
		Mockito.when(schoolReportsService.createAndStoreSchoolLabelsReportsFromSchools("ADDRESS_LABEL_PSI", List.of(school), null)).thenReturn(1);
		graduationController.createAndStoreSchoolLabelsReports(List.of(school), "ADDRESS_LABEL_PSI");
		Mockito.verify(schoolReportsService).createAndStoreSchoolLabelsReportsFromSchools("ADDRESS_LABEL_PSI", List.of(school), null);
	}

	@Test
	void testGetSchoolLabelsReports() throws IOException {
		ca.bc.gov.educ.api.graduation.model.report.School school = new ca.bc.gov.educ.api.graduation.model.report.School();
		school.setMincode("005994567");
		school.setName("Test School Name");
		Mockito.when(schoolReportsService.getSchoolLabelsReportsFromSchools("ADDRESS_LABEL_PSI", List.of(school))).thenReturn(new byte[0]);
		graduationController.getSchoolLabelsReports(List.of(school),"ADDRESS_LABEL_PSI");
		Mockito.verify(schoolReportsService).getSchoolLabelsReportsFromSchools("ADDRESS_LABEL_PSI", List.of(school));
	}

	@Test
	void testCreateAndStoreDistrictYearEndPdfReports() throws IOException {
		Mockito.when(districtReportService.getDistrictYearEndReports()).thenReturn(new byte[0]);
		graduationController.getDistrictYearEndReports();
		Mockito.verify(districtReportService).getDistrictYearEndReports();
	}

	@Test
	void testCreateAndStoreDistrictYearEndNonGradPdfReports() throws IOException {
		Mockito.when(districtReportService.getDistrictYearEndNonGradReports()).thenReturn(new byte[0]);
		graduationController.getDistrictYearEndNonGradReports();
		Mockito.verify(districtReportService).getDistrictYearEndNonGradReports();
	}

	@Test
	void testCreateAndStoreDistrictPdfReports() throws IOException {
		Mockito.when(districtReportService.getDistrictReports()).thenReturn(new byte[0]);
		graduationController.getDistrictReports();
		Mockito.verify(districtReportService).getDistrictReports();
	}

	@Test
	void testGetSchoolReports() throws Exception {
		byte[] bytesSAR1 = readBinaryFile("data/sample.pdf");
		UUID schoolId = UUID.randomUUID();
		Mockito.when(graduationService.getSchoolReports(List.of(schoolId),"GRADREG")).thenReturn(bytesSAR1);
		graduationController.getSchoolReports(List.of(schoolId),"GRADREG");
		Mockito.verify(graduationService).getSchoolReports(List.of(schoolId),"GRADREG");

		Mockito.when(graduationService.getSchoolReports(List.of(schoolId),"NONGRADREG")).thenReturn(bytesSAR1);
		graduationController.getSchoolReports(List.of(schoolId),"NONGRADREG");
		Mockito.verify(graduationService).getSchoolReports(List.of(schoolId),"NONGRADREG");

		Mockito.when(graduationService.getSchoolReports(List.of(schoolId),"NONGRADPRJ")).thenReturn(bytesSAR1);
		graduationController.getSchoolReports(List.of(schoolId),"NONGRADPRJ");
		Mockito.verify(graduationService).getSchoolReports(List.of(schoolId),"NONGRADPRJ");
	}

	@Test
	void testGetSchoolReportsEmpty() {
		byte[] bytesSAR1 = new byte[0];
		UUID schoolId = UUID.randomUUID();
		Mockito.when(graduationService.getSchoolReports(List.of(schoolId),"GRADREG")).thenReturn(bytesSAR1);
		graduationController.getSchoolReports(List.of(schoolId),"GRADREG");
		Mockito.verify(graduationService).getSchoolReports(List.of(schoolId),"GRADREG");

		Mockito.when(graduationService.getSchoolReports(List.of(schoolId),"NONGRADREG")).thenReturn(bytesSAR1);
		graduationController.getSchoolReports(List.of(schoolId),"NONGRADREG");
		Mockito.verify(graduationService).getSchoolReports(List.of(schoolId),"NONGRADREG");

		Mockito.when(graduationService.getSchoolReports(List.of(schoolId),"NONGRADPRJ")).thenReturn(bytesSAR1);
		graduationController.getSchoolReports(List.of(schoolId),"NONGRADPRJ");
		Mockito.verify(graduationService).getSchoolReports(List.of(schoolId),"NONGRADPRJ");
	}

	@Test
	void testGetSchoolReportsNull() {
		UUID schoolId = UUID.randomUUID();
		Mockito.when(graduationService.getSchoolReports(List.of(schoolId),"GRADREG")).thenReturn(null);
		graduationController.getSchoolReports(List.of(schoolId),"GRADREG");
		Mockito.verify(graduationService).getSchoolReports(List.of(schoolId),"GRADREG");

		Mockito.when(graduationService.getSchoolReports(List.of(schoolId),"NONGRADREG")).thenReturn(null);
		graduationController.getSchoolReports(List.of(schoolId),"NONGRADREG");
		Mockito.verify(graduationService).getSchoolReports(List.of(schoolId),"NONGRADREG");

		Mockito.when(graduationService.getSchoolReports(List.of(schoolId),"NONGRADPRJ")).thenReturn(null);
		graduationController.getSchoolReports(List.of(schoolId),"NONGRADPRJ");
		Mockito.verify(graduationService).getSchoolReports(List.of(schoolId),"NONGRADPRJ");
	}

	@Test
	void testCreateAndStoreStudentCertificate() {

		Mockito.when(graduationService.createAndStoreStudentCertificates("123456789", true)).thenReturn(1);
		graduationController.createAndStoreStudentCertificate("123456789", "Y");
		Mockito.verify(graduationService).createAndStoreStudentCertificates("123456789", true);
	}

	private byte[] readBinaryFile(String path) throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(path);
		return inputStream.readAllBytes();
	}
}
