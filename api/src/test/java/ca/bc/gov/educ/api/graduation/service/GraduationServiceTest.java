package ca.bc.gov.educ.api.graduation.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;

import ca.bc.gov.educ.api.graduation.model.dto.AlgorithmResponse;
import ca.bc.gov.educ.api.graduation.model.dto.GradAlgorithmGraduationStudentRecord;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationData;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationMessages;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationStudentRecord;
import ca.bc.gov.educ.api.graduation.model.dto.ProgramCertificate;
import ca.bc.gov.educ.api.graduation.model.dto.School;
import ca.bc.gov.educ.api.graduation.model.dto.StudentDemographics;
import ca.bc.gov.educ.api.graduation.model.dto.StudentOptionalProgram;
import ca.bc.gov.educ.api.graduation.model.report.ReportData;
import ca.bc.gov.educ.api.graduation.util.GradBusinessRuleException;
import ca.bc.gov.educ.api.graduation.util.GradValidation;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class GraduationServiceTest {

	@Autowired
	private GraduationService graduationService;
	
	@MockBean
	private GradStatusService gradStatusService;
	
	@MockBean
	private GradAlgorithmService gradAlgorithmService;
	
	@MockBean
	private SpecialProgramService specialProgramService;
	
	@MockBean
	private ReportService reportService;
	
	@Autowired
	GradValidation validation;
	
	@Mock
	WebClient webClient;
	
	@Test
	public void testGraduateStudent() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="REGFM";
		String accessToken="accessToken";
		
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setStudentID(new UUID(1, 1));
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("A");
		
		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");
		
		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setStudentCourses(null);
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setSpecialProgramCode("BD");
		spgm.setSpecialProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<StudentOptionalProgram>();
		list.add(spgm);
		
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken)).thenReturn(graduationDataStatus);
		try {
			Mockito.when(specialProgramService.projectedSpecialPrograms(graduationDataStatus, studentID, accessToken)).thenReturn(list);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		AlgorithmResponse response = graduationService.graduateStudent(studentID,accessToken,projectedType);
		assertNotNull(response);
	}
	
	@Test
	public void testGraduateStudent_error() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="REGFM";
		String accessToken="accessToken";
		validation.clear();
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setStudentID(new UUID(1, 1));
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");
		
		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");
		
		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setStudentCourses(null);
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setSpecialProgramCode("BD");
		spgm.setSpecialProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<StudentOptionalProgram>();
		list.add(spgm);
		
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken)).thenReturn(graduationDataStatus);
		try {
			Mockito.when(specialProgramService.projectedSpecialPrograms(graduationDataStatus, studentID, accessToken)).thenReturn(list);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		try {
			graduationService.graduateStudent(studentID,accessToken,projectedType);
		} catch (GradBusinessRuleException e) {
			List<String> errors = validation.getErrors();
			assertEquals(1, errors.size());
			return;
		}
	}
	
	@Test
	public void testGraduateStudent_error2() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="REGFM";
		String accessToken="accessToken";
		validation.clear();
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setStudentID(new UUID(1, 1));
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("M");
		
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken)).thenReturn(gradResponse);
		try {
			graduationService.graduateStudent(studentID,accessToken,projectedType);
		} catch (GradBusinessRuleException e) {
			List<String> errors = validation.getErrors();
			assertEquals(1, errors.size());
			return;
		}
	}
	
	@Test
	public void testGraduateStudent_withProjectedTypeFM() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="FM";
		String accessToken="accessToken";
		
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setStudentID(new UUID(1, 1));
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("A");
		
		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");
		
		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setStudentCourses(null);
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setSpecialProgramCode("BD");
		spgm.setSpecialProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<StudentOptionalProgram>();
		list.add(spgm);
		
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken)).thenReturn(graduationDataStatus);
		try {
			Mockito.when(specialProgramService.projectedSpecialPrograms(graduationDataStatus, studentID, accessToken)).thenReturn(list);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		AlgorithmResponse response = graduationService.graduateStudent(studentID,accessToken,projectedType);
		assertNotNull(response);
	}
	
	@Test
	public void testGraduateStudent_withProjectedTypeGS() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="GS";
		String accessToken="accessToken";
		
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setStudentID(new UUID(1, 1));
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("A");
		
		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");
		
		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setStudentCourses(null);
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setSpecialProgramCode("BD");
		spgm.setSpecialProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<StudentOptionalProgram>();
		list.add(spgm);
		
		StudentDemographics sD = new StudentDemographics();
		sD.setPen("123123123");
		sD.setLegalFirstName("ABC");
		sD.setLegalLastName("FDG");
		sD.setLegalMiddleNames("DER");
		sD.setSchoolOfRecord("12321321");
		
		GraduationMessages gM = new GraduationMessages();
		gM.setGradMessage("asdad");
		
		School schoolObj = new School();
		schoolObj.setMinCode("1231123");
		schoolObj.setIndependentDesignation("2");
		
		ReportData data = new ReportData();
		data.setGradMessage("ABC");
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.prepareGraduationStatusObj(graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareReportData(graduationDataStatus,gradResponse,accessToken)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID, accessToken,gradResponse)).thenReturn(gradResponse);
		try {
			Mockito.when(specialProgramService.saveAndLogSpecialPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		} catch (JsonProcessingException e1) {
			e1.printStackTrace();
		}
		AlgorithmResponse response = graduationService.graduateStudent(studentID,accessToken,projectedType);
		assertNotNull(response);
	}
	
	@Test
	public void testGraduateStudent_withProjectedTypeGS_programcompletionDate_notnull() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="GS";
		String accessToken="accessToken";
		validation.clear();
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setStudentID(new UUID(1, 1));
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate("2021-09-01");
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("A");
		
		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");
		
		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setStudentCourses(null);
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setSpecialProgramCode("BD");
		spgm.setSpecialProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<StudentOptionalProgram>();
		list.add(spgm);
		
		StudentDemographics sD = new StudentDemographics();
		sD.setPen("123123123");
		sD.setLegalFirstName("ABC");
		sD.setLegalLastName("FDG");
		sD.setLegalMiddleNames("DER");
		sD.setSchoolOfRecord("12321321");
		
		GraduationMessages gM = new GraduationMessages();
		gM.setGradMessage("asdad");
		
		School schoolObj = new School();
		schoolObj.setMinCode("1231123");
		schoolObj.setIndependentDesignation("2");
		
		ReportData data = new ReportData();
		data.setGradMessage("ABC");
		
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken)).thenReturn(gradResponse);
		try {
			AlgorithmResponse response = graduationService.graduateStudent(studentID,accessToken,projectedType);
			assertNotNull(response);
		} catch (GradBusinessRuleException e) {
			List<String> errors = validation.getErrors();
			assertEquals(1, errors.size());
			return;
		}
		
	}
	
	@Test
	public void testGraduateStudent_withProjectedTypeGS_programCompletionDate_notnull_program_sccp() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="GS";
		String accessToken="accessToken";
		
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setStudentID(new UUID(1, 1));
		gradResponse.setProgram("SCCP");
		gradResponse.setProgramCompletionDate("2021-09-01");
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("A");
		
		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");
		
		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setStudentCourses(null);
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setSpecialProgramCode("BD");
		spgm.setSpecialProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<StudentOptionalProgram>();
		list.add(spgm);
		
		StudentDemographics sD = new StudentDemographics();
		sD.setPen("123123123");
		sD.setLegalFirstName("ABC");
		sD.setLegalLastName("FDG");
		sD.setLegalMiddleNames("DER");
		sD.setSchoolOfRecord("12321321");
		
		GraduationMessages gM = new GraduationMessages();
		gM.setGradMessage("asdad");
		
		School schoolObj = new School();
		schoolObj.setMinCode("1231123");
		schoolObj.setIndependentDesignation("2");
		
		ReportData data = new ReportData();
		data.setGradMessage("ABC");
		
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.prepareGraduationStatusObj(graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareReportData(graduationDataStatus,gradResponse,accessToken)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID, accessToken,gradResponse)).thenReturn(gradResponse);
		try {
			Mockito.when(specialProgramService.saveAndLogSpecialPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		} catch (JsonProcessingException e1) {
			e1.printStackTrace();
		}
		AlgorithmResponse response = graduationService.graduateStudent(studentID,accessToken,projectedType);
		assertNotNull(response);
	}
	
	@Test
	public void testGraduateStudent_withProjectedTypeGS_programCompletionDate_null_program_sccp() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="GS";
		String accessToken="accessToken";
		
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setStudentID(new UUID(1, 1));
		gradResponse.setProgram("SCCP");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("A");
		
		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");
		
		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setStudentCourses(null);
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setSpecialProgramCode("BD");
		spgm.setSpecialProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<StudentOptionalProgram>();
		list.add(spgm);
		
		StudentDemographics sD = new StudentDemographics();
		sD.setPen("123123123");
		sD.setLegalFirstName("ABC");
		sD.setLegalLastName("FDG");
		sD.setLegalMiddleNames("DER");
		sD.setSchoolOfRecord("12321321");
		
		GraduationMessages gM = new GraduationMessages();
		gM.setGradMessage("asdad");
		
		School schoolObj = new School();
		schoolObj.setMinCode("1231123");
		schoolObj.setIndependentDesignation("2");
		
		ReportData data = new ReportData();
		data.setGradMessage("ABC");		
		
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.prepareGraduationStatusObj(graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareReportData(graduationDataStatus,gradResponse,accessToken)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID, accessToken,gradResponse)).thenReturn(gradResponse);
		try {
			Mockito.when(specialProgramService.saveAndLogSpecialPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		} catch (JsonProcessingException e1) {
			e1.printStackTrace();
		}
		AlgorithmResponse response = graduationService.graduateStudent(studentID,accessToken,projectedType);
		assertNotNull(response);
	}
	
	@Test
	public void testGraduateStudent_withProjectedTypeGS_Graduated() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="GS";
		String accessToken="accessToken";
		
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setStudentID(new UUID(1, 1));
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("A");
		
		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");
		
		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(true);
		graduationDataStatus.setStudentCourses(null);
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setSpecialProgramCode("BD");
		spgm.setSpecialProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<StudentOptionalProgram>();
		list.add(spgm);
		
		StudentDemographics sD = new StudentDemographics();
		sD.setPen("123123123");
		sD.setLegalFirstName("ABC");
		sD.setLegalLastName("FDG");
		sD.setLegalMiddleNames("DER");
		sD.setSchoolOfRecord("12321321");
		
		GraduationMessages gM = new GraduationMessages();
		gM.setGradMessage("asdad");
		
		School schoolObj = new School();
		schoolObj.setMinCode("1231123");
		schoolObj.setIndependentDesignation("2");
		
		ReportData data = new ReportData();
		data.setGradMessage("ABC");
		
		List<ProgramCertificate> certificateList = new ArrayList<ProgramCertificate>();
		ProgramCertificate pc= new ProgramCertificate();
		pc.setCertificateTypeCode("E");
		certificateList.add(pc);
		
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.prepareGraduationStatusObj(graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareReportData(graduationDataStatus,gradResponse,accessToken)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID, accessToken,gradResponse)).thenReturn(gradResponse);
		Mockito.when(reportService.getCertificateList(gradResponse,graduationDataStatus,list,accessToken)).thenReturn(certificateList);
		doNothing().when(reportService).saveStudentCertificateReportJasper(gradResponse,graduationDataStatus,accessToken,pc);
		try {
			Mockito.when(specialProgramService.saveAndLogSpecialPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		} catch (JsonProcessingException e1) {
			e1.printStackTrace();
		}
		AlgorithmResponse response = graduationService.graduateStudent(studentID,accessToken,projectedType);
		assertNotNull(response);
	}
	
	@Test
	public void testGraduateStudent_withProjectedTypeFMR_programCompletionDate_null() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="FMR";
		String accessToken="accessToken";
		
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setStudentID(new UUID(1, 1));
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("A");
		
		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");
		
		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setStudentCourses(null);
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setSpecialProgramCode("BD");
		spgm.setSpecialProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<StudentOptionalProgram>();
		list.add(spgm);
		
		StudentDemographics sD = new StudentDemographics();
		sD.setPen("123123123");
		sD.setLegalFirstName("ABC");
		sD.setLegalLastName("FDG");
		sD.setLegalMiddleNames("DER");
		sD.setSchoolOfRecord("12321321");
		
		GraduationMessages gM = new GraduationMessages();
		gM.setGradMessage("asdad");
		
		School schoolObj = new School();
		schoolObj.setMinCode("1231123");
		schoolObj.setIndependentDesignation("2");
		
		ReportData data = new ReportData();
		data.setGradMessage("ABC");
		
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.prepareGraduationStatusObj(graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareReportData(graduationDataStatus,gradResponse,accessToken)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID, accessToken,gradResponse)).thenReturn(gradResponse);
		try {
			Mockito.when(specialProgramService.saveAndLogSpecialPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		} catch (JsonProcessingException e1) {
			e1.printStackTrace();
		}
		try {
			AlgorithmResponse response = graduationService.graduateStudent(studentID,accessToken,projectedType);
			assertNotNull(response);
		} catch (GradBusinessRuleException e) {
			List<String> errors = validation.getErrors();
			assertEquals(1, errors.size());
			return;
		}
		
	}
	
	@Test
	public void testGraduateStudent_withProjectedTypeFMR() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="FMR";
		String accessToken="accessToken";
		
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setStudentID(new UUID(1, 1));
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate("2021-09-01");
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("A");
		
		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");
		
		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setStudentCourses(null);
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setSpecialProgramCode("BD");
		spgm.setSpecialProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<StudentOptionalProgram>();
		list.add(spgm);
		
		StudentDemographics sD = new StudentDemographics();
		sD.setPen("123123123");
		sD.setLegalFirstName("ABC");
		sD.setLegalLastName("FDG");
		sD.setLegalMiddleNames("DER");
		sD.setSchoolOfRecord("12321321");
		
		GraduationMessages gM = new GraduationMessages();
		gM.setGradMessage("asdad");
		
		School schoolObj = new School();
		schoolObj.setMinCode("1231123");
		schoolObj.setIndependentDesignation("2");
		
		ReportData data = new ReportData();
		data.setGradMessage("ABC");
		
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.prepareGraduationStatusObj(graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareReportData(graduationDataStatus,gradResponse,accessToken)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID, accessToken,gradResponse)).thenReturn(gradResponse);
		try {
			Mockito.when(specialProgramService.saveAndLogSpecialPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		} catch (JsonProcessingException e1) {
			e1.printStackTrace();
		}
		try {
			AlgorithmResponse response = graduationService.graduateStudent(studentID,accessToken,projectedType);
			assertNotNull(response);
		} catch (GradBusinessRuleException e) {
			List<String> errors = validation.getErrors();
			assertEquals(1, errors.size());
			return;
		}
		
	}
	
	@Test
	public void testGraduateStudent_withProjectedTypeFMR_programcompletionDate_notnull() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="GS";
		String accessToken="accessToken";
		validation.clear();
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setStudentID(new UUID(1, 1));
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate("2021-09-01");
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("A");
		
		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");
		
		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setStudentCourses(null);
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setSpecialProgramCode("BD");
		spgm.setSpecialProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<StudentOptionalProgram>();
		list.add(spgm);
		
		StudentDemographics sD = new StudentDemographics();
		sD.setPen("123123123");
		sD.setLegalFirstName("ABC");
		sD.setLegalLastName("FDG");
		sD.setLegalMiddleNames("DER");
		sD.setSchoolOfRecord("12321321");
		
		GraduationMessages gM = new GraduationMessages();
		gM.setGradMessage("asdad");
		
		School schoolObj = new School();
		schoolObj.setMinCode("1231123");
		schoolObj.setIndependentDesignation("2");
		
		ReportData data = new ReportData();
		data.setGradMessage("ABC");
		
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken)).thenReturn(gradResponse);
		try {
			AlgorithmResponse response = graduationService.graduateStudent(studentID,accessToken,projectedType);
			assertNotNull(response);
		} catch (GradBusinessRuleException e) {
			List<String> errors = validation.getErrors();
			assertEquals(1, errors.size());
			return;
		}
		
	}
	
	@Test
	public void testGraduateStudent_withProjectedTypeFMR_programCompletionDate_notnull_program_sccp() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="GS";
		String accessToken="accessToken";
		
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setStudentID(new UUID(1, 1));
		gradResponse.setProgram("SCCP");
		gradResponse.setProgramCompletionDate("2021-09-01");
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("A");
		
		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");
		
		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setStudentCourses(null);
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setSpecialProgramCode("BD");
		spgm.setSpecialProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<StudentOptionalProgram>();
		list.add(spgm);
		
		StudentDemographics sD = new StudentDemographics();
		sD.setPen("123123123");
		sD.setLegalFirstName("ABC");
		sD.setLegalLastName("FDG");
		sD.setLegalMiddleNames("DER");
		sD.setSchoolOfRecord("12321321");
		
		GraduationMessages gM = new GraduationMessages();
		gM.setGradMessage("asdad");
		
		School schoolObj = new School();
		schoolObj.setMinCode("1231123");
		schoolObj.setIndependentDesignation("2");
		
		ReportData data = new ReportData();
		data.setGradMessage("ABC");
		
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.prepareGraduationStatusObj(graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareReportData(graduationDataStatus,gradResponse,accessToken)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID, accessToken,gradResponse)).thenReturn(gradResponse);
		try {
			Mockito.when(specialProgramService.saveAndLogSpecialPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		} catch (JsonProcessingException e1) {
			e1.printStackTrace();
		}
		AlgorithmResponse response = graduationService.graduateStudent(studentID,accessToken,projectedType);
		assertNotNull(response);
	}
	
	@Test
	public void testGraduateStudent_withProjectedTypeFMR_programCompletionDate_null_program_sccp() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="GS";
		String accessToken="accessToken";
		
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setStudentID(new UUID(1, 1));
		gradResponse.setProgram("SCCP");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("A");
		
		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");
		
		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setStudentCourses(null);
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setSpecialProgramCode("BD");
		spgm.setSpecialProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<StudentOptionalProgram>();
		list.add(spgm);
		
		StudentDemographics sD = new StudentDemographics();
		sD.setPen("123123123");
		sD.setLegalFirstName("ABC");
		sD.setLegalLastName("FDG");
		sD.setLegalMiddleNames("DER");
		sD.setSchoolOfRecord("12321321");
		
		GraduationMessages gM = new GraduationMessages();
		gM.setGradMessage("asdad");
		
		School schoolObj = new School();
		schoolObj.setMinCode("1231123");
		schoolObj.setIndependentDesignation("2");
		
		ReportData data = new ReportData();
		data.setGradMessage("ABC");
		
		
		
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.prepareGraduationStatusObj(graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareReportData(graduationDataStatus,gradResponse,accessToken)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID, accessToken,gradResponse)).thenReturn(gradResponse);
		try {
			Mockito.when(specialProgramService.saveAndLogSpecialPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		} catch (JsonProcessingException e1) {
			e1.printStackTrace();
		}
		AlgorithmResponse response = graduationService.graduateStudent(studentID,accessToken,projectedType);
		assertNotNull(response);
	}
	
	@Test
	public void testGraduateStudent_withProjectedTypeFMR_Graduated() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="GS";
		String accessToken="accessToken";
		
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setStudentID(new UUID(1, 1));
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("A");
		
		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");
		
		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(true);
		graduationDataStatus.setStudentCourses(null);
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setSpecialProgramCode("BD");
		spgm.setSpecialProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<StudentOptionalProgram>();
		list.add(spgm);
		
		StudentDemographics sD = new StudentDemographics();
		sD.setPen("123123123");
		sD.setLegalFirstName("ABC");
		sD.setLegalLastName("FDG");
		sD.setLegalMiddleNames("DER");
		sD.setSchoolOfRecord("12321321");
		
		GraduationMessages gM = new GraduationMessages();
		gM.setGradMessage("asdad");
		
		School schoolObj = new School();
		schoolObj.setMinCode("1231123");
		schoolObj.setIndependentDesignation("2");
		
		ReportData data = new ReportData();
		data.setGradMessage("ABC");
		
		List<ProgramCertificate> certificateList = new ArrayList<ProgramCertificate>();
		ProgramCertificate pc= new ProgramCertificate();
		pc.setCertificateTypeCode("E");
		certificateList.add(pc);
		
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.prepareGraduationStatusObj(graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareReportData(graduationDataStatus,gradResponse,accessToken)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID, accessToken,gradResponse)).thenReturn(gradResponse);
		Mockito.when(reportService.getCertificateList(gradResponse,graduationDataStatus,list,accessToken)).thenReturn(certificateList);
		doNothing().when(reportService).saveStudentCertificateReportJasper(gradResponse,graduationDataStatus,accessToken,pc);
		try {
			Mockito.when(specialProgramService.saveAndLogSpecialPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		} catch (JsonProcessingException e1) {
			e1.printStackTrace();
		}
		AlgorithmResponse response = graduationService.graduateStudent(studentID,accessToken,projectedType);
		assertNotNull(response);
	}
}
