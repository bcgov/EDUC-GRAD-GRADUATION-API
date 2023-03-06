package ca.bc.gov.educ.api.graduation.controller;

import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.model.report.GradProgram;
import ca.bc.gov.educ.api.graduation.model.report.ReportData;
import ca.bc.gov.educ.api.graduation.service.GraduationService;
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

import static ca.bc.gov.educ.api.graduation.service.SchoolReportsService.DISTREP_YE_SC;
import static ca.bc.gov.educ.api.graduation.service.SchoolReportsService.DISTREP_YE_SD;


@ExtendWith(MockitoExtension.class)
class GraduationControllerTest {

	@Mock
	private GraduationService graduationService;

	@Mock
	private SchoolReportsService schoolReportsService;
	
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
	void testCreateAndStoreDistrictYearEndReports() {
		Mockito.when(schoolReportsService.createAndStoreDistrictReports(DISTREP_YE_SD, "accessToken")).thenReturn(1);
		graduationController.createAndStoreDistrictYearEndReports("accessToken");
		Mockito.verify(schoolReportsService).createAndStoreDistrictReports(DISTREP_YE_SD, "accessToken");
	}

	@Test
	void testCreateAndStoreDistrictSchoolYearEndReports() {
		Mockito.when(schoolReportsService.createAndStoreSchoolDistrictYearEndReports("accessToken")).thenReturn(1);
		graduationController.createAndStoreSchoolDistrictYearEndReports("accessToken");
		Mockito.verify(schoolReportsService).createAndStoreSchoolDistrictYearEndReports("accessToken");
	}

	@Test
	void testCreateAndStoreDistrictSchoolYearEndPdfReports() {
		Mockito.when(schoolReportsService.getSchoolDistrictYearEndReports("accessToken")).thenReturn(new byte[0]);
		graduationController.getSchoolDistrictYearEndReports("accessToken");
		Mockito.verify(schoolReportsService).getSchoolDistrictYearEndReports("accessToken");
	}

	@Test
	void testCreateAndStoreSchoolYearEndPdfReports() {
		Mockito.when(schoolReportsService.getSchoolYearEndReports("accessToken")).thenReturn(new byte[0]);
		graduationController.getSchoolYearEndReports("accessToken");
		Mockito.verify(schoolReportsService).getSchoolYearEndReports("accessToken");
	}

	@Test
	void testCreateAndStoreDistrictYearEndPdfReports() {
		Mockito.when(schoolReportsService.getDistrictYearEndReports("accessToken")).thenReturn(new byte[0]);
		graduationController.getDistrictYearEndReports("accessToken");
		Mockito.verify(schoolReportsService).getDistrictYearEndReports("accessToken");
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
