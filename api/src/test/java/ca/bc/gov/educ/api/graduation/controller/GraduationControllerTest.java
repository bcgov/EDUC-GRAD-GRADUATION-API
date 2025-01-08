package ca.bc.gov.educ.api.graduation.controller;

import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.model.report.GradProgram;
import ca.bc.gov.educ.api.graduation.model.report.ReportData;
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static ca.bc.gov.educ.api.graduation.service.SchoolReportsService.*;


@ExtendWith(MockitoExtension.class)
class GraduationControllerTest {

	@Mock
	private GraduationService graduationService;

	@Mock
	private SchoolReportsService schoolReportsService;

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
		graduationController.createAndStoreSchoolReports(List.of(schoolId),"accessToken","NONGRAD");
		Mockito.verify(graduationService).createAndStoreSchoolReports(List.of(schoolId),"NONGRAD");
	}

	@Test
	void testCreateAndStoreSchoolYearEndReports() {
		Mockito.when(schoolReportsService.createAndStoreSchoolReports(DISTREP_YE_SC, "accessToken")).thenReturn(1);
		graduationController.createAndStoreSchoolYearEndReports("accessToken");
		Mockito.verify(schoolReportsService).createAndStoreSchoolReports(DISTREP_YE_SC, "accessToken");
	}

	@Test
	void testCreateAndStoreSchoolsReports() {
		Mockito.when(schoolReportsService.createAndStoreSchoolReports(DISTREP_SC, "accessToken")).thenReturn(1);
		graduationController.createAndStoreSchoolReports("accessToken");
		Mockito.verify(schoolReportsService).createAndStoreSchoolReports(DISTREP_SC, "accessToken");
	}

	@Test
	void testCreateAndStoreDistrictYearEndReports() {
		Mockito.when(schoolReportsService.createAndStoreDistrictReports(DISTREP_YE_SD, "accessToken")).thenReturn(1);
		graduationController.createAndStoreDistrictYearEndReports("accessToken");
		Mockito.verify(schoolReportsService).createAndStoreDistrictReports(DISTREP_YE_SD, "accessToken");
	}

	@Test
	void testCreateAndStoreDistrictYearEndNonGradReports() {
		Mockito.when(schoolReportsService.createAndStoreDistrictReports(NONGRADDISTREP_SD, "accessToken")).thenReturn(1);
		graduationController.createAndStoreDistrictYearEndNonGradReports("accessToken");
		Mockito.verify(schoolReportsService).createAndStoreDistrictReports(NONGRADDISTREP_SD, "accessToken");
	}

	@Test
	void testCreateAndStoreDistrictReports() {
		Mockito.when(schoolReportsService.createAndStoreDistrictReports(DISTREP_SD, "accessToken")).thenReturn(1);
		graduationController.createAndStoreDistrictReports("accessToken");
		Mockito.verify(schoolReportsService).createAndStoreDistrictReports(DISTREP_SD, "accessToken");
	}

	@Test
	void testCreateAndStoreDistrictSchoolYearEndReports() {
		Mockito.when(schoolReportsService.createAndStoreSchoolDistrictYearEndReports("accessToken", ADDRESS_LABEL_YE, DISTREP_YE_SD, DISTREP_YE_SC)).thenReturn(1);
		graduationController.createAndStoreSchoolDistrictYearEndReports("accessToken", ADDRESS_LABEL_YE, DISTREP_YE_SD, DISTREP_YE_SC);
		Mockito.verify(schoolReportsService).createAndStoreSchoolDistrictYearEndReports("accessToken", ADDRESS_LABEL_YE, DISTREP_YE_SD, DISTREP_YE_SC);
	}

	@Test
	void testCreateAndStoreDistrictSchoolYearEndReportsBySchools() {
		ca.bc.gov.educ.api.graduation.model.report.School school = new ca.bc.gov.educ.api.graduation.model.report.School();
		school.setMincode("005994567");
		school.setName("Test School Name");
		List<String> schools = new ArrayList<>();
		schools.add(school.getMincode());

		Mockito.when(schoolReportsService.createAndStoreSchoolDistrictYearEndReports("accessToken", ADDRESS_LABEL_YE, DISTREP_YE_SD, DISTREP_YE_SC, schools)).thenReturn(1);
		graduationController.createAndStoreSchoolDistrictYearEndReports("accessToken", ADDRESS_LABEL_YE, DISTREP_YE_SD, DISTREP_YE_SC, schools);
		Mockito.verify(schoolReportsService).createAndStoreSchoolDistrictYearEndReports("accessToken", ADDRESS_LABEL_YE, DISTREP_YE_SD, DISTREP_YE_SC, schools);
	}

	@Test
	void testCreateAndStoreDistrictSchoolPsiReports() {
		Mockito.when(schoolReportsService.createAndStoreSchoolDistrictReports("accessToken", ADDRESS_LABEL_PSI, null, null)).thenReturn(1);
		graduationController.createAndStoreSchoolDistrictReports("accessToken", ADDRESS_LABEL_PSI, null, null);
		Mockito.verify(schoolReportsService).createAndStoreSchoolDistrictReports("accessToken", ADDRESS_LABEL_PSI, null, null);
	}

	@Test
	void testCreateAndStoreDistrictSchoolReports() {
		Mockito.when(schoolReportsService.createAndStoreSchoolDistrictReports("accessToken", ADDRESS_LABEL_SCHL, DISTREP_SD, DISTREP_SC)).thenReturn(1);
		graduationController.createAndStoreSchoolDistrictReports("accessToken", ADDRESS_LABEL_SCHL, DISTREP_SD, DISTREP_SC);
		Mockito.verify(schoolReportsService).createAndStoreSchoolDistrictReports("accessToken", ADDRESS_LABEL_SCHL, DISTREP_SD, DISTREP_SC);
	}

	@Test
	void testCreateAndStoreDistrictSchoolYearEndPdfReports() {
		Mockito.when(schoolReportsService.getSchoolDistrictYearEndReports("accessToken", ADDRESS_LABEL_YE, DISTREP_YE_SD, DISTREP_YE_SC)).thenReturn(new byte[0]);
		graduationController.getSchoolDistrictYearEndReports("accessToken", ADDRESS_LABEL_YE, DISTREP_YE_SD, DISTREP_YE_SC);
		Mockito.verify(schoolReportsService).getSchoolDistrictYearEndReports("accessToken", ADDRESS_LABEL_YE, DISTREP_YE_SD, DISTREP_YE_SC);
	}

	@Test
	void testCreateAndStoreDistrictSchoolSuppReports() {
		List<ReportGradStudentData> reportGradStudentData = new ArrayList<>();
		ReportGradStudentData data = new ReportGradStudentData();
		reportGradStudentData.add(data);
		Mockito.when(reportService.getStudentsForSchoolYearEndReport("accessToken")).thenReturn(reportGradStudentData);
		Mockito.when(schoolReportsService.createAndStoreSchoolDistrictReports("accessToken", reportGradStudentData, SchoolReportsService.ADDRESS_LABEL_SCHL, null, DISTREP_SC)).thenReturn(1);
		graduationController.createAndStoreSchoolDistrictSuppReports("accessToken", ADDRESS_LABEL_SCHL, null, DISTREP_SC);
		Mockito.verify(schoolReportsService).createAndStoreSchoolDistrictReports("accessToken", reportGradStudentData, ADDRESS_LABEL_SCHL, null, DISTREP_SC);
	}

	@Test
	void testStudentsForYearEndReports() {
		List<ReportGradStudentData> reportGradStudentData = new ArrayList<>();
		ReportGradStudentData data = new ReportGradStudentData();
		reportGradStudentData.add(data);
		Mockito.when(reportService.getStudentsForSchoolYearEndReport("accessToken")).thenReturn(reportGradStudentData);
		graduationController.getStudentsForYearEndReports("accessToken");
		Mockito.verify(reportService).getStudentsForSchoolYearEndReport("accessToken");
	}

	@Test
	void testCreateAndStoreDistrictSchoolYearEndNonGradReports() {
		List<ReportGradStudentData> reportGradStudentData = new ArrayList<>();
		ReportGradStudentData data = new ReportGradStudentData();
		reportGradStudentData.add(data);
		Mockito.when(reportService.getStudentsForSchoolNonGradYearEndReport()).thenReturn(reportGradStudentData);
		Mockito.when(schoolReportsService.createAndStoreSchoolDistrictReports("accessToken", reportGradStudentData, SchoolReportsService.ADDRESS_LABEL_SCHL, null, DISTREP_SC)).thenReturn(1);
		graduationController.createAndStoreSchoolDistrictYearEndNonGradReports("accessToken", ADDRESS_LABEL_SCHL, null, DISTREP_SC);
		Mockito.verify(schoolReportsService).createAndStoreSchoolDistrictReports("accessToken", reportGradStudentData, ADDRESS_LABEL_SCHL, null, DISTREP_SC);
	}

	@Test
	void testCreateAndStoreDistrictSchoolYearEndNonGradReportsWithSchools() {
		List<ReportGradStudentData> reportGradStudentData = new ArrayList<>();
		ReportGradStudentData data = new ReportGradStudentData();
		data.setMincode("1234567");
		reportGradStudentData.add(data);
		Mockito.when(reportService.getStudentsForSchoolNonGradYearEndReport(data.getMincode())).thenReturn(reportGradStudentData);
		Mockito.when(schoolReportsService.createAndStoreSchoolDistrictReports("accessToken", reportGradStudentData, SchoolReportsService.ADDRESS_LABEL_SCHL, null, DISTREP_SC)).thenReturn(1);
		graduationController.createAndStoreSchoolDistrictYearEndNonGradReports("accessToken", ADDRESS_LABEL_SCHL, null, DISTREP_SC, List.of(data.getMincode()));
		Mockito.verify(schoolReportsService).createAndStoreSchoolDistrictReports("accessToken", reportGradStudentData, ADDRESS_LABEL_SCHL, null, DISTREP_SC);
	}

	@Test
	void testGetDistrictSchoolYearEndNonGradPdfReports() {
		List<ReportGradStudentData> reportGradStudentData = new ArrayList<>();
		ReportGradStudentData data = new ReportGradStudentData();
		reportGradStudentData.add(data);
		Mockito.when(reportService.getStudentsForSchoolNonGradYearEndReport()).thenReturn(reportGradStudentData);
		Mockito.when(schoolReportsService.getSchoolDistrictReports("accessToken", reportGradStudentData, ADDRESS_LABEL_SCHL, null, DISTREP_SC)).thenReturn(new byte[0]);
		graduationController.getSchoolDistrictYearEndNonGradReports("accessToken", ADDRESS_LABEL_SCHL, null, DISTREP_SC);
		Mockito.verify(schoolReportsService).getSchoolDistrictReports("accessToken", reportGradStudentData, ADDRESS_LABEL_SCHL, null, DISTREP_SC);
	}

	@Test
	void testGetDistrictSchoolSuppPdfReports() {
		List<ReportGradStudentData> reportGradStudentData = new ArrayList<>();
		ReportGradStudentData data = new ReportGradStudentData();
		reportGradStudentData.add(data);
		Mockito.when(reportService.getStudentsForSchoolYearEndReport("accessToken")).thenReturn(reportGradStudentData);
		Mockito.when(schoolReportsService.getSchoolDistrictReports("accessToken", reportGradStudentData, ADDRESS_LABEL_SCHL, null, DISTREP_SC)).thenReturn(new byte[0]);
		graduationController.getSchoolDistrictSuppReports("accessToken", ADDRESS_LABEL_SCHL, null, DISTREP_SC);
		Mockito.verify(schoolReportsService).getSchoolDistrictReports("accessToken", reportGradStudentData, ADDRESS_LABEL_SCHL, null, DISTREP_SC);
	}

	@Test
	void testCreateAndStoreDistrictSchoolPdfReports() {
		Mockito.when(schoolReportsService.getSchoolDistrictReports("accessToken", ADDRESS_LABEL_SCHL, DISTREP_SD, DISTREP_SC)).thenReturn(new byte[0]);
		graduationController.getSchoolDistrictReports("accessToken", ADDRESS_LABEL_SCHL, DISTREP_SD, DISTREP_SC);
		Mockito.verify(schoolReportsService).getSchoolDistrictReports("accessToken", ADDRESS_LABEL_SCHL, DISTREP_SD, DISTREP_SC);
	}

	@Test
	void testCreateAndStoreSchoolYearEndPdfReports() {
		Mockito.when(schoolReportsService.getSchoolYearEndReports("accessToken")).thenReturn(new byte[0]);
		graduationController.getSchoolYearEndReports("accessToken");
		Mockito.verify(schoolReportsService).getSchoolYearEndReports("accessToken");
	}

	@Test
	void testCreateAndStoreSchoolPdfReports() {
		Mockito.when(schoolReportsService.getSchoolReports("accessToken")).thenReturn(new byte[0]);
		graduationController.getSchoolReports("accessToken");
		Mockito.verify(schoolReportsService).getSchoolReports("accessToken");
	}

	@Test
	void testCreateAndStoreSchoolLabelsReports() {
		ca.bc.gov.educ.api.graduation.model.report.School school = new ca.bc.gov.educ.api.graduation.model.report.School();
		school.setMincode("005994567");
		school.setName("Test School Name");
		Mockito.when(schoolReportsService.createAndStoreSchoolLabelsReportsFromSchools("ADDRESS_LABEL_PSI", List.of(school),"accessToken", null)).thenReturn(1);
		graduationController.createAndStoreSchoolLabelsReports(List.of(school),"accessToken", "ADDRESS_LABEL_PSI");
		Mockito.verify(schoolReportsService).createAndStoreSchoolLabelsReportsFromSchools("ADDRESS_LABEL_PSI", List.of(school),"accessToken", null);
	}

	@Test
	void testGetSchoolLabelsReports() {
		ca.bc.gov.educ.api.graduation.model.report.School school = new ca.bc.gov.educ.api.graduation.model.report.School();
		school.setMincode("005994567");
		school.setName("Test School Name");
		Mockito.when(schoolReportsService.getSchoolLabelsReportsFromSchools("ADDRESS_LABEL_PSI", List.of(school),"accessToken")).thenReturn(new byte[0]);
		graduationController.getSchoolLabelsReports(List.of(school),"accessToken", "ADDRESS_LABEL_PSI");
		Mockito.verify(schoolReportsService).getSchoolLabelsReportsFromSchools("ADDRESS_LABEL_PSI", List.of(school),"accessToken");
	}

	@Test
	void testCreateAndStoreDistrictYearEndPdfReports() {
		Mockito.when(schoolReportsService.getDistrictYearEndReports("accessToken")).thenReturn(new byte[0]);
		graduationController.getDistrictYearEndReports("accessToken");
		Mockito.verify(schoolReportsService).getDistrictYearEndReports("accessToken");
	}

	@Test
	void testCreateAndStoreDistrictYearEndNonGradPdfReports() {
		Mockito.when(schoolReportsService.getDistrictYearEndNonGradReports("accessToken")).thenReturn(new byte[0]);
		graduationController.getDistrictYearEndNonGradReports("accessToken");
		Mockito.verify(schoolReportsService).getDistrictYearEndNonGradReports("accessToken");
	}

	@Test
	void testCreateAndStoreDistrictPdfReports() {
		Mockito.when(schoolReportsService.getDistrictReports("accessToken")).thenReturn(new byte[0]);
		graduationController.getDistrictReports("accessToken");
		Mockito.verify(schoolReportsService).getDistrictReports("accessToken");
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
	void testGetSchoolReportsEmpty() throws Exception {
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
