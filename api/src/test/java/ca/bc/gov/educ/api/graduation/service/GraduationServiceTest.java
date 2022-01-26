package ca.bc.gov.educ.api.graduation.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ca.bc.gov.educ.api.graduation.model.report.Student;
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
import ca.bc.gov.educ.api.graduation.model.dto.AlgorithmResponse;
import ca.bc.gov.educ.api.graduation.model.dto.ExceptionMessage;
import ca.bc.gov.educ.api.graduation.model.dto.GradAlgorithmGraduationStudentRecord;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationData;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationMessages;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationStudentRecord;
import ca.bc.gov.educ.api.graduation.model.dto.ProgramCertificateTranscript;
import ca.bc.gov.educ.api.graduation.model.dto.School;
import ca.bc.gov.educ.api.graduation.model.dto.StudentAssessment;
import ca.bc.gov.educ.api.graduation.model.dto.StudentAssessments;
import ca.bc.gov.educ.api.graduation.model.dto.StudentCourse;
import ca.bc.gov.educ.api.graduation.model.dto.StudentCourses;
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
	
	@Autowired
	private ExceptionMessage exception;
	
	@MockBean
	private GradStatusService gradStatusService;
	
	@MockBean
	private GradAlgorithmService gradAlgorithmService;
	
	@MockBean
	private OptionalProgramService optionalProgramService;
	
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
		String pen="4334";
		exception = new ExceptionMessage();		
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setStudentID(new UUID(1, 1));
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("CUR");
		
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
		StudentCourses sc = new StudentCourses();
		sc.setStudentCourseList(new ArrayList<StudentCourse>());
		graduationDataStatus.setStudentCourses(sc);
		
		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<StudentAssessment>());;
		graduationDataStatus.setStudentAssessments(sA);
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<StudentOptionalProgram>();
		list.add(spgm);

		ReportData data = new ReportData();
		data.setOrgCode("BC");
		Student std = new Student();
		std.setFirstName("Sreepad");
		data.setStudent(std);
		Mockito.when(gradStatusService.saveStudentRecordProjectedRun(studentID, null, accessToken, exception)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareAchievementReportData(graduationDataStatus, list)).thenReturn(data);
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken,exception)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runProjectedAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.processProjectedResults(gradResponse,graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(optionalProgramService.projectedOptionalPrograms(graduationDataStatus, studentID, accessToken)).thenReturn(list);

		AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
		assertNotNull(response);
	}
	
	@Test
	public void testGraduateStudent_error() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="REGFM";
		String accessToken="accessToken";
		validation.clear();
		exception = new ExceptionMessage();	
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
		StudentCourses sc = new StudentCourses();
		sc.setStudentCourseList(new ArrayList<StudentCourse>());
		graduationDataStatus.setStudentCourses(sc);
		
		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<StudentAssessment>());;
		graduationDataStatus.setStudentAssessments(sA);
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<StudentOptionalProgram>();
		list.add(spgm);

		ReportData data = new ReportData();
		data.setOrgCode("BC");
		Student std = new Student();
		std.setFirstName("Sreepad");
		data.setStudent(std);
		Mockito.when(gradStatusService.saveStudentRecordProjectedRun(studentID, null, accessToken, exception)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareAchievementReportData(graduationDataStatus, list)).thenReturn(data);
		
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken,exception)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runProjectedAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.processProjectedResults(gradResponse,graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(optionalProgramService.projectedOptionalPrograms(graduationDataStatus, studentID, accessToken)).thenReturn(list);

		try {
			graduationService.graduateStudent(studentID,null,accessToken,projectedType);
		} catch (GradBusinessRuleException e) {
			List<String> errors = validation.getErrors();
			assertEquals(0, errors.size());
			return;
		}
	}
	
	@Test
	public void testGraduateStudent_error2() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="REGFM";
		String accessToken="accessToken";
		validation.clear();
		exception = new ExceptionMessage();	
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setStudentID(new UUID(1, 1));
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("MER");

		ReportData data = new ReportData();
		data.setOrgCode("BC");
		Student std = new Student();
		std.setFirstName("Sreepad");
		data.setStudent(std);
		Mockito.when(gradStatusService.saveStudentRecordProjectedRun(studentID, null, accessToken, exception)).thenReturn(gradResponse);
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken,exception)).thenReturn(gradResponse);
		try {
			graduationService.graduateStudent(studentID,null,accessToken,projectedType);
		} catch (GradBusinessRuleException e) {
			List<String> errors = validation.getErrors();
			assertEquals(0, errors.size());
			return;
		}
	}
	
	@Test
	public void testGraduateStudent_withProjectedTypeFM() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="FM";
		String accessToken="accessToken";
		exception = new ExceptionMessage();	
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
		StudentCourses sc = new StudentCourses();
		sc.setStudentCourseList(new ArrayList<StudentCourse>());
		graduationDataStatus.setStudentCourses(sc);
		
		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<StudentAssessment>());;
		graduationDataStatus.setStudentAssessments(sA);
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<StudentOptionalProgram>();
		list.add(spgm);
		
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken,exception)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runProjectedAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken)).thenReturn(graduationDataStatus);
		Mockito.when(optionalProgramService.projectedOptionalPrograms(graduationDataStatus, studentID, accessToken)).thenReturn(list);

		AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
		assertNotNull(response);
	}
	
	@Test
	public void testGraduateStudent_withProjectedTypeGS() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="GS";
		String accessToken="accessToken";
		exception = new ExceptionMessage();	
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
		StudentCourses sc = new StudentCourses();
		sc.setStudentCourseList(new ArrayList<StudentCourse>());
		graduationDataStatus.setStudentCourses(sc);
		
		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<StudentAssessment>());;
		graduationDataStatus.setStudentAssessments(sA);
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
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
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken,exception)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken,exception)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.prepareGraduationStatusObj(graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareReportData(graduationDataStatus,gradResponse,accessToken,exception)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID,null, accessToken,gradResponse,exception)).thenReturn(gradResponse);
		Mockito.when(optionalProgramService.saveAndLogOptionalPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
		assertNotNull(response);
	}
	
	@Test
	public void testGraduateStudent_withProjectedTypeGS_programcompletionDate_notnull() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="GS";
		String accessToken="accessToken";
		validation.clear();
		exception = new ExceptionMessage();	
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
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
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
		
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken,exception)).thenReturn(gradResponse);
		try {
			AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
			assertNotNull(response);
		} catch (GradBusinessRuleException e) {
			List<String> errors = validation.getErrors();
			assertEquals(0, errors.size());
			return;
		}
		
	}
	
	@Test
	public void testGraduateStudent_withProjectedTypeGS_programCompletionDate_notnull_program_sccp() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="GS";
		String accessToken="accessToken";
		exception = new ExceptionMessage();	
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
		StudentCourses sc = new StudentCourses();
		sc.setStudentCourseList(new ArrayList<StudentCourse>());
		graduationDataStatus.setStudentCourses(sc);
		
		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<StudentAssessment>());;
		graduationDataStatus.setStudentAssessments(sA);
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
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
		
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken,exception)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken,exception)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.prepareGraduationStatusObj(graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareReportData(graduationDataStatus,gradResponse,accessToken,exception)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID,null, accessToken,gradResponse,exception)).thenReturn(gradResponse);
		Mockito.when(optionalProgramService.saveAndLogOptionalPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
		assertNotNull(response);
	}
	
	@Test
	public void testGraduateStudent_withProjectedTypeGS_programCompletionDate_null_program_sccp() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="GS";
		String accessToken="accessToken";
		exception = new ExceptionMessage();	
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
		StudentCourses sc = new StudentCourses();
		sc.setStudentCourseList(new ArrayList<StudentCourse>());
		graduationDataStatus.setStudentCourses(sc);
		
		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<StudentAssessment>());;
		graduationDataStatus.setStudentAssessments(sA);
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
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
		
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken,exception)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken,exception)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.prepareGraduationStatusObj(graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareReportData(graduationDataStatus,gradResponse,accessToken,exception)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID,null, accessToken,gradResponse,exception)).thenReturn(gradResponse);
		Mockito.when(optionalProgramService.saveAndLogOptionalPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
		assertNotNull(response);
	}
	
	@Test
	public void testGraduateStudent_withProjectedTypeGS_Graduated() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="GS";
		String accessToken="accessToken";
		exception = new ExceptionMessage();	
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
		StudentCourses sc = new StudentCourses();
		sc.setStudentCourseList(new ArrayList<StudentCourse>());
		graduationDataStatus.setStudentCourses(sc);
		
		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<StudentAssessment>());;
		graduationDataStatus.setStudentAssessments(sA);
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
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
		
		List<ProgramCertificateTranscript> certificateList = new ArrayList<ProgramCertificateTranscript>();
		ProgramCertificateTranscript pc= new ProgramCertificateTranscript();
		pc.setCertificateTypeCode("E");
		certificateList.add(pc);
		
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken,exception)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken,exception)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.prepareGraduationStatusObj(graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareReportData(graduationDataStatus,gradResponse,accessToken,exception)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID,null, accessToken,gradResponse,exception)).thenReturn(gradResponse);
		Mockito.when(reportService.getCertificateList(gradResponse,graduationDataStatus,list,accessToken,exception)).thenReturn(certificateList);
		doNothing().when(reportService).saveStudentCertificateReportJasper(gradResponse,graduationDataStatus,accessToken,pc,exception);
		Mockito.when(optionalProgramService.saveAndLogOptionalPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
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
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
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
		
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken,exception)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken,exception)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.prepareGraduationStatusObj(graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareReportData(graduationDataStatus,gradResponse,accessToken,exception)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID,null, accessToken,gradResponse,exception)).thenReturn(gradResponse);
		Mockito.when(optionalProgramService.saveAndLogOptionalPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		try {
			AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
			assertNotNull(response);
		} catch (GradBusinessRuleException e) {
			List<String> errors = validation.getErrors();
			assertEquals(0, errors.size());
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
		StudentCourses sc = new StudentCourses();
		sc.setStudentCourseList(new ArrayList<StudentCourse>());
		graduationDataStatus.setStudentCourses(sc);
		
		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<StudentAssessment>());;
		graduationDataStatus.setStudentAssessments(sA);
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
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
		
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken,exception)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken,exception)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.prepareGraduationStatusObj(graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareReportData(graduationDataStatus,gradResponse,accessToken,exception)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID,null, accessToken,gradResponse,exception)).thenReturn(gradResponse);
		Mockito.when(optionalProgramService.saveAndLogOptionalPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		try {
			AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
			assertNotNull(response);
		} catch (GradBusinessRuleException e) {
			List<String> errors = validation.getErrors();
			assertEquals(0, errors.size());
			return;
		}
		
	}
	
	@Test
	public void testGraduateStudent_withProjectedTypeFMR_programcompletionDate_notnull() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="GS";
		String accessToken="accessToken";
		validation.clear();
		exception = new ExceptionMessage();	
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
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
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
		
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken,exception)).thenReturn(gradResponse);
		try {
			AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
			assertNotNull(response);
		} catch (GradBusinessRuleException e) {
			List<String> errors = validation.getErrors();
			assertEquals(0, errors.size());
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
		StudentCourses sc = new StudentCourses();
		sc.setStudentCourseList(new ArrayList<StudentCourse>());
		graduationDataStatus.setStudentCourses(sc);
		
		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<StudentAssessment>());;
		graduationDataStatus.setStudentAssessments(sA);
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
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
		
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken,exception)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken,exception)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.prepareGraduationStatusObj(graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareReportData(graduationDataStatus,gradResponse,accessToken,exception)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID,null, accessToken,gradResponse,exception)).thenReturn(gradResponse);
		Mockito.when(optionalProgramService.saveAndLogOptionalPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
		assertNotNull(response);
	}
	
	@Test
	public void testGraduateStudent_withProjectedTypeFMR_programCompletionDate_null_program_sccp() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="GS";
		String accessToken="accessToken";
		exception = new ExceptionMessage();	
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
		StudentCourses sc = new StudentCourses();
		sc.setStudentCourseList(new ArrayList<StudentCourse>());
		graduationDataStatus.setStudentCourses(sc);
		
		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<StudentAssessment>());;
		graduationDataStatus.setStudentAssessments(sA);
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
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
		
		
		
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken,exception)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken,exception)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.prepareGraduationStatusObj(graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareReportData(graduationDataStatus,gradResponse,accessToken,exception)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID,null, accessToken,gradResponse,exception)).thenReturn(gradResponse);
		Mockito.when(optionalProgramService.saveAndLogOptionalPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
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
		
		StudentCourses sc = new StudentCourses();
		sc.setStudentCourseList(new ArrayList<StudentCourse>());
		graduationDataStatus.setStudentCourses(sc);
		
		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<StudentAssessment>());;
		graduationDataStatus.setStudentAssessments(sA);
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
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
		
		List<ProgramCertificateTranscript> certificateList = new ArrayList<ProgramCertificateTranscript>();
		ProgramCertificateTranscript pc= new ProgramCertificateTranscript();
		pc.setCertificateTypeCode("E");
		certificateList.add(pc);
		
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken,exception)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken,exception)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.prepareGraduationStatusObj(graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareReportData(graduationDataStatus,gradResponse,accessToken,exception)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID,null, accessToken,gradResponse,exception)).thenReturn(gradResponse);
		Mockito.when(reportService.getCertificateList(gradResponse,graduationDataStatus,list,accessToken,exception)).thenReturn(certificateList);
		doNothing().when(reportService).saveStudentCertificateReportJasper(gradResponse,graduationDataStatus,accessToken,pc,exception);
		Mockito.when(optionalProgramService.saveAndLogOptionalPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
		assertNotNull(response);
	}
}
