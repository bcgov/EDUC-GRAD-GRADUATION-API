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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@ExtendWith(MockitoExtension.class)
class GraduationControllerTest {

	private static final String DISTREP_YE_SD = "DISTREP_YE_SD";
	private static final String DISTREP_YE_SC = "DISTREP_YE_SC";
	private static final String ADDRESS_LABEL_SCHL = "ADDRESS_LABEL_SCHL";
	private static final String ADDRESS_LABEL_YE = "ADDRESS_LABEL_YE";
	private static final String ADDRESS_LABEL_PSI = "ADDRESS_LABEL_PSI";
	private static final String DISTREP_SD = "DISTREP_SD";
	private static final String DISTREP_SC = "DISTREP_SC";

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
		Mockito.when(graduationService.graduateStudent(studentID,null,"accessToken",projectedType)).thenReturn(alRes);
		graduationController.graduateStudentNew(studentID,projectedType,null, "accessToken");
		Mockito.verify(graduationService).graduateStudent(studentID,null,"accessToken",projectedType);
	}

	@Test
	void testReportDataByPen() {
		ReportData data = new ReportData();
		data.setGradProgram(new GradProgram());
		Mockito.when(graduationService.prepareReportData("12312312312","XML","accessToken")).thenReturn(data);
		graduationController.reportDataByPen("12312312312","XML","accessToken");
		Mockito.verify(graduationService).prepareReportData("12312312312","XML","accessToken");
	}

	@Test
	void testReportTranscriptByPen() {
		byte[] bytesSAR = "Any String you want".getBytes();
		Mockito.when(graduationService.prepareTranscriptReport("12312312312","Interim", null,"accessToken")).thenReturn(bytesSAR);
		graduationController.reportTranscriptByPen("12312312312","Interim",null,"accessToken");
		Mockito.verify(graduationService).prepareTranscriptReport("12312312312","Interim",null,"accessToken");
	}

	@Test
	void testReportTranscriptByPen_null() {
		byte[] bytesSAR = null;
		Mockito.when(graduationService.prepareTranscriptReport("12312312312","Interim",null,"accessToken")).thenReturn(bytesSAR);
		graduationController.reportTranscriptByPen("12312312312","Interim",null,"accessToken");
		Mockito.verify(graduationService).prepareTranscriptReport("12312312312","Interim",null,"accessToken");
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
		Mockito.when(graduationService.prepareReportData(graduationData,"XML","accessToken")).thenReturn(data);
		graduationController.reportDataFromGraduation(graduationData,"XML","accessToken");
		Mockito.verify(graduationService).prepareReportData(graduationData,"XML","accessToken");
	}

	@Test
	void testCreateAndStoreSchoolReports() {
		Mockito.when(graduationService.createAndStoreSchoolReports(List.of("12321312"),"NONGRAD","accessToken")).thenReturn(1);
		graduationController.createAndStoreSchoolReports(List.of("12321312"),"accessToken","NONGRAD");
		Mockito.verify(graduationService).createAndStoreSchoolReports(List.of("12321312"),"NONGRAD","accessToken");
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
	void testCreateAndStoreDistrictPdfReports() {
		Mockito.when(schoolReportsService.getDistrictReports("accessToken")).thenReturn(new byte[0]);
		graduationController.getDistrictReports("accessToken");
		Mockito.verify(schoolReportsService).getDistrictReports("accessToken");
	}

	@Test
	void testGetSchoolReports() {
		Mockito.when(graduationService.getSchoolReports(List.of("12321312"),"GRADREG","accessToken")).thenReturn(new byte[0]);
		graduationController.getSchoolReports(List.of("12321312"),"accessToken","GRADREG");
		Mockito.verify(graduationService).getSchoolReports(List.of("12321312"),"GRADREG","accessToken");

		Mockito.when(graduationService.getSchoolReports(List.of("12321312"),"NONGRADREG","accessToken")).thenReturn(new byte[0]);
		graduationController.getSchoolReports(List.of("12321312"),"accessToken","NONGRADREG");
		Mockito.verify(graduationService).getSchoolReports(List.of("12321312"),"NONGRADREG","accessToken");

		Mockito.when(graduationService.getSchoolReports(List.of("12321312"),"NONGRADPRJ","accessToken")).thenReturn(new byte[0]);
		graduationController.getSchoolReports(List.of("12321312"),"accessToken","NONGRADPRJ");
		Mockito.verify(graduationService).getSchoolReports(List.of("12321312"),"NONGRADPRJ","accessToken");
	}
	
}
