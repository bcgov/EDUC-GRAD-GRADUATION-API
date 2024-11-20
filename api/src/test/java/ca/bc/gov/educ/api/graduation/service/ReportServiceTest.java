package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.exception.ServiceException;
import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.model.report.Code;
import ca.bc.gov.educ.api.graduation.model.report.ReportData;
import ca.bc.gov.educ.api.graduation.model.report.Transcript;
import ca.bc.gov.educ.api.graduation.model.report.TranscriptResult;
import ca.bc.gov.educ.api.graduation.util.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@IntegrationComponentScan
@EnableIntegration
public class ReportServiceTest {

	@Autowired
	private GraduationService graduationService;

	@Autowired
	private ReportService reportService;

	@Autowired
	JsonTransformer jsonTransformer;

	@Autowired
	private ExceptionMessage exception;

	@Autowired
	GradValidation validation;

	@MockBean
	TokenUtils tokenUtils;

	@MockBean
	RESTService restService;

	@MockBean
	WebClient webClient;

	@Autowired
	private EducGraduationApiConstants constants;

	@Before
	public void setUp() {
		openMocks(this);
	}

	@After
	public void tearDown() {

	}

	@Test
	public void testGetStudentsForSchoolYearEndReport() {
		List<ReportGradStudentData> gradStudentDataList = createStudentSchoolYearEndData("json/studentSchoolYearEndResponse.json");

		when(this.restService.get(constants.getSchoolYearEndStudents(), List.class, "accessToken")).thenReturn(gradStudentDataList);

		var result = reportService.getStudentsForSchoolYearEndReport("accessToken");
		assertNotNull(result);
	}

	@Test
	public void testGetStudentsForSchoolYearEndReportWithSchools() {
		List<ReportGradStudentData> gradStudentDataList = createStudentSchoolYearEndData("json/studentSchoolYearEndResponse.json");

		when(this.restService.post(constants.getSchoolYearEndStudents(), List.of("00502001"), List.class)).thenReturn(gradStudentDataList);

		var result = reportService.getStudentsForSchoolYearEndReport(List.of("00502001"));
		assertNotNull(result);
	}

	@Test
	public void testGetStudentsForSchoolYearEndNonGradReport() {
		List<ReportGradStudentData> gradStudentDataList = createStudentSchoolYearEndData("json/studentSchoolYearEndResponse.json");
		ParameterizedTypeReference<List<ReportGradStudentData>> reportGradStudentDataType = new ParameterizedTypeReference<>() {
		};

		when(this.restService.get(constants.getStudentNonGradReportData(), List.class)).thenReturn(gradStudentDataList);

		var result = reportService.getStudentsForSchoolNonGradYearEndReport();
		assertNotNull(result);
	}

	@Test
	public void testGetStudentsForSchoolYearEndNonGradReportWithMincode() {
		List<ReportGradStudentData> gradStudentDataList = createStudentSchoolYearEndData("json/studentSchoolYearEndResponse.json");

		when(this.restService.get(String.format(constants.getStudentNonGradReportDataMincode(), "02396738"), List.class)).thenReturn(gradStudentDataList);

		var result = reportService.getStudentsForSchoolNonGradYearEndReport("02396738");
		assertNotNull(result);
	}

	@Test
	public void testGetStudentsForSchoolReport() {
		List<ReportGradStudentData> gradStudentDataList = createStudentSchoolYearEndData("json/studentSchoolYearEndResponse.json");

		when(this.restService.get(constants.getSchoolStudents(), List.class)).thenReturn(gradStudentDataList);

		var result = reportService.getStudentsForSchoolReport();
		assertNotNull(result);
	}

	@Test
	public void testSaveStudentCertificateReport() {
		UUID studentID = new UUID(1, 1);
		ExceptionMessage exception = new ExceptionMessage();
		String accessToken = "accessToken";
		String pen="212321123";
		List<ProgramCertificateTranscript> certificateList = new ArrayList<ProgramCertificateTranscript>();
		ProgramCertificateTranscript pc= new ProgramCertificateTranscript();
		pc.setCertificateTypeCode("E");
		certificateList.add(pc);
		ReportData data = new ReportData();
		data.setGradMessage("ABC");

		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setStudentID(studentID);
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setHonoursStanding("Y");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");

		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(true);
		graduationDataStatus.setStudentCourses(null);

		GradSearchStudent stuObj = new GradSearchStudent();
		stuObj.setPen("123090109");
		stuObj.setLegalFirstName("TEST");
		stuObj.setLegalLastName("QA");
		stuObj.setSchoolOfRecord("06011033");
		graduationDataStatus.setGradStudent(stuObj);

		School school = new School();
		school.setMinCode("06011033");
		school.setSchoolName("Test School");
		school.setCity("Vancouver");
		school.setPostal("V6T 1T2");
		school.setProvCode("BC");
		graduationDataStatus.setSchool(school);

		GradStudentCertificates rep = new GradStudentCertificates();
		rep.setPen(pen);
		byte[] bytesSAR = RandomUtils.nextBytes(20);

		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setStudentID(studentID);
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate("2020/02");
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("A");
		gradResponse.setUpdateDate(LocalDateTime.now());

		ParameterizedTypeReference<List<StudentOptionalProgram>> optionalProgramsResponseType = new ParameterizedTypeReference<>() {
		};

		StudentOptionalProgram studentOptionalProgram = new StudentOptionalProgram();
		studentOptionalProgram.setOptionalProgramCode("AD");
		studentOptionalProgram.setOptionalProgramName("Advanced Placement");
		studentOptionalProgram.setStudentID(studentID);

		when(this.restService.get(String.format(constants.getStudentOptionalPrograms(), studentID), List.class)).thenReturn(List.of(studentOptionalProgram));


		GraduationProgramCode gP = new GraduationProgramCode();
		gP.setProgramCode("2018-EN");
		gP.setProgramName("2018 Graduation Program");

		when(this.restService.get(String.format(constants.getProgramNameEndpoint(),gradAlgorithmGraduationStatus.getProgram()), GraduationProgramCode.class)).thenReturn(gP);
		when(this.restService.get(constants.getCertificateReport(), byte[].class)).thenReturn(bytesSAR);
		when(this.restService.post(eq(String.format(constants.getUpdateGradStudentCertificate(),pen)), any(), eq(GradStudentCertificates.class))).thenReturn(rep);

		reportService.saveStudentCertificateReportJasper(gradResponse, graduationDataStatus, pc,false);
		assertThat(exception.getExceptionName()).isNull();
	}

	@Test
	public void testSaveStudentTranscriptReport() {
		String studentID = new UUID(1, 1).toString();
		String schoolId = "b69bc244-b93b-2a9f-d2b1-3d8ffae92866";
		String accessToken = "accessToken";
		String pen="212321123";
		boolean isGraduated= false;
		ReportData data = new ReportData();
		data.setGradMessage("ABC");
		Transcript transcript = new Transcript();
		Code code= new Code();
		code.setCode("BC1996-PUB");
		transcript.setTranscriptTypeCode(code);
		data.setTranscript(transcript);
		ExceptionMessage exception = new ExceptionMessage();
		GradStudentReports rep = new GradStudentReports();
		rep.setPen(pen);
		byte[] bytesSAR = RandomUtils.nextBytes(20);

		School schoolDetails = new School();
		schoolDetails.setSchoolId(schoolId);
		schoolDetails.setMinCode("09323027");
		schoolDetails.setSchoolCategoryCode("INDEPEN");
		schoolDetails.setSchoolCategoryLegacyCode("02");

		when(this.restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(), schoolId), School.class)).thenReturn(schoolDetails);

		when(this.restService.post(eq(constants.getTranscriptReport()), any(), eq(byte[].class))).thenReturn(bytesSAR);
		when(this.restService.post(eq(String.format(constants.getUpdateGradStudentTranscript(),isGraduated)), any(), eq(GradStudentReports.class))).thenReturn(rep);

		reportService.saveStudentTranscriptReportJasper(data, UUID.fromString(studentID),exception,isGraduated, false);
		assertThat(exception.getExceptionName()).isNull();
	}

	@Test
	public void testGetCertificateList_whenAPIisDown_throwsException() {
		String studentID = new UUID(1, 1).toString();
		String schoolId = "b69bc244-b93b-2a9f-d2b1-3d8ffae92866";
		String accessToken = "accessToken";
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setSchoolOfRecordId(UUID.fromString(schoolId));
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");

		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setSchoolOfRecordId(UUID.fromString(schoolId));
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");

		School schoolObj = new School();
		schoolObj.setSchoolId(schoolId);
		schoolObj.setMinCode("09323027");

		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setSchool(schoolObj);
		graduationDataStatus.setStudentCourses(null);

		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("DD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setProgramCode("2018-EN");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<StudentOptionalProgram>();
		list.add(spgm);

		School schoolDetails = new School();
		schoolDetails.setSchoolId(schoolId);
		schoolDetails.setMinCode("1123");
		schoolDetails.setSchoolCategoryCode("INDEPEN");
		schoolDetails.setSchoolCategoryLegacyCode("02");

		when(this.restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(), schoolId), School.class)).thenReturn(schoolDetails);

		when(this.restService.post(eq(constants.getCertList()), any(), eq(List.class))).thenThrow(new RuntimeException("Test - API is down"));

		var results = reportService.getCertificateList(gradResponse, graduationDataStatus, list,exception);
		assertNotNull(results);
		assertThat(results).isEmpty();
		assertNotNull(exception);
		assertThat(exception.getExceptionName()).isEqualTo("GRAD-GRADUATION-REPORT-API IS DOWN");
	}

	@Test
	public void testGetCertificateList() {
		String studentID = new UUID(1, 1).toString();
		String schoolId = "b69bc244-b93b-2a9f-d2b1-3d8ffae92866";
		String accessToken = "accessToken";
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
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

		School schoolObj = new School();
		schoolObj.setSchoolId(schoolId);
		schoolObj.setMinCode("09323027");

		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setSchool(schoolObj);
		graduationDataStatus.setStudentCourses(null);

		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("DD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setProgramCode("2018-EN");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<StudentOptionalProgram>();
		list.add(spgm);

		List<ProgramCertificateTranscript> clist= new ArrayList<ProgramCertificateTranscript>();
		ProgramCertificateTranscript pc = new ProgramCertificateTranscript();
		pc.setCertificateTypeCode("E");
		pc.setSchoolCategoryCode(" ");
		clist.add(pc);


		School schoolDetails = new School();
		schoolDetails.setMinCode("1123");
		schoolDetails.setSchoolCategoryCode("INDEPEN");
		schoolDetails.setSchoolCategoryLegacyCode("02");

		when(this.restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(), schoolId), School.class)).thenReturn(schoolDetails);

		when(this.restService.post(eq(constants.getCertList()), any(), eq(List.class))).thenReturn(clist);

		List<ProgramCertificateTranscript> listCC =reportService.getCertificateList(gradResponse, graduationDataStatus, list,exception);
		assertThat(listCC).hasSize(1);
	}

	@Test
	public void testGetCertificateList_PFProgram() {
		String studentID = new UUID(1, 1).toString();
		String schoolId = "b69bc244-b93b-2a9f-d2b1-3d8ffae92866";
		String accessToken = "accessToken";
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-PF");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setSchoolOfRecordId(UUID.fromString(schoolId));
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");

		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setSchoolOfRecordId(UUID.fromString(schoolId));
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");

		School schoolObj = new School();
		schoolObj.setSchoolId(schoolId);
		schoolObj.setMinCode("09323027");

		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setSchool(schoolObj);
		graduationDataStatus.setStudentCourses(null);
		graduationDataStatus.setDualDogwood(true);

		List<ProgramCertificateTranscript> clist= new ArrayList<ProgramCertificateTranscript>();
		ProgramCertificateTranscript pc = new ProgramCertificateTranscript();
		pc.setCertificateTypeCode("E");
		pc.setSchoolCategoryCode(" ");
		clist.add(pc);

		when(this.restService.post(eq(constants.getCertList()), any(), eq(List.class))).thenReturn(clist);

		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("DD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setProgramCode("2018-EN");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<StudentOptionalProgram>();
		list.add(spgm);

		School schoolDetails = new School();
		schoolDetails.setMinCode("1123");
		schoolDetails.setSchoolCategoryCode("INDEPEN");
		schoolDetails.setSchoolCategoryLegacyCode("02");

		when(this.restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(), schoolId), School.class)).thenReturn(schoolDetails);

		List<ProgramCertificateTranscript> listCC = reportService.getCertificateList(gradResponse, graduationDataStatus, list,exception);
		assertThat(listCC).hasSize(1);
	}

	@Test
	public void testGetCertificateList_PFProgram_nodogwood() {
		String studentID = new UUID(1, 1).toString();
		String schoolId = "b69bc244-b93b-2a9f-d2b1-3d8ffae92866";
		String accessToken = "accessToken";
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-PF");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setSchoolOfRecordId(UUID.fromString(schoolId));
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");

		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");

		School schoolObj = new School();
		schoolObj.setSchoolId(schoolId);
		schoolObj.setMinCode("09323027");

		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setSchool(schoolObj);
		graduationDataStatus.setStudentCourses(null);

		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("DD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setProgramCode("2018-EN");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<StudentOptionalProgram>();
		list.add(spgm);

		School schoolDetails = new School();
		schoolDetails.setMinCode("1123");
		schoolDetails.setSchoolCategoryCode("INDEPEN");
		schoolDetails.setSchoolCategoryLegacyCode("02");

		when(this.restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(), schoolId), School.class)).thenReturn(schoolDetails);

		List<ProgramCertificateTranscript> clist= new ArrayList<ProgramCertificateTranscript>();
		ProgramCertificateTranscript pc = new ProgramCertificateTranscript();
		pc.setCertificateTypeCode("E");
		pc.setSchoolCategoryCode(" ");
		clist.add(pc);

		when(this.restService.post(eq(constants.getCertList()), any(), eq(List.class))).thenReturn(clist);

		List<ProgramCertificateTranscript> listCC= reportService.getCertificateList(gradResponse, graduationDataStatus, list,exception);
		assertThat(listCC).hasSize(1);
	}

	@Test
	public void testGetCertificateList_emptyOptionalProgram() {
		UUID studentID = new UUID(1, 1);
		String schoolId = "b69bc244-b93b-2a9f-d2b1-3d8ffae92866";
		String accessToken = "accessToken";
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setStudentID(studentID);
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setSchoolOfRecordId(UUID.fromString(schoolId));
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");

		School schoolDetails = new School();
		schoolDetails.setMinCode("1123");
		schoolDetails.setSchoolCategoryCode("INDEPEN");
		schoolDetails.setSchoolCategoryLegacyCode("02");

		when(this.restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(), schoolId), School.class)).thenReturn(schoolDetails);

		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");

		School schoolObj = new School();
		schoolObj.setSchoolId(schoolId);
		schoolObj.setMinCode("09323027");

		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setSchool(schoolObj);
		graduationDataStatus.setStudentCourses(null);

		List<ProgramCertificateTranscript> clist= new ArrayList<ProgramCertificateTranscript>();
		ProgramCertificateTranscript pc = new ProgramCertificateTranscript();
		pc.setCertificateTypeCode("E");
		pc.setSchoolCategoryCode(" ");
		clist.add(pc);

		when(this.restService.post(eq(constants.getCertList()), any(), eq(List.class))).thenReturn(clist);

		List<StudentOptionalProgram> list = new ArrayList<StudentOptionalProgram>();
		List<ProgramCertificateTranscript> listCC = reportService.getCertificateList(gradResponse, graduationDataStatus, list,exception);
		assertThat(listCC).hasSize(1);
	}

	@Test
	public void testGetCertificateList_FrenchImmersion() {
		String studentID = new UUID(1, 1).toString();
		String schoolId = "b69bc244-b93b-2a9f-d2b1-3d8ffae92866";
		String accessToken = "accessToken";
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setSchoolOfRecordId(UUID.fromString(schoolId));
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");

		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");

		School schoolObj = new School();
		schoolObj.setSchoolId(schoolId);
		schoolObj.setMinCode("09323027");
		schoolObj.setSchoolId(UUID.randomUUID().toString());

		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setSchool(schoolObj);
		graduationDataStatus.setStudentCourses(null);

		List<ProgramCertificateTranscript> clist= new ArrayList<ProgramCertificateTranscript>();
		ProgramCertificateTranscript pc = new ProgramCertificateTranscript();
		pc.setCertificateTypeCode("E");
		pc.setSchoolCategoryCode(" ");
		clist.add(pc);

		when(this.restService.post(eq(constants.getCertList()), any(), eq(List.class))).thenReturn(clist);


		School schoolDetails = new School();
		schoolDetails.setMinCode("1123");
		schoolDetails.setSchoolCategoryCode("INDEPEN");
		schoolDetails.setSchoolCategoryLegacyCode("02");

		when(this.restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(), schoolId), School.class)).thenReturn(schoolDetails);

		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("FI");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setProgramCode("2018-EN");
		spgm.setStudentID(UUID.fromString(studentID));
		spgm.setOptionalProgramCompletionDate("2020-09-01");
		List<StudentOptionalProgram> list = new ArrayList<StudentOptionalProgram>();
		list.add(spgm);
		List<ProgramCertificateTranscript> listCC =  reportService.getCertificateList(gradResponse, graduationDataStatus, list,exception);
		assertThat(listCC).hasSize(1);
	}

	@Test
	public void testGetCertificateList_FrenchImmersion_nullProgramCompletionDate() {
		String studentID = new UUID(1, 1).toString();
		String schoolId = "b69bc244-b93b-2a9f-d2b1-3d8ffae92866";
		String accessToken = "accessToken";
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setSchoolOfRecordId(UUID.fromString(schoolId));
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");

		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setSchoolOfRecordId(UUID.fromString(schoolId));
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");

		School schoolObj = new School();
		schoolObj.setSchoolId(schoolId);
		schoolObj.setMinCode("09323027");
		schoolObj.setSchoolId(UUID.randomUUID().toString());

		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setSchool(schoolObj);
		graduationDataStatus.setStudentCourses(null);

		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("FI");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setProgramCode("2018-EN");
		spgm.setStudentID(UUID.fromString(studentID));
		spgm.setOptionalProgramCompletionDate(null);
		List<StudentOptionalProgram> list = new ArrayList<StudentOptionalProgram>();
		list.add(spgm);

		List<ProgramCertificateTranscript> clist= new ArrayList<ProgramCertificateTranscript>();
		ProgramCertificateTranscript pc = new ProgramCertificateTranscript();
		pc.setCertificateTypeCode("E");
		pc.setSchoolCategoryCode("02");
		clist.add(pc);

		when(this.restService.post(eq(constants.getCertList()), any(), eq(List.class))).thenReturn(clist);

		School schoolDetails = new School();
		schoolDetails.setMinCode("1123");
		schoolDetails.setSchoolCategoryCode("INDEPEN");
		schoolDetails.setSchoolCategoryLegacyCode("02");

		when(this.restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(), schoolId), School.class)).thenReturn(schoolDetails);

		List<ProgramCertificateTranscript> listCC = reportService.getCertificateList(gradResponse, graduationDataStatus, list,exception);
		assertThat(listCC).hasSize(1);
	}

	@Test
	public void testPrepareReportData() {
		String accessToken = "accessToken";
		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");

		School schoolObj = new School();
		schoolObj.setMinCode("09323027");
		schoolObj.setSchoolId(UUID.randomUUID().toString());

		GradSearchStudent stuObj = new GradSearchStudent();
		stuObj.setPen("123123123");
		stuObj.setLegalFirstName("ABC");
		stuObj.setLegalLastName("FDG");
		stuObj.setSchoolOfRecord("12321321");

		StudentCourse sc= new StudentCourse();
		sc.setCourseCode("FDFE");
		sc.setCourseName("DERQ WEW");
		sc.setCreditsUsedForGrad(4);
		sc.setCredits(4);
		sc.setCustomizedCourseName("SREE");
		sc.setSessionDate("1990/12");
		sc.setEquivOrChallenge("E");
		sc.setProvExamCourse("Y");
		sc.setSpecialCase("A");
		sc.setCourseLevel("11");
		sc.setBestExamPercent(60.8D);
		sc.setBestSchoolPercent(90.3D);
		List<StudentCourse> sList= new ArrayList<>();
		sList.add(sc);
		StudentCourses sCourses = new StudentCourses();
		sCourses.setStudentCourseList(sList);

		StudentAssessment sA= new StudentAssessment();
		sA.setAssessmentCode("FDFE");
		sA.setAssessmentName("AASASA");
		sA.setSessionDate("2020/12");
		sA.setSpecialCase("A");
		List<StudentAssessment> aList= new ArrayList<>();
		aList.add(sA);
		StudentAssessments sAssessments = new StudentAssessments();
		sAssessments.setStudentAssessmentList(aList);

		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setSchool(schoolObj);
		graduationDataStatus.setStudentCourses(sCourses);
		graduationDataStatus.setStudentAssessments(sAssessments);
		graduationDataStatus.setGradStudent(stuObj);

		GraduationProgramCode gP = new GraduationProgramCode();
		gP.setProgramCode("2018-EN");
		gP.setProgramName("2018 Graduation Program");

		SpecialCase sp = new SpecialCase();
		sp.setSpCase("A");
		sp.setLabel("AEG");

		when(this.restService.get(String.format(constants.getSpecialCase(),"A"), SpecialCase.class)).thenReturn(sp);
		when(this.restService.get(String.format(constants.getProgramNameEndpoint(),gradAlgorithmGraduationStatus.getProgram()), GraduationProgramCode.class)).thenReturn(gP);


		School schoolDetails = new School();
		schoolDetails.setMinCode("1123");
		schoolDetails.setSchoolCategoryCode("INDEPEN");
		schoolDetails.setSchoolCategoryLegacyCode("02");

		when(this.restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(), "06011033"), School.class)).thenReturn(schoolDetails);

		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");
		gradResponse.setUpdateDate(LocalDateTime.now());

		List<CodeDTO> optionalProgram = new ArrayList<CodeDTO>();
		CodeDTO cDto = new CodeDTO();
		cDto.setCode("FI");
		cDto.setName("French Immersion");
		optionalProgram.add(cDto);

		ReportData dta = reportService.prepareTranscriptData(graduationDataStatus,gradResponse,false,exception);
		assertThat(dta).isNotNull();
	}

	private GradAlgorithmGraduationStudentRecord getGradAlgorithmGraduationStatus(String program) {
		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram(program);
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");
		return gradAlgorithmGraduationStatus;
	}

	private GradSearchStudent getStudentObj () {
		GradSearchStudent stuObj = new GradSearchStudent();
		stuObj.setPen("123123123");
		stuObj.setLegalFirstName("ABC");
		stuObj.setLegalLastName("FDG");
		stuObj.setSchoolOfRecord("12321321");
		return stuObj;
	}

	private List<StudentCourse> getStudentCourses(int totalCredits,int originalCredits) {
		StudentCourse sc= new StudentCourse();
		sc.setCourseCode("FDFE FE");
		sc.setCourseName("FEREE FREE");
		sc.setCourseLevel("11");
		sc.setCredits(totalCredits);
		sc.setOriginalCredits(originalCredits);
		sc.setCreditsUsedForGrad(2);
		sc.setCustomizedCourseName("SREE");
		sc.setSessionDate("2020/12");
		sc.setFineArtsAppliedSkills("B");
		sc.setEquivOrChallenge("E");
		sc.setSpecialCase("F");
		sc.setRestricted(true);
		sc.setProvExamCourse("N");
		sc.setBestExamPercent(50.0D);
		sc.setBestSchoolPercent(50.0D);
		List<StudentCourse> sList= new ArrayList<>();
		sList.add(sc);
		sc= new StudentCourse();
		sc.setCourseCode("CPUY");
		sc.setCourseName("CP FEREE FREE");
		sc.setCourseLevel("12A");
		sc.setCredits(totalCredits);
		sc.setOriginalCredits(originalCredits);
		sc.setCreditsUsedForGrad(2);
		sc.setCustomizedCourseName("SREE");
		sc.setSessionDate("2020/12");
		sc.setFineArtsAppliedSkills("B");
		sc.setEquivOrChallenge("E");
		sc.setSpecialCase("F");
		sc.setProvExamCourse("Y");
		sc.setRestricted(true);
		sList.add(sc);
		sc= new StudentCourse();
		sc.setCourseCode("CPUY FR");
		sc.setCourseName("CP FEREE FREE");
		sc.setCourseLevel("12A");
		sc.setCredits(totalCredits);
		sc.setSessionDate("1990/11");
		sc.setOriginalCredits(originalCredits);
		sc.setCreditsUsedForGrad(2);
		sc.setBestSchoolPercent(89D);
		sc.setCustomizedCourseName("SREE");
		sc.setSessionDate("2020/12");
		sc.setFineArtsAppliedSkills("B");
		sc.setEquivOrChallenge("E");
		sc.setSpecialCase("F");
		sc.setProvExamCourse("Y");
		sc.setRestricted(true);
		sList.add(sc);
		sc= new StudentCourse();
		sc.setCourseCode("CPUY ZS");
		sc.setCourseName("CP FEREE FREE");
		sc.setCourseLevel("12C");
		sc.setCredits(totalCredits);
		sc.setOriginalCredits(originalCredits);
		sc.setCreditsUsedForGrad(2);
		sc.setBestSchoolPercent(89D);
		sc.setCustomizedCourseName("SREE");
		sc.setSessionDate("2020/12");
		sc.setFineArtsAppliedSkills("B");
		sc.setEquivOrChallenge("E");
		sc.setSpecialCase("F");
		sc.setProvExamCourse("Y");
		sc.setRestricted(true);
		sList.add(sc);
		return sList;
	}

	private List<StudentAssessment> getStudentAssessments() {
		StudentAssessment sA= new StudentAssessment();
		sA.setAssessmentCode("FDFE");
		sA.setAssessmentName("AASASA");
		sA.setSessionDate("2020/12");
		sA.setSpecialCase("A");
		List<StudentAssessment> aList= new ArrayList<>();
		aList.add(sA);
		return aList;
	}

	@Test
	public void testPrepareReportData_nullProgramData() {
		String accessToken = "accessToken";
		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = getGradAlgorithmGraduationStatus("2018-EN");

		School schoolObj = new School();
		schoolObj.setMinCode("09323027");
		schoolObj.setSchoolId(UUID.randomUUID().toString());

		GradSearchStudent stuObj = getStudentObj();
		StudentCourses sCourses = new StudentCourses();
		sCourses.setStudentCourseList(getStudentCourses(4,4));


		StudentAssessments sAssessments = new StudentAssessments();
		sAssessments.setStudentAssessmentList(getStudentAssessments());

		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setSchool(schoolObj);
		graduationDataStatus.setStudentCourses(sCourses);
		graduationDataStatus.setStudentAssessments(sAssessments);
		graduationDataStatus.setGradStudent(stuObj);

		GraduationProgramCode gP = new GraduationProgramCode();
		gP.setProgramCode("2018-EN");
		gP.setProgramName("2018 Graduation Program");

		SpecialCase sp = new SpecialCase();
		sp.setSpCase("A");
		sp.setLabel("AEG");

		when(this.restService.get(String.format(constants.getSpecialCase(),"A"), SpecialCase.class)).thenReturn(sp);
		when(this.restService.get(String.format(constants.getProgramNameEndpoint(),gradAlgorithmGraduationStatus.getProgram()), GraduationProgramCode.class)).thenReturn(gP);

		School schoolDetails = new School();
		schoolDetails.setMinCode("09323027");
		schoolDetails.setSchoolCategoryCode("INDEPEN");
		schoolDetails.setSchoolCategoryLegacyCode("02");

		when(this.restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(), "06011033"), School.class)).thenReturn(schoolDetails);

		List<CodeDTO> optionalProgram = new ArrayList<CodeDTO>();
		CodeDTO cDto = new CodeDTO();
		cDto.setCode("FI");
		cDto.setName("French Immersion");
		optionalProgram.add(cDto);

		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");
		gradResponse.setUpdateDate(LocalDateTime.now());

		ReportData dta = reportService.prepareTranscriptData(graduationDataStatus,gradResponse,false,exception);
		assertThat(dta).isNotNull();
	}

	@Test
	public void testPrepareReportData_exams_notnull() {
		String accessToken = "accessToken";
		testPrepareReportData_exams_notnull("1996-EN",accessToken);
		testPrepareReportData_exams_notnull("1950",accessToken);
		testPrepareReportData_exams_notnull("2004-EN",accessToken);
	}


	public void testPrepareReportData_exams_notnull(String program,String accessToken) {

		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = getGradAlgorithmGraduationStatus(program);

		School schoolObj = new School();
		schoolObj.setMinCode("09323027");
		schoolObj.setSchoolId(UUID.randomUUID().toString());

		GradSearchStudent stuObj = getStudentObj();

		StudentCourses sCourses = new StudentCourses();
		sCourses.setStudentCourseList(getStudentCourses(2,4));

		StudentExam se= new StudentExam();
		se.setCourseCode("FDFE");
		List<StudentExam> eList= new ArrayList<>();
		eList.add(se);
		StudentExams eCourses = new StudentExams();
		eCourses.setStudentExamList(eList);

		StudentAssessments sAssessments = new StudentAssessments();
		sAssessments.setStudentAssessmentList(getStudentAssessments());

		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setSchool(schoolObj);
		graduationDataStatus.setStudentCourses(sCourses);
		graduationDataStatus.setStudentAssessments(sAssessments);
		graduationDataStatus.setStudentExams(eCourses);
		graduationDataStatus.setGradStudent(stuObj);

		GraduationProgramCode gP = new GraduationProgramCode();
		gP.setProgramCode(program);
		gP.setProgramName("2018 Graduation Program");

		SpecialCase sp = new SpecialCase();
		sp.setSpCase("A");
		sp.setLabel("AEG");

		when(this.restService.get(String.format(constants.getSpecialCase(),"A"), SpecialCase.class)).thenReturn(sp);
		when(this.restService.get(String.format(constants.getProgramNameEndpoint(),gradAlgorithmGraduationStatus.getProgram()), GraduationProgramCode.class)).thenReturn(gP);

		School schoolDetails = new School();
		schoolDetails.setMinCode("06011033");
		schoolDetails.setSchoolCategoryCode("INDEPEN");
		schoolDetails.setSchoolCategoryLegacyCode("02");

		when(this.restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(), "06011033"), School.class)).thenReturn(schoolDetails);

		List<CodeDTO> optionalProgram = new ArrayList<CodeDTO>();
		CodeDTO cDto = new CodeDTO();
		cDto.setCode("FI");
		cDto.setName("French Immersion");
		optionalProgram.add(cDto);

		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setProgram(program);
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");
		gradResponse.setUpdateDate(LocalDateTime.now());
		ReportData dta = reportService.prepareTranscriptData(graduationDataStatus,gradResponse,false,exception);
		assertThat(dta).isNotNull();
	}

	@Test
	public void testPrepareReportData_Desig_3() {
		String accessToken = "accessToken";
		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");

		School schoolObj = new School();
		schoolObj.setMinCode("09323027");
		schoolObj.setSchoolId(UUID.randomUUID().toString());

		GradSearchStudent stuObj = new GradSearchStudent();
		stuObj.setPen("123123123");
		stuObj.setLegalFirstName("ABC");
		stuObj.setLegalLastName("FDG");
		stuObj.setSchoolOfRecord("12321321");

		StudentCourse sc= new StudentCourse();
		sc.setCourseCode("FDFE");
		sc.setCourseName("FDFE FREE");
		sc.setCreditsUsedForGrad(4);
		sc.setCredits(4);
		sc.setProvExamCourse("Y");
		sc.setCustomizedCourseName("SREE");
		sc.setSessionDate("2020/12");
		sc.setEquivOrChallenge("E");
		sc.setCourseLevel("11");
		sc.setBestExamPercent(60.8D);
		sc.setBestSchoolPercent(90.3D);
		List<StudentCourse> sList= new ArrayList<>();
		sList.add(sc);
		StudentCourses sCourses = new StudentCourses();
		sCourses.setStudentCourseList(sList);

		StudentAssessment sA= new StudentAssessment();
		sA.setAssessmentCode("FDFE");
		sA.setAssessmentName("AASASA");
		sA.setSessionDate("2020/12");
		sA.setSpecialCase("A");
		List<StudentAssessment> aList= new ArrayList<>();
		aList.add(sA);
		StudentAssessments sAssessments = new StudentAssessments();
		sAssessments.setStudentAssessmentList(aList);

		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setSchool(schoolObj);
		graduationDataStatus.setStudentCourses(sCourses);
		graduationDataStatus.setStudentAssessments(sAssessments);		graduationDataStatus.setGradStudent(stuObj);

		SpecialCase sp = new SpecialCase();
		sp.setSpCase("A");
		sp.setLabel("AEG");

		when(this.restService.get(String.format(constants.getSpecialCase(),"A"), SpecialCase.class)).thenReturn(sp);

		GraduationProgramCode gP = new GraduationProgramCode();
		gP.setProgramCode("2018-EN");
		gP.setProgramName("2018 Graduation Program");

		when(this.restService.get(String.format(constants.getProgramNameEndpoint(),gradAlgorithmGraduationStatus.getProgram()), GraduationProgramCode.class)).thenReturn(gP);

		School schoolDetails = new School();
		schoolDetails.setMinCode("09323027");
		schoolDetails.setSchoolCategoryCode("INDEPEN");
		schoolDetails.setSchoolCategoryLegacyCode("02");

		when(this.restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(), "06011033"), School.class)).thenReturn(schoolDetails);

		List<CodeDTO> optionalProgram = new ArrayList<CodeDTO>();
		CodeDTO cDto = new CodeDTO();
		cDto.setCode("FI");
		cDto.setName("French Immersion");
		optionalProgram.add(cDto);

		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");
		gradResponse.setUpdateDate(LocalDateTime.now());

		ReportData dta = reportService.prepareTranscriptData(graduationDataStatus,gradResponse,false,exception);
		assertThat(dta).isNotNull();
	}

	@Test
	public void testPrepareReportData_Desig_4() {
		String accessToken = "accessToken";
		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");

		School schoolObj = new School();
		schoolObj.setMinCode("09323027");
		schoolObj.setSchoolId(UUID.randomUUID().toString());

		GradSearchStudent stuObj = new GradSearchStudent();
		stuObj.setPen("123123123");
		stuObj.setLegalFirstName("ABC");
		stuObj.setLegalLastName("FDG");
		stuObj.setSchoolOfRecord("12321321");

		StudentCourse sc= new StudentCourse();
		sc.setCourseCode("FDFE");
		sc.setCourseName("FDFE FREE");
		sc.setCreditsUsedForGrad(2);
		sc.setCredits(4);
		sc.setCustomizedCourseName("SREE");
		sc.setSessionDate("2020/12");
		sc.setEquivOrChallenge("E");
		sc.setSpecialCase("A");
		sc.setProvExamCourse("N");
		sc.setCourseLevel("11");
		sc.setBestExamPercent(60.8D);
		sc.setBestSchoolPercent(90.3D);
		List<StudentCourse> sList= new ArrayList<>();
		sList.add(sc);
		StudentCourses sCourses = new StudentCourses();
		sCourses.setStudentCourseList(sList);

		StudentAssessment sA= new StudentAssessment();
		sA.setAssessmentCode("FDFE");
		sA.setAssessmentName("AASASA");
		sA.setSessionDate("2020/12");
		sA.setSpecialCase("A");
		List<StudentAssessment> aList= new ArrayList<>();
		aList.add(sA);
		StudentAssessments sAssessments = new StudentAssessments();
		sAssessments.setStudentAssessmentList(aList);

		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setSchool(schoolObj);
		graduationDataStatus.setStudentCourses(sCourses);
		graduationDataStatus.setStudentAssessments(sAssessments);		graduationDataStatus.setGradStudent(stuObj);

		GraduationProgramCode gP = new GraduationProgramCode();
		gP.setProgramCode("2018-EN");
		gP.setProgramName("2018 Graduation Program");

		SpecialCase sp = new SpecialCase();
		sp.setSpCase("A");
		sp.setLabel("AEG");

		when(this.restService.get(String.format(constants.getSpecialCase(),"A"), SpecialCase.class)).thenReturn(sp);
		when(this.restService.get(String.format(constants.getProgramNameEndpoint(),gradAlgorithmGraduationStatus.getProgram()), GraduationProgramCode.class)).thenReturn(gP);

		School schoolDetails = new School();
		schoolDetails.setMinCode("09323027");
		schoolDetails.setSchoolCategoryCode("INDEPEN");
		schoolDetails.setSchoolCategoryLegacyCode("02");

		when(this.restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(), "06011033"), School.class)).thenReturn(schoolDetails);


		List<CodeDTO> optionalProgram = new ArrayList<CodeDTO>();
		CodeDTO cDto = new CodeDTO();
		cDto.setCode("FI");
		cDto.setName("French Immersion");
		optionalProgram.add(cDto);

		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");
		gradResponse.setUpdateDate(LocalDateTime.now());

		ReportData dta = reportService.prepareTranscriptData(graduationDataStatus,gradResponse,false,exception);
		assertThat(dta).isNotNull();
	}


	public void testPrepareReportData_Desig_2() {
		String accessToken = "accessToken";
		String schoolId = "b69bc244-b93b-2a9f-d2b1-3d8ffae92866";
		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");

		School schoolObj = new School();
		schoolObj.setSchoolId(schoolId);
		schoolObj.setMinCode("09323027");
		schoolObj.setSchoolId(UUID.randomUUID().toString());

		GradSearchStudent stuObj = new GradSearchStudent();
		stuObj.setPen("123123123");
		stuObj.setLegalFirstName("ABC");
		stuObj.setLegalLastName("FDG");
		stuObj.setSchoolOfRecord("12321321");

		StudentCourse sc= new StudentCourse();
		sc.setCourseCode("FDFE");
		sc.setCredits(4);
		sc.setCustomizedCourseName("SREE");
		sc.setSessionDate("2020/12");
		sc.setEquivOrChallenge("E");
		List<StudentCourse> sList= new ArrayList<>();
		sList.add(sc);
		StudentCourses sCourses = new StudentCourses();
		sCourses.setStudentCourseList(sList);

		StudentAssessment sA= new StudentAssessment();
		sA.setAssessmentCode("FDFE");
		sA.setAssessmentName("AASASA");
		sA.setSessionDate("2020/12");
		sA.setSpecialCase("A");
		List<StudentAssessment> aList= new ArrayList<>();
		aList.add(sA);
		StudentAssessments sAssessments = new StudentAssessments();
		sAssessments.setStudentAssessmentList(aList);

		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setSchool(schoolObj);
		graduationDataStatus.setStudentCourses(sCourses);
		graduationDataStatus.setStudentAssessments(sAssessments);
		graduationDataStatus.setGradStudent(stuObj);

		GraduationProgramCode gP = new GraduationProgramCode();
		gP.setProgramCode("2018-EN");
		gP.setProgramName("2018 Graduation Program");

		when(this.restService.get(String.format(constants.getProgramNameEndpoint(),gradAlgorithmGraduationStatus.getProgram()), GraduationProgramCode.class)).thenReturn(gP);


		List<CodeDTO> optionalProgram = new ArrayList<CodeDTO>();
		CodeDTO cDto = new CodeDTO();
		cDto.setCode("FI");
		cDto.setName("French Immersion");
		optionalProgram.add(cDto);

		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");
		gradResponse.setUpdateDate(LocalDateTime.now());

		ReportData dta = reportService.prepareTranscriptData(graduationDataStatus,gradResponse,false,exception);
		assertThat(dta).isNotNull();
	}

	@Test
	public void testIsGraduatedForSCCPWithNullDate() {
		String programCompletionDate = null; // null or empty
		String gradProgram = "SCCP";

		var result = reportService.isGraduated(programCompletionDate, gradProgram);
		assertThat(result).isFalse();
	}

	@Test
	public void testIsGraduatedForSCCPWithFutureDate() {
		String programCompletionDate = "2900/09"; // future date: 2900 Sept.
		String gradProgram = "SCCP";

		var result = reportService.isGraduated(programCompletionDate, gradProgram);
		assertThat(result).isFalse();
	}

	@Test
	public void testIsGraduatedForSCCPWithPastDate() {
		String programCompletionDate = "2002/09"; // past date: 2002 Sept.
		String gradProgram = "SCCP";

		var result = reportService.isGraduated(programCompletionDate, gradProgram);
		assertThat(result).isTrue();
	}

	@Test
	public void testIsGraduatedForSCCPWithFullDateFormat() {
		String programCompletionDate = "2002-09-01"; // past date: 2002 Sept. 01
		String gradProgram = "SCCP";

		var result = reportService.isGraduated(programCompletionDate, gradProgram);
		assertThat(result).isTrue();
	}

	@Test
	public void testIsGraduatedForSCCPWithWrongDateFormat() {
		String programCompletionDate = "2002/09/01"; // past date: 2002 Sept. 01
		String gradProgram = "SCCP";

		var result = reportService.isGraduated(programCompletionDate, gradProgram);
		assertThat(result).isFalse();
	}

	@Test
	public void testSaveStudentAchievementReport() throws Exception {
		String studentID = new UUID(1, 1).toString();
		String schoolId = "b69bc244-b93b-2a9f-d2b1-3d8ffae92866";
		String accessToken = "accessToken";
		String pen = "212321123";
		boolean isGraduated = false;
		ReportData data = createReportData("json/reportdataAchv.json");
		GradStudentReports rep = new GradStudentReports();
		rep.setPen(pen);
		byte[] bytesSAR = RandomUtils.nextBytes(20);
		ExceptionMessage exception = new ExceptionMessage();

		School schoolDetails = new School();
		schoolDetails.setSchoolId(schoolId);
		schoolDetails.setMinCode("09323027");
		schoolDetails.setSchoolCategoryCode("INDEPEN");
		schoolDetails.setSchoolCategoryLegacyCode("02");

		when(this.restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(), schoolId), School.class)).thenReturn(schoolDetails);
		when(this.restService.post(eq(constants.getAchievementReport()), any(), eq(byte[].class))).thenReturn(bytesSAR);
		when(this.restService.post(eq(String.format(constants.getUpdateGradStudentReport(),isGraduated)), any(), eq(GradStudentReports.class))).thenReturn(rep);

		reportService.saveStudentAchivementReportJasper(pen, data, UUID.fromString(studentID), exception, isGraduated);
		assertThat(exception.getExceptionName()).isNull();
	}

	@Test
	public void testStudentAchievementReport() throws Exception {
		GraduationData gradStatus = createGraduationData("json/gradstatus.json");
		List<StudentOptionalProgram> optionalProgram = createStudentOptionalProgramData("json/optionalprograms.json");

		GraduationProgramCode gradProgram = new GraduationProgramCode();
		gradProgram.setProgramCode("2018-EN");
		gradProgram.setProgramName("2018 Graduation Program");

		when(this.restService.get(String.format(constants.getProgramNameEndpoint(),gradStatus.getGradStudent().getProgram()), GraduationProgramCode.class)).thenReturn(gradProgram);

		List<ProgramRequirementCode> programRequirementCodes = new ArrayList<>();

		ProgramRequirementCode programRequirementCode = new ProgramRequirementCode();
		programRequirementCode.setProReqCode("105");
		programRequirementCode.setTraxReqChar("h");

		programRequirementCodes.add(programRequirementCode);

		programRequirementCode = new ProgramRequirementCode();
		programRequirementCode.setProReqCode("109");
		programRequirementCode.setTraxReqChar("I");

		programRequirementCodes.add(programRequirementCode);

		programRequirementCode = new ProgramRequirementCode();
		programRequirementCode.setProReqCode("113");
		programRequirementCode.setTraxReqChar("n");

		programRequirementCodes.add(programRequirementCode);

		programRequirementCode = new ProgramRequirementCode();
		programRequirementCode.setProReqCode("117");
		programRequirementCode.setTraxReqChar("i");

		programRequirementCodes.add(programRequirementCode);

		when(this.restService.get(constants.getProgramRequirementsEndpoint(), List.class)).thenReturn(programRequirementCodes);

		SpecialCase spc = new SpecialCase();
		spc.setLabel("dfsdgs");
		spc.setDescription("wasd");
		spc.setSpCase("A");
		spc.setPassFlag("Y");

		when(this.restService.get(String.format(constants.getSpecialCase(),"A"), SpecialCase.class)).thenReturn(spc);

		ReportData data = reportService.prepareAchievementReportData(gradStatus,optionalProgram, exception);
		assertNotNull(data);
		assertNotNull(data.getStudentExams());
		assertNotNull(data.getStudentCourses());
		assertNotNull(data.getNonGradReasons());
	}

	@Test
	public void testGetTranscript() throws Exception {

		String schoolId = "b69bc244-b93b-2a9f-d2b1-3d8ffae92866";
		GraduationData gradStatus = createGraduationData("json/gradstatus.json");
		assertNotNull(gradStatus);
		String pen = gradStatus.getGradStudent().getPen();
		GradSearchStudent gradSearchStudent = new GradSearchStudent();
		gradSearchStudent.setPen(pen);
		gradSearchStudent.setStudentID(gradStatus.getGradStudent().getStudentID());

		GraduationStudentRecord graduationStudentRecord = new GraduationStudentRecord();
		graduationStudentRecord.setPen(pen);
		graduationStudentRecord.setProgramCompletionDate("2003/01");
		graduationStudentRecord.setStudentID(UUID.fromString(gradSearchStudent.getStudentID()));
		graduationStudentRecord.setUpdateDate(LocalDateTime.now());

		School schoolDetails = new School();
		schoolDetails.setSchoolId(schoolId);
		schoolDetails.setMinCode("09323027");
		schoolDetails.setSchoolCategoryCode("INDEPEN");
		schoolDetails.setSchoolCategoryLegacyCode("02");

		GraduationProgramCode gradProgram = new GraduationProgramCode();
		gradProgram.setProgramCode("1950");
		gradProgram.setProgramName("1950 Adult Graduation Program");

		ProgramCertificateTranscript programCertificateTranscript = new ProgramCertificateTranscript();
		programCertificateTranscript.setPcId(UUID.randomUUID());
		programCertificateTranscript.setGraduationProgramCode(gradProgram.getProgramCode());
		programCertificateTranscript.setSchoolCategoryCode(schoolDetails.getSchoolCategoryCode());
		programCertificateTranscript.setCertificateTypeCode("E");

		when(this.restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(), schoolDetails.getSchoolId()), School.class)).thenReturn(schoolDetails);
		when(this.restService.post(eq(constants.getTranscript()), any(), eq(ProgramCertificateTranscript.class))).thenReturn(programCertificateTranscript);


		var result = reportService.getTranscript(graduationStudentRecord, gradStatus, new ExceptionMessage());
		assertNotNull(result);
	}

	@Test
	public void testGetTranscriptException() throws Exception {

		String schoolId = "b69bc244-b93b-2a9f-d2b1-3d8ffae92866";
		GraduationData gradStatus = createGraduationData("json/gradstatus.json");
		assertNotNull(gradStatus);
		String pen = gradStatus.getGradStudent().getPen();
		GradSearchStudent gradSearchStudent = new GradSearchStudent();
		gradSearchStudent.setPen(pen);
		gradSearchStudent.setStudentID(gradStatus.getGradStudent().getStudentID());

		GraduationStudentRecord graduationStudentRecord = new GraduationStudentRecord();
		graduationStudentRecord.setPen(pen);
		graduationStudentRecord.setProgramCompletionDate("2003/01");
		graduationStudentRecord.setStudentID(UUID.fromString(gradSearchStudent.getStudentID()));
		graduationStudentRecord.setUpdateDate(LocalDateTime.now());

		School schoolDetails = new School();
		schoolDetails.setSchoolId(schoolId);
		schoolDetails.setMinCode("09323027");
		schoolDetails.setSchoolCategoryCode("INDEPEN");
		schoolDetails.setSchoolCategoryLegacyCode("02");

		when(this.restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(),schoolDetails.getSchoolId()), School.class)).thenReturn(schoolDetails);

		when(this.restService.post(eq(constants.getTranscript()), any(), eq(ProgramCertificateTranscript.class))).thenThrow(new RuntimeException());

		ExceptionMessage message = new ExceptionMessage();

		var result = reportService.getTranscript(graduationStudentRecord, gradStatus, message);
		assertNull(result);
		assertEquals("GRAD-GRADUATION-REPORT-API IS DOWN", message.getExceptionName());
	}

	@Test
	public void testReportDataByPen() throws Exception {
		String schoolId = "b69bc244-b93b-2a9f-d2b1-3d8ffae92866";
		GraduationData gradStatus = createGraduationData("json/gradstatus.json");
		assertNotNull(gradStatus);
		String pen = gradStatus.getGradStudent().getPen();
		GradSearchStudent gradSearchStudent = new GradSearchStudent();
		gradSearchStudent.setPen(pen);
		gradSearchStudent.setStudentID(gradStatus.getGradStudent().getStudentID());

		final ParameterizedTypeReference<List<GradSearchStudent>> gradSearchStudentResponseType = new ParameterizedTypeReference<>() {
		};

		when(this.restService.get(String.format(constants.getPenStudentApiByPenUrl(),pen), List.class)).thenReturn(List.of(gradSearchStudent));

		StudentCareerProgram studentCareerProgram1 = new StudentCareerProgram();
		studentCareerProgram1.setCareerProgramCode("XH");
		StudentCareerProgram studentCareerProgram2 = new StudentCareerProgram();
		studentCareerProgram2.setCareerProgramCode("FR");

		GraduationStudentRecord graduationStudentRecord = new GraduationStudentRecord();
		graduationStudentRecord.setPen(pen);
		graduationStudentRecord.setProgramCompletionDate("2003/01");
		graduationStudentRecord.setStudentID(UUID.fromString(gradSearchStudent.getStudentID()));
		graduationStudentRecord.setUpdateDate(LocalDateTime.now());
		graduationStudentRecord.setCareerPrograms(List.of(studentCareerProgram1,studentCareerProgram2));

		GraduationProgramCode gradProgram = new GraduationProgramCode();
		gradProgram.setProgramCode("1950");
		gradProgram.setProgramName("1950 Adult Graduation Program");

		GraduationProgramCode graduationProgramCode = new GraduationProgramCode();
		graduationProgramCode.setProgramCode(gradProgram.getProgramCode());
		graduationProgramCode.setProgramName(gradProgram.getProgramName());
		gradStatus.setGradProgram(graduationProgramCode);
		gradStatus.getGradStatus().setProgram(gradProgram.getProgramCode());
		gradStatus.getGradStatus().setProgramName(gradProgram.getProgramName());

		String studentGradData = readFile("json/gradstatus.json");
		assertNotNull(studentGradData);
		graduationStudentRecord.setStudentGradData(new ObjectMapper().writeValueAsString(gradStatus));

		for(StudentCourse result: gradStatus.getStudentCourses().getStudentCourseList()) {
			if("3, 4".equalsIgnoreCase(result.getGradReqMet())) {
				assertEquals("3, 4", result.getGradReqMet());
				assertTrue(StringUtils.contains(result.getGradReqMetDetail(), "3 - met, 4 - met again"));
			}
		}

		when(this.restService.get(String.format(constants.getProgramNameEndpoint(),gradProgram.getProgramCode()), GraduationProgramCode.class)).thenReturn(gradProgram);
		when(this.restService.get(String.format(constants.getReadGradStudentRecord(),graduationStudentRecord.getStudentID().toString()), GraduationStudentRecord.class)).thenReturn(graduationStudentRecord);

		School schoolDetails = new School();
		schoolDetails.setSchoolId(schoolId);
		schoolDetails.setMinCode("09323027");
		schoolDetails.setSchoolCategoryCode("INDEPEN");
		schoolDetails.setSchoolCategoryLegacyCode("02");

		when(this.restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(),schoolDetails.getSchoolId()), School.class)).thenReturn(schoolDetails);


		ProgramCertificateTranscript programCertificateTranscript = new ProgramCertificateTranscript();
		programCertificateTranscript.setPcId(UUID.randomUUID());
		programCertificateTranscript.setGraduationProgramCode(gradProgram.getProgramCode());
		programCertificateTranscript.setSchoolCategoryCode(schoolDetails.getSchoolCategoryCode());
		programCertificateTranscript.setCertificateTypeCode("E");

		ProgramCertificateReq req = new ProgramCertificateReq();
		req.setProgramCode(gradProgram.getProgramCode());
		req.setSchoolCategoryCode(schoolDetails.getSchoolCategoryCode());

		SpecialCase sp = new SpecialCase();
		sp.setSpCase("A");
		sp.setLabel("AEG");

		when(this.restService.get(String.format(constants.getSpecialCase(),"A"), SpecialCase.class)).thenReturn(sp);
		when(this.restService.post(eq(constants.getTranscript()), any(), eq(ProgramCertificateTranscript.class))).thenReturn(programCertificateTranscript);

		School schtrax = new School();
		schtrax.setMinCode("00502001");
		schtrax.setSchoolName("ROBERT DDGELL");
		schtrax.setAddress1("My Address");

		when(this.restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(),schtrax.getMinCode()), School.class)).thenReturn(schtrax);

		District distInstitute = new District();
		distInstitute.setDistrictNumber("005");
		distInstitute.setDisplayName("My District");

		when(this.restService.get(String.format(constants.getDistrictDetails(),schtrax.getMinCode()), District.class)).thenReturn(distInstitute);


		List<ProgramRequirementCode> programRequirementCodes = new ArrayList<>();

		ProgramRequirementCode programRequirementCode = new ProgramRequirementCode();
		programRequirementCode.setProReqCode("105");
		programRequirementCode.setTraxReqChar("h");

		programRequirementCodes.add(programRequirementCode);

		programRequirementCode = new ProgramRequirementCode();
		programRequirementCode.setProReqCode("109");
		programRequirementCode.setTraxReqChar("I");

		programRequirementCodes.add(programRequirementCode);

		programRequirementCode = new ProgramRequirementCode();
		programRequirementCode.setProReqCode("113");
		programRequirementCode.setTraxReqChar("n");

		programRequirementCodes.add(programRequirementCode);

		programRequirementCode = new ProgramRequirementCode();
		programRequirementCode.setProReqCode("117");
		programRequirementCode.setTraxReqChar("i");

		programRequirementCodes.add(programRequirementCode);

		when(this.restService.get(constants.getProgramRequirementsEndpoint(), List.class)).thenReturn(programRequirementCodes);

		StudentOptionalProgram studentOptionalProgram = new StudentOptionalProgram();
		studentOptionalProgram.setOptionalProgramCode("DD");
		studentOptionalProgram.setOptionalProgramName("Advanced Placement");
		studentOptionalProgram.setStudentID(graduationStudentRecord.getStudentID());

		when(this.restService.get(String.format(constants.getStudentOptionalPrograms(), graduationStudentRecord.getStudentID()), List.class)).thenReturn(List.of(studentOptionalProgram));

		ReportData transcriptData = reportService.prepareTranscriptData(pen, true, exception);
		assertNotNull(transcriptData);
		assertNotNull(transcriptData.getStudent());
		assertNotNull(transcriptData.getTranscript());
		assertEquals("1950", transcriptData.getGradProgram().getCode().getCode());

		transcriptData = reportService.prepareTranscriptData(pen, false, exception);
		assertNotNull(transcriptData);
		assertNotNull(transcriptData.getStudent());
		assertNotNull(transcriptData.getTranscript());
		assertEquals("false", transcriptData.getTranscript().getInterim());

		for(TranscriptResult result: transcriptData.getTranscript().getResults()) {
			assertFalse(result.getRequirement(), StringUtils.contains(result.getRequirement(), "3, 4"));
			assertFalse(result.getRequirementName(), StringUtils.contains(result.getRequirementName(), "3 - met, 4 - met again"));
		}

		ReportData certificateData = reportService.prepareCertificateData(pen, exception);
		assertNotNull(certificateData);
		assertNotNull(certificateData.getStudent());
		assertNotNull(certificateData.getCertificate());

	}

	@Test
	public void testReportDataByPen_witherrors3() throws Exception {
		GraduationData gradStatus = createGraduationData("json/gradstatus.json");
		assertNotNull(gradStatus);
		String pen = gradStatus.getGradStudent().getPen();
		GradSearchStudent gradSearchStudent = new GradSearchStudent();
		gradSearchStudent.setPen(pen);
		gradSearchStudent.setStudentID(gradStatus.getGradStudent().getStudentID());

		when(this.restService.get(String.format(constants.getPenStudentApiByPenUrl(),pen), List.class)).thenReturn(List.of(gradSearchStudent));

		GraduationStudentRecord graduationStudentRecord = new GraduationStudentRecord();
		graduationStudentRecord.setPen(pen);
		graduationStudentRecord.setProgramCompletionDate("2003/01");
		graduationStudentRecord.setStudentID(UUID.fromString(gradSearchStudent.getStudentID()));
		graduationStudentRecord.setUpdateDate(LocalDateTime.now());

		String studentGradData = null;
		assertNull(studentGradData);
		graduationStudentRecord.setStudentGradData(null);

		GraduationProgramCode gradProgram = new GraduationProgramCode();
		gradProgram.setProgramCode("2018-EN");
		gradProgram.setProgramName("2018 Graduation Program");

		when(this.restService.get(String.format(constants.getProgramNameEndpoint(),gradStatus.getGradStudent().getProgram()), GraduationProgramCode.class)).thenReturn(gradProgram);
		when(this.restService.get(String.format(constants.getReadGradStudentRecord(),graduationStudentRecord.getStudentID().toString()), GraduationStudentRecord.class)).thenReturn(graduationStudentRecord);


		ReportData transcriptData = reportService.prepareTranscriptData(pen, true, exception);
		assertNull(transcriptData.getTranscript());

		ReportData certificateData = reportService.prepareCertificateData(pen, exception);
		assertNull(certificateData.getCertificate());

	}

	@Test
	public void testReportDataByPen_witherrors2() throws Exception {
		GraduationData gradStatus = createGraduationData("json/gradstatus.json");
		assertNotNull(gradStatus);
		String pen = gradStatus.getGradStudent().getPen();
		GradSearchStudent gradSearchStudent = new GradSearchStudent();
		gradSearchStudent.setPen(pen);
		gradSearchStudent.setStudentID(gradStatus.getGradStudent().getStudentID());

		when(this.restService.get(String.format(constants.getPenStudentApiByPenUrl(),pen), List.class)).thenReturn(List.of(gradSearchStudent));


		GraduationStudentRecord graduationStudentRecord = new GraduationStudentRecord();
		graduationStudentRecord.setPen(pen);
		graduationStudentRecord.setProgramCompletionDate("2003/01");
		graduationStudentRecord.setStudentID(UUID.fromString(gradSearchStudent.getStudentID()));
		graduationStudentRecord.setUpdateDate(LocalDateTime.now());

		String studentGradData = readFile("json/gradstatus.json");
		assertNotNull(studentGradData);
		graduationStudentRecord.setStudentGradData(new ObjectMapper().writeValueAsString(gradStatus));

		GraduationProgramCode gradProgram = new GraduationProgramCode();
		gradProgram.setProgramCode("2018-EN");
		gradProgram.setProgramName("2018 Graduation Program");

		when(this.restService.get(String.format(constants.getProgramNameEndpoint(),gradStatus.getGradStudent().getProgram()), GraduationProgramCode.class)).thenReturn(gradProgram);
		when(this.restService.get(String.format(constants.getReadGradStudentRecord(),graduationStudentRecord.getStudentID().toString()), Exception.class)).thenReturn(new Exception());


		ReportData transcriptData = reportService.prepareTranscriptData(pen, true, exception);
		assertNull(transcriptData.getTranscript());

		ReportData certificateData = reportService.prepareCertificateData(pen, exception);
		assertNull(certificateData.getCertificate());

	}

	@Test
	public void testReportDataByPen_withErrors() throws Exception {
		GraduationData gradStatus = createGraduationData("json/gradstatus.json");
		assertNotNull(gradStatus);
		String pen = gradStatus.getGradStudent().getPen();
		GradSearchStudent gradSearchStudent = new GradSearchStudent();
		gradSearchStudent.setPen(pen);
		gradSearchStudent.setStudentID(gradStatus.getGradStudent().getStudentID());

		when(this.restService.get(String.format(constants.getPenStudentApiByPenUrl(),pen), Exception.class)).thenReturn(new Exception());

		ReportData transcriptData = reportService.prepareTranscriptData(pen, true, exception);
		assertNull(transcriptData.getTranscript());

		ReportData certificateData = reportService.prepareCertificateData(pen, exception);
		assertNull(certificateData.getGraduationData());

	}

	@Test
	public void testTranscriptReportByPen() throws Exception {
		String schoolId = "b69bc244-b93b-2a9f-d2b1-3d8ffae92866";
		GraduationData gradStatus = createGraduationData("json/gradstatus.json");
		assertNotNull(gradStatus);
		String pen = gradStatus.getGradStudent().getPen();
		GradSearchStudent gradSearchStudent = new GradSearchStudent();
		gradSearchStudent.setPen(pen);
		gradSearchStudent.setStudentID(gradStatus.getGradStudent().getStudentID());

		when(this.restService.get(String.format(constants.getPenStudentApiByPenUrl(),pen), List.class)).thenReturn(List.of(gradSearchStudent));

		GraduationStudentRecord graduationStudentRecord = new GraduationStudentRecord();
		graduationStudentRecord.setPen(pen);
		graduationStudentRecord.setProgramCompletionDate("2003/01");
		graduationStudentRecord.setStudentID(UUID.fromString(gradSearchStudent.getStudentID()));
		graduationStudentRecord.setUpdateDate(LocalDateTime.now());

		graduationStudentRecord.setStudentGradData(new ObjectMapper().writeValueAsString(gradStatus));

		GraduationProgramCode gradProgram = new GraduationProgramCode();
		gradProgram.setProgramCode("2018-EN");
		gradProgram.setProgramName("2018 Graduation Program");

		when(this.restService.get(String.format(constants.getProgramNameEndpoint(),gradStatus.getGradStudent().getProgram()), GraduationProgramCode.class)).thenReturn(gradProgram);
		when(this.restService.get(String.format(constants.getReadGradStudentRecord(),graduationStudentRecord.getStudentID().toString()), GraduationStudentRecord.class)).thenReturn(graduationStudentRecord);

		School schoolDetails = new School();
		schoolDetails.setSchoolId(schoolId);
		schoolDetails.setMinCode("09323027");
		schoolDetails.setSchoolCategoryCode("INDEPEN");
		schoolDetails.setSchoolCategoryLegacyCode("02");

		when(this.restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(),schoolDetails.getSchoolId()), School.class)).thenReturn(schoolDetails);

		ProgramCertificateTranscript programCertificateTranscript = new ProgramCertificateTranscript();
		programCertificateTranscript.setPcId(UUID.randomUUID());
		programCertificateTranscript.setGraduationProgramCode(gradProgram.getProgramCode());
		programCertificateTranscript.setSchoolCategoryCode(schoolDetails.getSchoolCategoryCode());
		programCertificateTranscript.setCertificateTypeCode("E");

		ProgramCertificateReq req = new ProgramCertificateReq();
		req.setProgramCode(gradProgram.getProgramCode());
		req.setSchoolCategoryCode(schoolDetails.getSchoolCategoryCode());

		when(this.restService.post(eq(constants.getTranscript()), any(), eq(ProgramCertificateTranscript.class))).thenReturn(programCertificateTranscript);

		SpecialCase sp = new SpecialCase();
		sp.setSpCase("A");
		sp.setLabel("AEG");

		when(this.restService.get(String.format(constants.getSpecialCase(),"A"), SpecialCase.class)).thenReturn(sp);

		List<ProgramRequirementCode> programRequirementCodes = new ArrayList<>();

		ProgramRequirementCode programRequirementCode = new ProgramRequirementCode();
		programRequirementCode.setProReqCode("105");
		programRequirementCode.setTraxReqChar("h");

		programRequirementCodes.add(programRequirementCode);

		programRequirementCode = new ProgramRequirementCode();
		programRequirementCode.setProReqCode("109");
		programRequirementCode.setTraxReqChar("I");

		programRequirementCodes.add(programRequirementCode);

		programRequirementCode = new ProgramRequirementCode();
		programRequirementCode.setProReqCode("113");
		programRequirementCode.setTraxReqChar("n");

		programRequirementCodes.add(programRequirementCode);

		programRequirementCode = new ProgramRequirementCode();
		programRequirementCode.setProReqCode("117");
		programRequirementCode.setTraxReqChar("i");

		programRequirementCodes.add(programRequirementCode);

		when(this.restService.get(constants.getProgramRequirementsEndpoint(), List.class)).thenReturn(programRequirementCodes);

		StudentOptionalProgram studentOptionalProgram = new StudentOptionalProgram();
		studentOptionalProgram.setOptionalProgramCode("FI");
		studentOptionalProgram.setOptionalProgramName("Advanced Placement");
		studentOptionalProgram.setStudentID(graduationStudentRecord.getStudentID());

		when(this.restService.get(String.format(constants.getStudentOptionalPrograms(), graduationStudentRecord.getStudentID()), List.class)).thenReturn(List.of(studentOptionalProgram));

		ReportData transcriptData = reportService.prepareTranscriptData(pen, true, exception);
		assertNotNull(transcriptData);
		assertNotNull(transcriptData.getStudent());
		assertNotNull(transcriptData.getTranscript());

		byte[] bytesSAR = RandomUtils.nextBytes(20);

		when(this.restService.post(eq(constants.getTranscriptReport()), any(), eq(byte[].class))).thenReturn(bytesSAR);

		byte[] result = graduationService.prepareTranscriptReport(pen, "Interim", "true");
		assertNotNull(result);
		assertNotEquals(0, result.length);

	}

	@Test
	public void testTranscriptReportByPenEmpty() throws Exception {
		GraduationData gradStatus = createGraduationData("json/gradstatus.json");
		assertNotNull(gradStatus);
		String pen = gradStatus.getGradStudent().getPen();

		when(this.restService.post(eq(constants.getTranscriptReport()), any(), eq(byte[].class))).thenThrow(new ServiceException("NO_CONTENT", 204));

		byte[] result = graduationService.prepareTranscriptReport(pen, "Interim", "true");
		assertNotNull(result);
		assertEquals(0, result.length);

	}

	@Test(expected = ServiceException.class)
	public void testTranscriptReportByPenException() throws Exception {
		GraduationData gradStatus = createGraduationData("json/gradstatus.json");
		String pen = gradStatus.getGradStudent().getPen();

		when(this.restService.post(eq(constants.getTranscriptReport()), any(), eq(byte[].class))).thenThrow(new ServiceException("INTERNAL_SERVER_ERROR", 500));

		graduationService.prepareTranscriptReport(pen, "Interim", "true");
	}


	@Test
	public void testReportDataByGraduationData() throws Exception {
		String schoolId = "b69bc244-b93b-2a9f-d2b1-3d8ffae92866";
		GraduationData gradStatus = createGraduationData("json/gradstatus.json");
		assertNotNull(gradStatus);
		String pen = gradStatus.getGradStudent().getPen();

		GraduationStudentRecord graduationStudentRecord = new GraduationStudentRecord();
		graduationStudentRecord.setPen(pen);
		graduationStudentRecord.setProgramCompletionDate("2003/01");
		graduationStudentRecord.setStudentID(UUID.fromString(gradStatus.getGradStudent().getStudentID()));
		graduationStudentRecord.setUpdateDate(LocalDateTime.now());

		String studentGradData = readFile("json/gradstatus.json");
		assertNotNull(studentGradData);
		graduationStudentRecord.setStudentGradData(studentGradData);

		GraduationProgramCode gradProgram = new GraduationProgramCode();
		gradProgram.setProgramCode("2018-EN");
		gradProgram.setProgramName("2018 Graduation Program");

		when(this.restService.get(String.format(constants.getProgramNameEndpoint(),gradStatus.getGradStudent().getProgram()), GraduationProgramCode.class)).thenReturn(gradProgram);
		when(this.restService.get(String.format(constants.getReadGradStudentRecord(),graduationStudentRecord.getStudentID().toString()), GraduationStudentRecord.class)).thenReturn(graduationStudentRecord);

		School schoolDetails = new School();
		schoolDetails.setSchoolId(schoolId);
		schoolDetails.setMinCode("09323027");
		schoolDetails.setSchoolCategoryCode("INDEPEN");
		schoolDetails.setSchoolCategoryLegacyCode("02");

		when(this.restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(),schoolDetails.getSchoolId()), School.class)).thenReturn(schoolDetails);

		ProgramCertificateTranscript programCertificateTranscript = new ProgramCertificateTranscript();
		programCertificateTranscript.setPcId(UUID.randomUUID());
		programCertificateTranscript.setGraduationProgramCode(gradProgram.getProgramCode());
		programCertificateTranscript.setSchoolCategoryCode("02");
		programCertificateTranscript.setCertificateTypeCode("E");

		SpecialCase sp = new SpecialCase();
		sp.setSpCase("A");
		sp.setLabel("AEG");

		when(this.restService.get(String.format(constants.getSpecialCase(),"A"), SpecialCase.class)).thenReturn(sp);
		when(this.restService.post(eq(constants.getTranscript()), any(), eq(ProgramCertificateTranscript.class))).thenReturn(programCertificateTranscript);

		List<ProgramRequirementCode> programRequirementCodes = new ArrayList<>();

		ProgramRequirementCode programRequirementCode = new ProgramRequirementCode();
		programRequirementCode.setProReqCode("105");
		programRequirementCode.setTraxReqChar("h");

		programRequirementCodes.add(programRequirementCode);

		programRequirementCode = new ProgramRequirementCode();
		programRequirementCode.setProReqCode("109");
		programRequirementCode.setTraxReqChar("I");

		programRequirementCodes.add(programRequirementCode);

		programRequirementCode = new ProgramRequirementCode();
		programRequirementCode.setProReqCode("113");
		programRequirementCode.setTraxReqChar("n");

		programRequirementCodes.add(programRequirementCode);

		programRequirementCode = new ProgramRequirementCode();
		programRequirementCode.setProReqCode("117");
		programRequirementCode.setTraxReqChar("i");

		programRequirementCodes.add(programRequirementCode);

		when(this.restService.get(constants.getProgramRequirementsEndpoint(), List.class)).thenReturn(programRequirementCodes);

		StudentOptionalProgram studentOptionalProgram = new StudentOptionalProgram();
		studentOptionalProgram.setOptionalProgramCode("CP");
		studentOptionalProgram.setOptionalProgramName("Advanced Placement");
		studentOptionalProgram.setStudentID(graduationStudentRecord.getStudentID());

		when(this.restService.get(String.format(constants.getStudentOptionalPrograms(), graduationStudentRecord.getStudentID()), List.class)).thenReturn(List.of(studentOptionalProgram));

		ReportData data = reportService.prepareTranscriptData(gradStatus, true, exception);
		assertNotNull(data);
		assertNotNull(data.getStudent());
		assertNotNull(data.getTranscript());

	}

	@Test
	public void testReportDataByGraduationData_GSRNULL() throws Exception {
		String schoolId = "b69bc244-b93b-2a9f-d2b1-3d8ffae92866";
		GraduationData gradStatus = createGraduationData("json/gradstatus.json");
		assertNotNull(gradStatus);

		String studentGradData = readFile("json/gradstatus.json");
		assertNotNull(studentGradData);

		GraduationProgramCode gradProgram = new GraduationProgramCode();
		gradProgram.setProgramCode("2018-EN");
		gradProgram.setProgramName("2018 Graduation Program");

		when(this.restService.get(String.format(constants.getProgramNameEndpoint(),gradStatus.getGradStudent().getProgram()), GraduationProgramCode.class)).thenReturn(gradProgram);
		when(this.restService.get(String.format(constants.getReadGradStudentRecord(),gradStatus.getGradStudent().getStudentID()), GraduationStudentRecord.class)).thenReturn(null);

		School schoolDetails = new School();
		schoolDetails.setSchoolId(schoolId);
		schoolDetails.setMinCode("09323027");
		schoolDetails.setSchoolCategoryCode("INDEPEN");
		schoolDetails.setSchoolCategoryLegacyCode("02");
		when(this.restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(),schoolDetails.getSchoolId()), School.class)).thenReturn(schoolDetails);

		ProgramCertificateTranscript programCertificateTranscript = new ProgramCertificateTranscript();
		programCertificateTranscript.setPcId(UUID.randomUUID());
		programCertificateTranscript.setGraduationProgramCode(gradProgram.getProgramCode());
		programCertificateTranscript.setSchoolCategoryCode("02");
		programCertificateTranscript.setCertificateTypeCode("E");

		when(this.restService.post(eq(constants.getTranscript()), any(), eq(ProgramCertificateTranscript.class))).thenReturn(programCertificateTranscript);

		ReportData data = reportService.prepareTranscriptData(gradStatus, true, exception);
		assertNotNull(data);
		assertNull(data.getStudent());
		assertNull(data.getTranscript());

	}

	@Test
	public void testGetGraduationStudentRecordAndGraduationData() throws Exception {
		GraduationData gradStatus = createGraduationData("json/gradstatus.json");
		assertNotNull(gradStatus);
		String pen = gradStatus.getGradStudent().getPen();

		GraduationStudentRecord graduationStudentRecord = new GraduationStudentRecord();
		graduationStudentRecord.setPen(pen);
		graduationStudentRecord.setProgramCompletionDate("2003/01");
		graduationStudentRecord.setStudentID(UUID.fromString(gradStatus.getGradStudent().getStudentID()));
		graduationStudentRecord.setUpdateDate(LocalDateTime.now());

		String studentGradData = readFile("json/gradstatus.json");
		assertNotNull(studentGradData);
		graduationStudentRecord.setStudentGradData(studentGradData);

		GradSearchStudent gradSearchStudent = new GradSearchStudent();
		gradSearchStudent.setPen(pen);
		gradSearchStudent.setStudentID(gradStatus.getGradStudent().getStudentID());

		when(this.restService.get(String.format(constants.getPenStudentApiByPenUrl(),pen), List.class)).thenReturn(List.of(gradSearchStudent));
		when(this.restService.get(String.format(constants.getReadGradStudentRecord(),graduationStudentRecord.getStudentID().toString()), GraduationStudentRecord.class)).thenReturn(graduationStudentRecord);

		var result = reportService.getGraduationStudentRecordAndGraduationData(pen);
		assertNotNull(result);
		assertNotNull(result.getLeft());
		assertNotNull(result.getRight());
	}

	@Test
	public void testGetGraduationStudentRecordAndGraduationData_Exception() throws Exception {
		GraduationData gradStatus = createGraduationData("json/gradstatus.json");
		assertNotNull(gradStatus);
		String pen = gradStatus.getGradStudent().getPen();
		String studentID = gradStatus.getGradStudent().getStudentID();

		GradSearchStudent gradSearchStudent = new GradSearchStudent();
		gradSearchStudent.setPen(pen);
		gradSearchStudent.setStudentID(gradStatus.getGradStudent().getStudentID());

		final ParameterizedTypeReference<List<GradSearchStudent>> gradSearchStudentResponseType = new ParameterizedTypeReference<>() {
		};

		when(this.restService.get(String.format(constants.getPenStudentApiByPenUrl(),pen), List.class)).thenReturn(List.of(gradSearchStudent));
		when(this.restService.get(String.format(constants.getReadGradStudentRecord(),studentID), GraduationStudentRecord.class)).thenReturn(null);

		var result = reportService.getGraduationStudentRecordAndGraduationData(pen);
		assertNull(result);
	}

	@Test
	public void testReportDataByGraduationData_StudentNull() throws Exception {
		GraduationData gradStatus = createGraduationData("json/gradstatus.json");
		assertNotNull(gradStatus);
		gradStatus.getGradStudent().setStudentID(null);

		ReportData data = reportService.prepareTranscriptData(gradStatus, true, exception);
		assertNotNull(data);
		assertNull(data.getStudent());
		assertNull(data.getTranscript());

	}

	@Test
	public void testReportDataByGraduationData_EXCEPTION() throws Exception {
		String schoolId = "b69bc244-b93b-2a9f-d2b1-3d8ffae92866";
		GraduationData gradStatus = createGraduationData("json/gradstatus.json");
		assertNotNull(gradStatus);
		String pen = gradStatus.getGradStudent().getPen();

		GraduationStudentRecord graduationStudentRecord = new GraduationStudentRecord();
		graduationStudentRecord.setPen(pen);
		graduationStudentRecord.setProgramCompletionDate("2003/01");
		graduationStudentRecord.setStudentID(UUID.fromString(gradStatus.getGradStudent().getStudentID()));
		graduationStudentRecord.setUpdateDate(LocalDateTime.now());

		String studentGradData = readFile("json/gradstatus.json");
		assertNotNull(studentGradData);
		graduationStudentRecord.setStudentGradData(studentGradData);

		GraduationProgramCode gradProgram = new GraduationProgramCode();
		gradProgram.setProgramCode("2018-EN");
		gradProgram.setProgramName("2018 Graduation Program");

		when(this.restService.get(String.format(constants.getProgramNameEndpoint(),gradStatus.getGradStudent().getProgram()), GraduationProgramCode.class)).thenReturn(gradProgram);
		when(this.restService.get(String.format(constants.getReadGradStudentRecord(),graduationStudentRecord.getStudentID().toString()), Exception.class)).thenReturn(new Exception());


		School schoolDetails = new School();
		schoolDetails.setSchoolId(schoolId);
		schoolDetails.setMinCode("09323027");
		schoolDetails.setSchoolCategoryCode("INDEPEN");
		schoolDetails.setSchoolCategoryLegacyCode("02");

		when(this.restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(),schoolDetails.getSchoolId()), School.class)).thenReturn(schoolDetails);


		ProgramCertificateTranscript programCertificateTranscript = new ProgramCertificateTranscript();
		programCertificateTranscript.setPcId(UUID.randomUUID());
		programCertificateTranscript.setGraduationProgramCode(gradProgram.getProgramCode());
		programCertificateTranscript.setSchoolCategoryCode("02");
		programCertificateTranscript.setCertificateTypeCode("E");

		when(this.restService.post(eq(constants.getTranscript()), any(), eq(ProgramCertificateTranscript.class))).thenReturn(programCertificateTranscript);

		ReportData data = reportService.prepareTranscriptData(gradStatus, true, exception);
		assertNotNull(data);
		assertNull(data.getStudent());
		assertNull(data.getTranscript());

	}

	@Test
	public void testPrepareCertificateData() throws Exception {
		String schoolId = "b69bc244-b93b-2a9f-d2b1-3d8ffae92866";
		GraduationData gradStatus = createGraduationData("json/gradstatus.json");
		assertNotNull(gradStatus);
		String pen = gradStatus.getGradStudent().getPen();

		GraduationStudentRecord graduationStudentRecord = new GraduationStudentRecord();
		graduationStudentRecord.setPen(pen);
		graduationStudentRecord.setProgramCompletionDate("2003/01");
		graduationStudentRecord.setStudentID(UUID.fromString(gradStatus.getGradStudent().getStudentID()));
		graduationStudentRecord.setUpdateDate(LocalDateTime.now());

		String studentGradData = readFile("json/gradstatus.json");
		assertNotNull(studentGradData);
		graduationStudentRecord.setStudentGradData(studentGradData);

		GraduationProgramCode gradProgram = new GraduationProgramCode();
		gradProgram.setProgramCode("2018-EN");
		gradProgram.setProgramName("2018 Graduation Program");

		when(this.restService.get(String.format(constants.getProgramNameEndpoint(),gradStatus.getGradStudent().getProgram()), GraduationProgramCode.class)).thenReturn(gradProgram);
		when(this.restService.get(String.format(constants.getReadGradStudentRecord(),graduationStudentRecord.getStudentID().toString()), GraduationStudentRecord.class)).thenReturn(graduationStudentRecord);

		StudentOptionalProgram studentOptionalProgram = new StudentOptionalProgram();
		studentOptionalProgram.setOptionalProgramCode("FR");
		studentOptionalProgram.setOptionalProgramName("Advanced Placement");
		studentOptionalProgram.setStudentID(graduationStudentRecord.getStudentID());

		when(this.restService.get(String.format(constants.getStudentOptionalPrograms(), graduationStudentRecord.getStudentID()), List.class)).thenReturn(List.of(studentOptionalProgram));

		School schoolDetails = new School();
		schoolDetails.setSchoolId(schoolId);
		schoolDetails.setMinCode("09323027");
		schoolDetails.setSchoolCategoryCode("INDEPEN");
		schoolDetails.setSchoolCategoryLegacyCode("02");

		when(this.restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(),schoolDetails.getSchoolId()), School.class)).thenReturn(schoolDetails);


		ProgramCertificateTranscript programCertificateTranscript = new ProgramCertificateTranscript();
		programCertificateTranscript.setPcId(UUID.randomUUID());
		programCertificateTranscript.setGraduationProgramCode(gradProgram.getProgramCode());
		programCertificateTranscript.setSchoolCategoryCode("02");
		programCertificateTranscript.setCertificateTypeCode("E");

		SpecialCase sp = new SpecialCase();
		sp.setSpCase("A");
		sp.setLabel("AEG");

		when(this.restService.get(String.format(constants.getSpecialCase(),"A"), SpecialCase.class)).thenReturn(sp);
		when(this.restService.post(eq(constants.getTranscript()), any(), eq(ProgramCertificateTranscript.class))).thenReturn(programCertificateTranscript);

		ReportData data = reportService.prepareCertificateData(gradStatus, exception);
		assertNotNull(data);
		assertNotNull(data.getStudent());
		assertNotNull(data.getCertificate());

	}

	@Test
	public void testPrepareCertificateData_GSRNULL() throws Exception {
		String schoolId = "b69bc244-b93b-2a9f-d2b1-3d8ffae92866";
		GraduationData gradStatus = createGraduationData("json/gradstatus.json");
		assertNotNull(gradStatus);

		String studentGradData = readFile("json/gradstatus.json");
		assertNotNull(studentGradData);

		GraduationProgramCode gradProgram = new GraduationProgramCode();
		gradProgram.setProgramCode("2018-EN");
		gradProgram.setProgramName("2018 Graduation Program");

		when(this.restService.get(String.format(constants.getProgramNameEndpoint(),gradStatus.getGradStudent().getProgram()), GraduationProgramCode.class)).thenReturn(gradProgram);
		when(this.restService.get(String.format(constants.getReadGradStudentRecord(),gradStatus.getGradStudent().getStudentID()), GraduationStudentRecord.class)).thenReturn(null);


		School schoolDetails = new School();
		schoolDetails.setSchoolId(schoolId);
		schoolDetails.setMinCode("09323027");
		schoolDetails.setSchoolCategoryCode("INDEPEN");
		schoolDetails.setSchoolCategoryLegacyCode("02");

		when(this.restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(),schoolDetails.getSchoolId()), School.class)).thenReturn(schoolDetails);

		ProgramCertificateTranscript programCertificateTranscript = new ProgramCertificateTranscript();
		programCertificateTranscript.setPcId(UUID.randomUUID());
		programCertificateTranscript.setGraduationProgramCode(gradProgram.getProgramCode());
		programCertificateTranscript.setSchoolCategoryCode("02");
		programCertificateTranscript.setCertificateTypeCode("E");

		when(this.restService.post(eq(constants.getTranscript()), any(), eq(ProgramCertificateTranscript.class))).thenReturn(programCertificateTranscript);

		ReportData data = reportService.prepareCertificateData(gradStatus, exception);
		assertNotNull(data);
		assertNull(data.getStudent());
		assertNull(data.getCertificate());

	}

	@Test
	public void testPrepareCertificateData_StudentNull() throws Exception {
		GraduationData gradStatus = createGraduationData("json/gradstatus.json");
		assertNotNull(gradStatus);
		gradStatus.getGradStudent().setStudentID(null);

		ReportData data = reportService.prepareCertificateData(gradStatus, exception);
		assertNotNull(data);
		assertNull(data.getStudent());
		assertNull(data.getCertificate());

	}

	@Test
	public void testPrepareCertificateData_EXCEPTION() throws Exception {
		String schoolId = "b69bc244-b93b-2a9f-d2b1-3d8ffae92866";
		GraduationData gradStatus = createGraduationData("json/gradstatus.json");
		assertNotNull(gradStatus);
		String pen = gradStatus.getGradStudent().getPen();

		GraduationStudentRecord graduationStudentRecord = new GraduationStudentRecord();
		graduationStudentRecord.setPen(pen);
		graduationStudentRecord.setProgramCompletionDate("2003/01");
		graduationStudentRecord.setStudentID(UUID.fromString(gradStatus.getGradStudent().getStudentID()));
		graduationStudentRecord.setUpdateDate(LocalDateTime.now());
		graduationStudentRecord.setSchoolOfRecordId(gradStatus.getGradStatus().getSchoolOfRecordId());

		String studentGradData = readFile("json/gradstatus.json");
		assertNotNull(studentGradData);
		graduationStudentRecord.setStudentGradData(studentGradData);

		GraduationProgramCode gradProgram = new GraduationProgramCode();
		gradProgram.setProgramCode("2018-EN");
		gradProgram.setProgramName("2018 Graduation Program");

		when(this.restService.get(String.format(constants.getProgramNameEndpoint(),gradStatus.getGradStudent().getProgram()), GraduationProgramCode.class)).thenReturn(gradProgram);
		when(this.restService.get(String.format(constants.getReadGradStudentRecord(),gradStatus.getGradStudent().getStudentID()), Exception.class)).thenReturn(new Exception());


		School schoolDetails = new School();
		schoolDetails.setSchoolId(schoolId);
		schoolDetails.setMinCode("09323027");
		schoolDetails.setSchoolCategoryCode("INDEPEN");
		schoolDetails.setSchoolCategoryLegacyCode("02");

		when(this.restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(),schoolDetails.getSchoolId()), School.class)).thenReturn(schoolDetails);

		ProgramCertificateTranscript programCertificateTranscript = new ProgramCertificateTranscript();
		programCertificateTranscript.setPcId(UUID.randomUUID());
		programCertificateTranscript.setGraduationProgramCode(gradProgram.getProgramCode());
		programCertificateTranscript.setSchoolCategoryCode("02");
		programCertificateTranscript.setCertificateTypeCode("E");

		when(this.restService.post(eq(constants.getTranscript()), any(), eq(ProgramCertificateTranscript.class))).thenReturn(programCertificateTranscript);

		ReportData data = reportService.prepareCertificateData(gradStatus, exception);
		assertNotNull(data);
		assertNull(data.getStudent());
		assertNull(data.getCertificate());

	}

	@Test
	public void testRemoveDuplicatedAssessments() throws Exception {
		StudentAssessment assessment1 = new StudentAssessment();
		assessment1.setPen("128309473");
		assessment1.setAssessmentCode("LTE12");
		assessment1.setAssessmentName("Literacy 12");
		assessment1.setSessionDate("2022/04");
		assessment1.setUsed(true);
		assessment1.setProjected(false);

		StudentAssessment assessment2 = new StudentAssessment();
		assessment2.setPen("128309473");
		assessment2.setAssessmentCode("LTE12");
		assessment2.setAssessmentName("Literacy 12");
		assessment2.setSessionDate("2023/01");
		assessment2.setUsed(false);
		assessment2.setProjected(true);

		StudentAssessment assessment3 = new StudentAssessment();
		assessment3.setPen("128309473");
		assessment3.setAssessmentCode("LTE10");
		assessment3.setAssessmentName("Literacy 10");
		assessment3.setSessionDate("2020/01");
		assessment3.setGradReqMet("15");
		assessment3.setGradReqMetDetail("15 - Literacy 10 Assessment");
		assessment3.setProficiencyScore(3.0D);
		assessment3.setUsed(true);
		assessment3.setProjected(false);

		StudentAssessment assessment4 = new StudentAssessment();
		assessment4.setPen("128309473");
		assessment4.setAssessmentCode("NME10");
		assessment4.setAssessmentName("Numeracy 10");
		assessment4.setSessionDate("2021/01");
		assessment4.setGradReqMet("16");
		assessment4.setGradReqMetDetail("16 - Numeracy 10 Assessment");
		assessment4.setProficiencyScore(1.0D);
		assessment4.setUsed(true);
		assessment4.setProjected(false);

		StudentAssessment assessment5 = new StudentAssessment();
		assessment5.setPen("128309473");
		assessment5.setAssessmentCode("NME10");
		assessment5.setAssessmentName("Numeracy 10");
		assessment5.setSessionDate("2020/04");
		assessment5.setUsed(false);
		assessment5.setProjected(false);

		StudentAssessment assessment6 = new StudentAssessment();
		assessment6.setPen("128309473");
		assessment6.setAssessmentCode("NME11");
		assessment6.setAssessmentName("Numeracy 11");
		assessment6.setSessionDate("2020/04");
		assessment6.setUsed(false);
		assessment6.setProjected(true);

		StudentAssessment assessment7 = new StudentAssessment();
		assessment7.setPen("128309473");
		assessment7.setAssessmentCode("NME11");
		assessment7.setAssessmentName("Numeracy 11");
		assessment7.setSessionDate("2020/04");
		assessment7.setUsed(true);
		assessment7.setProjected(false);

		StudentAssessment assessment8 = new StudentAssessment();
		assessment8.setPen("128309473");
		assessment8.setAssessmentCode("NME11");
		assessment8.setAssessmentName("Numeracy 11");
		assessment8.setSessionDate("2020/04");
		assessment8.setUsed(false);
		assessment8.setProjected(false);

		List<StudentAssessment> studentAssessmentList = List.of(
				assessment1, assessment2, assessment3, assessment4, assessment5, assessment6, assessment7, assessment8
		);

		List<StudentAssessment> result = studentAssessmentList.stream()
				.map((StudentAssessment studentAssessment) -> new StudentAssessmentDuplicatesWrapper(studentAssessment, true))
				.distinct()
				.map(StudentAssessmentDuplicatesWrapper::getStudentAssessment)
				.collect(Collectors.toList());
		assertTrue(result.size() < 8);
	}

	@Test
	public void testGetSchoolCategoryCode() {
		UUID schoolId = UUID.randomUUID();
		School schoolDetails = new School();
		schoolDetails.setSchoolId(schoolId.toString());
		schoolDetails.setMinCode("09323027");
		schoolDetails.setSchoolCategoryCode("INDEPEN");
		schoolDetails.setSchoolCategoryLegacyCode("02");
		when(this.restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(), schoolDetails.getSchoolId()), School.class)).thenReturn(schoolDetails);
		var result = reportService.getSchoolCategoryCode(schoolId);
		assertThat(result).isNotNull();
	}

	protected GraduationData createGraduationData(String jsonPath) throws Exception {
		File file = new File(Objects.requireNonNull(ReportServiceTest.class.getClassLoader().getResource(jsonPath)).getFile());
		return new ObjectMapper().readValue(file, GraduationData.class);

	}

	protected ReportData createReportData(String jsonPath) throws Exception {
		String json = readFile(jsonPath);
		return (ReportData)jsonTransformer.unmarshall(json, ReportData.class);
	}

	protected String readFile(String jsonPath) throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(jsonPath);
		return readInputStream(inputStream);
	}

	@SneakyThrows
	private List<ReportGradStudentData> createStudentSchoolYearEndData(String jsonPath) {
		String json = readFile(jsonPath);
		return (List<ReportGradStudentData>) jsonTransformer.unmarshall(json, new TypeReference<List<ReportGradStudentData>>(){});
	}

	@SneakyThrows
	protected List<StudentOptionalProgram> createStudentOptionalProgramData(String jsonPath) {
		String json = readFile(jsonPath);
		return new ObjectMapper().readValue(json, new TypeReference<List<StudentOptionalProgram>>(){});
	}

	private String readInputStream(InputStream is) throws Exception {
		StringBuffer sb = new StringBuffer();
		InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
		BufferedReader reader = new BufferedReader(streamReader);
		String line;
		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}
}

