package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.model.report.Code;
import ca.bc.gov.educ.api.graduation.model.report.Pen;
import ca.bc.gov.educ.api.graduation.model.report.ReportData;
import ca.bc.gov.educ.api.graduation.model.report.Student;
import ca.bc.gov.educ.api.graduation.process.AlgorithmSupport;
import ca.bc.gov.educ.api.graduation.util.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class GraduationServiceTest {

	@Autowired
	private GraduationService graduationService;

	@MockBean
	private GradStatusService gradStatusService;

	@MockBean
	private GradAlgorithmService gradAlgorithmService;

	@MockBean
	private OptionalProgramService optionalProgramService;

	@MockBean
	RESTService restService;

	@MockBean
	private ReportService reportService;

	@MockBean
	private ExceptionMessage exception;

	@Autowired
	private AlgorithmSupport algorithmSupports;

	@MockBean
	private AlgorithmSupport algorithmSupport;

	@MockBean
	private SchoolService schoolService;

	@MockBean
	private TokenUtils tokenUtils;

	@MockBean
	private ClientRegistrationRepository clientRegistrationRepository;

	@MockBean
	private OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository;

	@Autowired
	GradValidation validation;

	@MockBean(name = "webClient")
	WebClient webClient;

	@MockBean(name = "graduationServiceWebClient")
	WebClient graduationServiceWebClient;

	@Autowired
	JsonTransformer jsonTransformer;

	@Mock
	private WebClient.RequestHeadersSpec requestHeadersMock;
	@Mock
	private WebClient.RequestHeadersUriSpec requestHeadersUriMock;
	@Mock
	private WebClient.RequestBodySpec requestBodyMock;
	@Mock
	private WebClient.RequestBodyUriSpec requestBodyUriMock;
	@Mock
	private WebClient.ResponseSpec responseMock;
	@Mock
	private Mono<GraduationStudentRecord> monoResponse;

	@Autowired
	private EducGraduationApiConstants constants;

	@Test
	public void testGraduateStudent() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="REGFM";
		String accessToken="accessToken";
		ExceptionMessage exception = new ExceptionMessage();
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
		sc.setStudentCourseList(new ArrayList<>());
		graduationDataStatus.setStudentCourses(sc);

		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<>());
		graduationDataStatus.setStudentAssessments(sA);

		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<>();
		list.add(spgm);

		ProjectedRunClob projectedRunClob = ProjectedRunClob.builder().graduated(graduationDataStatus.isGraduated()).nonGradReasons(graduationDataStatus.getNonGradReasons()).build();

		ReportData data = new ReportData();
		data.setOrgCode("BC");
		Student std = new Student();
		std.setFirstName("Sreepad");
		data.setStudent(std);
		Mockito.when(gradStatusService.saveStudentRecordProjectedRun(projectedRunClob, studentID, null, accessToken, exception)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareAchievementReportData(graduationDataStatus, list,accessToken, exception)).thenReturn(data);
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken,exception)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runProjectedAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.processProjectedResults(gradResponse,graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(optionalProgramService.projectedOptionalPrograms(graduationDataStatus, studentID, accessToken)).thenReturn(list);

		AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
		assertNotNull(response);
	}

	@Test
	public void testGraduateStudent_when_gradResponse_has_exception() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="REGFM";
		String accessToken="accessToken";
		ExceptionMessage exception = new ExceptionMessage();

		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken, exception)).thenCallRealMethod();

		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getReadGradStudentRecord(), studentID))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(GraduationStudentRecord.class)).thenThrow(new RuntimeException("Unknown Exception"));

		AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
		assertNotNull(response);
		assertNotNull(response.getException());
	}

	@Test
	public void testGraduateStudent_excep_1() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="REGFM";
		String accessToken="accessToken";
		ExceptionMessage exception = new ExceptionMessage();
		ExceptionMessage eM2 = new ExceptionMessage();
		eM2.setExceptionName("RER");

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
		sc.setStudentCourseList(new ArrayList<>());
		graduationDataStatus.setStudentCourses(sc);

		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<>());
		graduationDataStatus.setStudentAssessments(sA);

		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<>();
		list.add(spgm);

		ProjectedRunClob projectedRunClob = ProjectedRunClob.builder().graduated(graduationDataStatus.isGraduated()).nonGradReasons(graduationDataStatus.getNonGradReasons()).build();

		ReportData data = new ReportData();
		data.setOrgCode("BC");
		data.setException(eM2);
		Student std = new Student();
		std.setFirstName("Sreepad");
		data.setStudent(std);
		Mockito.when(gradStatusService.saveStudentRecordProjectedRun(projectedRunClob, studentID, null, accessToken, exception)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareAchievementReportData(graduationDataStatus, list,accessToken, exception)).thenReturn(data);
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken,exception)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runProjectedAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.processProjectedResults(gradResponse,graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(optionalProgramService.projectedOptionalPrograms(graduationDataStatus, studentID, accessToken)).thenReturn(list);

		AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
		assertNotNull(response);
	}

	@Test
	public void testGraduateStudent_excep_2() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="REGFM";
		String accessToken="accessToken";
		ExceptionMessage exception = new ExceptionMessage();
		ExceptionMessage eM2 = new ExceptionMessage();
		eM2.setExceptionName("RER");

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
		sc.setStudentCourseList(new ArrayList<>());
		graduationDataStatus.setStudentCourses(sc);

		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<>());
		graduationDataStatus.setStudentAssessments(sA);

		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<>();
		list.add(spgm);

		ProjectedRunClob projectedRunClob = ProjectedRunClob.builder().graduated(graduationDataStatus.isGraduated()).nonGradReasons(graduationDataStatus.getNonGradReasons()).build();

		ReportData data = new ReportData();
		data.setOrgCode("BC");
		Student std = new Student();
		std.setFirstName("Sreepad");
		data.setStudent(std);
		Mockito.when(gradStatusService.saveStudentRecordProjectedRun(projectedRunClob, studentID, null, accessToken, exception)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareAchievementReportData(graduationDataStatus, list,accessToken, exception)).thenReturn(data);
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken,exception)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runProjectedAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.processProjectedResults(gradResponse,graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(optionalProgramService.projectedOptionalPrograms(graduationDataStatus, studentID, accessToken)).thenReturn(list);
		Mockito.when(reportService.saveStudentAchivementReportJasper("123090109",data,accessToken,UUID.fromString(studentID),exception,false)).thenReturn(eM2);
		AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
		assertNotNull(response);
	}

	@Test
	public void testGraduateStudent_error() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="REGFM";
		String accessToken="accessToken";
		validation.clear();
		ExceptionMessage exception = new ExceptionMessage();
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
		sc.setStudentCourseList(new ArrayList<>());
		graduationDataStatus.setStudentCourses(sc);

		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<>());
		graduationDataStatus.setStudentAssessments(sA);

		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<>();
		list.add(spgm);
		ProjectedRunClob projectedRunClob = ProjectedRunClob.builder().graduated(graduationDataStatus.isGraduated()).nonGradReasons(graduationDataStatus.getNonGradReasons()).build();

		ReportData data = new ReportData();
		data.setOrgCode("BC");
		Student std = new Student();
		std.setFirstName("Sreepad");
		data.setStudent(std);
		Mockito.when(gradStatusService.saveStudentRecordProjectedRun(projectedRunClob, studentID, null, accessToken, exception)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareAchievementReportData(graduationDataStatus, list,accessToken, exception)).thenReturn(data);

		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken,exception)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runProjectedAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.processProjectedResults(gradResponse,graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(optionalProgramService.projectedOptionalPrograms(graduationDataStatus, studentID, accessToken)).thenReturn(list);

		try {
			graduationService.graduateStudent(studentID,null,accessToken,projectedType);
		} catch (GradBusinessRuleException e) {
			List<String> errors = validation.getErrors();
			assertEquals(0, errors.size());
		}
	}

	@Test
	public void testGraduateStudent_error2() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="REGFM";
		String accessToken="accessToken";
		validation.clear();
		ExceptionMessage exception = new ExceptionMessage();
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setStudentID(new UUID(1, 1));
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("MER");

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

		ProjectedRunClob projectedRunClob = ProjectedRunClob.builder().graduated(graduationDataStatus.isGraduated()).nonGradReasons(graduationDataStatus.getNonGradReasons()).build();

		ReportData data = new ReportData();
		data.setOrgCode("BC");
		Student std = new Student();
		std.setFirstName("Sreepad");
		data.setStudent(std);
		Mockito.when(gradStatusService.saveStudentRecordProjectedRun(projectedRunClob, studentID, null, accessToken, exception)).thenReturn(gradResponse);
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken,exception)).thenReturn(gradResponse);
		try {
			graduationService.graduateStudent(studentID,null,accessToken,projectedType);
		} catch (GradBusinessRuleException e) {
			List<String> errors = validation.getErrors();
			assertEquals(0, errors.size());
		}
	}

	@Test
	public void testGraduateStudent_withProjectedTypeFM() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="FM";
		String accessToken="accessToken";
		ExceptionMessage exception = new ExceptionMessage();
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
		sc.setStudentCourseList(new ArrayList<>());
		graduationDataStatus.setStudentCourses(sc);

		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<>());
		graduationDataStatus.setStudentAssessments(sA);

		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<>();
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
		ExceptionMessage exception = new ExceptionMessage();
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
		sc.setStudentCourseList(new ArrayList<>());
		graduationDataStatus.setStudentCourses(sc);

		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<>());
		graduationDataStatus.setStudentAssessments(sA);


		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<>();
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
		Mockito.when(reportService.prepareTranscriptData(graduationDataStatus,gradResponse,false,accessToken,exception)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID,null, accessToken,gradResponse,exception)).thenReturn(gradResponse);
		Mockito.when(optionalProgramService.saveAndLogOptionalPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		doNothing().when(this.tokenUtils).setAccessToken(any());
		AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
		assertNotNull(response);
	}

	@Test
	public void testGraduateStudent_withProjectedTypeGS_excep_check() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="GS";
		String accessToken="accessToken";
		ExceptionMessage exception = new ExceptionMessage();
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setStudentID(UUID.fromString(studentID));
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("A");
		ExceptionMessage excep2 = new ExceptionMessage();
		excep2.setExceptionName("RER");
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
		sc.setStudentCourseList(new ArrayList<>());
		graduationDataStatus.setStudentCourses(sc);
		graduationDataStatus.setException(excep2);

		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<>());
		graduationDataStatus.setStudentAssessments(sA);


		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<>();
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
		data.setException(excep2);
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken,exception)).thenReturn(gradResponse);
		graduationDataStatus.setException(exception);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken,exception)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.prepareGraduationStatusObj(graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareTranscriptData(graduationDataStatus,gradResponse,false,accessToken,exception)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID,null, accessToken,gradResponse,exception)).thenReturn(gradResponse);
		Mockito.when(optionalProgramService.saveAndLogOptionalPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		doNothing().when(this.tokenUtils).setAccessToken(any());

		ExceptionMessage eM2 = new ExceptionMessage();
		AlgorithmResponse algorithmResponse = new AlgorithmResponse();
		ProcessorData pData = ProcessorData.builder().gradResponse(gradResponse).studentID(studentID).accessToken("accessToken").exception(exception).build();
		eM2.setExceptionName("RERE");
		AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
		assertNotNull(response);
	}

	@Test
	public void testGraduateStudent_withProjectedTypeGS_excep_check_2() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="GS";
		String accessToken="accessToken";
		ExceptionMessage exception = new ExceptionMessage();
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setStudentID(UUID.fromString(studentID));
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("A");
		ExceptionMessage excep2 = new ExceptionMessage();
		excep2.setExceptionName("RER");
		gradResponse.setException(excep2);
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
		sc.setStudentCourseList(new ArrayList<>());
		graduationDataStatus.setStudentCourses(sc);
		graduationDataStatus.setException(excep2);

		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<>());
		graduationDataStatus.setStudentAssessments(sA);


		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<>();
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
		graduationDataStatus.setException(exception);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken,exception)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.prepareGraduationStatusObj(graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareTranscriptData(graduationDataStatus,gradResponse,false,accessToken,exception)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID,null, accessToken,gradResponse,exception)).thenReturn(gradResponse);
		Mockito.when(optionalProgramService.saveAndLogOptionalPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		doNothing().when(this.tokenUtils).setAccessToken(any());

		ExceptionMessage eM2 = new ExceptionMessage();
		AlgorithmResponse algorithmResponse = new AlgorithmResponse();
		ProcessorData pData = ProcessorData.builder().gradResponse(gradResponse).studentID(studentID).accessToken("accessToken").exception(exception).build();
		eM2.setExceptionName("RERE");
		AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
		assertNotNull(response);
	}

	@Test
	public void testGraduateStudent_withProjectedTypeGS_excep_check_3() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="GS";
		String accessToken="accessToken";
		ExceptionMessage exception = new ExceptionMessage();
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setStudentID(UUID.fromString(studentID));
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("A");
		ExceptionMessage excep2 = new ExceptionMessage();
		excep2.setExceptionName("RER");
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
		sc.setStudentCourseList(new ArrayList<>());
		graduationDataStatus.setStudentCourses(sc);

		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<>());
		graduationDataStatus.setStudentAssessments(sA);


		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<>();
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

		ProcessorData pData = ProcessorData.builder().gradResponse(gradResponse).studentID(studentID).accessToken("accessToken").exception(exception).build();


		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken,exception)).thenReturn(gradResponse);
		graduationDataStatus.setException(exception);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken,exception)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.prepareGraduationStatusObj(graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareTranscriptData(graduationDataStatus,gradResponse,false,accessToken,exception)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID,null, accessToken,gradResponse,exception)).thenReturn(gradResponse);
		Mockito.when(optionalProgramService.saveAndLogOptionalPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		Mockito.when(algorithmSupport.createStudentCertificateTranscriptReports(graduationDataStatus,gradResponse,gradResponse,list,exception,data, pData, projectedType)).thenReturn(excep2);
		doNothing().when(this.tokenUtils).setAccessToken(any());
		ExceptionMessage eM2 = new ExceptionMessage();
		AlgorithmResponse algorithmResponse = new AlgorithmResponse();
		eM2.setExceptionName("RERE");
		AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
		assertNotNull(response);
	}


	@Test
	public void testGraduateStudent_withProjectedTypeGS_excep() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="GS";
		String accessToken="accessToken";
		ExceptionMessage exception = new ExceptionMessage();
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setStudentID(UUID.fromString(studentID));
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("A");
		ExceptionMessage excep2 = new ExceptionMessage();
		excep2.setExceptionName("RER");
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
		sc.setStudentCourseList(new ArrayList<>());
		graduationDataStatus.setStudentCourses(sc);
		graduationDataStatus.setException(excep2);

		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<>());
		graduationDataStatus.setStudentAssessments(sA);


		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<>();
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
		graduationDataStatus.setException(exception);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken,exception)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.prepareGraduationStatusObj(graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareTranscriptData(graduationDataStatus,gradResponse,false,accessToken,exception)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID,null, accessToken,gradResponse,exception)).thenReturn(gradResponse);
		Mockito.when(optionalProgramService.saveAndLogOptionalPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		doNothing().when(this.tokenUtils).setAccessToken(any());
		AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
		assertNotNull(response);
	}

	@Test
	public void testGraduateStudent_withProjectedTypeGS_programcompletionDate_notnull() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="GS";
		String accessToken="accessToken";
		validation.clear();
		ExceptionMessage exception = new ExceptionMessage();
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
		List<StudentOptionalProgram> list = new ArrayList<>();
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

		doNothing().when(this.tokenUtils).setAccessToken(any());
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken,exception)).thenReturn(gradResponse);
		try {
			AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
			assertNotNull(response);
		} catch (GradBusinessRuleException e) {
			List<String> errors = validation.getErrors();
			assertEquals(0, errors.size());
		}

	}

	@Test
	public void testGraduateStudent_withProjectedTypeGS_Graduated() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="GS";
		String accessToken="accessToken";
		ExceptionMessage exception = new ExceptionMessage();
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
		sc.setStudentCourseList(new ArrayList<>());
		graduationDataStatus.setStudentCourses(sc);

		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<>());
		graduationDataStatus.setStudentAssessments(sA);

		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<>();
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

		List<ProgramCertificateTranscript> certificateList = new ArrayList<>();
		ProgramCertificateTranscript pc= new ProgramCertificateTranscript();
		pc.setCertificateTypeCode("E");
		certificateList.add(pc);

		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken,exception)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken,exception)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.prepareGraduationStatusObj(graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareTranscriptData(graduationDataStatus,gradResponse,false,accessToken,exception)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID,null, accessToken,gradResponse,exception)).thenReturn(gradResponse);
		Mockito.when(reportService.getCertificateList(gradResponse,graduationDataStatus,list,accessToken,exception)).thenReturn(certificateList);
		doNothing().when(reportService).saveStudentCertificateReportJasper(gradResponse,graduationDataStatus,accessToken,pc,false);
		Mockito.when(optionalProgramService.saveAndLogOptionalPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		doNothing().when(this.tokenUtils).setAccessToken(any());
		AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
		assertNotNull(response);
	}

	@Test
	public void testGraduateStudent_withProjectedTypeFMR_programCompletionDate_null() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="FMR";
		String accessToken="accessToken";
		ExceptionMessage exception = new ExceptionMessage();
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
		List<StudentOptionalProgram> list = new ArrayList<>();
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
		Mockito.when(reportService.prepareTranscriptData(graduationDataStatus,gradResponse,false,accessToken,exception)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID,null, accessToken,gradResponse,exception)).thenReturn(gradResponse);
		Mockito.when(optionalProgramService.saveAndLogOptionalPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		try {
			AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
			assertNotNull(response);
		} catch (GradBusinessRuleException e) {
			List<String> errors = validation.getErrors();
			assertEquals(0, errors.size());
		}

	}

	@Test
	public void testGraduateStudent_withProjectedTypeFMR() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="FMR";
		String accessToken="accessToken";
		ExceptionMessage exception = new ExceptionMessage();
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
		sc.setStudentCourseList(new ArrayList<>());
		graduationDataStatus.setStudentCourses(sc);

		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<>());
		graduationDataStatus.setStudentAssessments(sA);

		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<>();
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
		Mockito.when(reportService.prepareTranscriptData(graduationDataStatus,gradResponse,false,accessToken,exception)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID,null, accessToken,gradResponse,exception)).thenReturn(gradResponse);
		Mockito.when(optionalProgramService.saveAndLogOptionalPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		try {
			AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
			assertNotNull(response);
		} catch (GradBusinessRuleException e) {
			List<String> errors = validation.getErrors();
			assertEquals(0, errors.size());

		}

	}

	@Test
	public void testGraduateStudent_withProjectedTypeFMR_excep1() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="FMR";
		String accessToken="accessToken";
		ExceptionMessage exception = new ExceptionMessage();
		ExceptionMessage em2 = new ExceptionMessage();
		em2.setExceptionName("RERE");
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setStudentID(new UUID(1, 1));
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate("2021-09-01");
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("A");
		gradResponse.setException(em2);
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
		sc.setStudentCourseList(new ArrayList<>());
		graduationDataStatus.setStudentCourses(sc);

		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<>());
		graduationDataStatus.setStudentAssessments(sA);

		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<>();
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
		Mockito.when(reportService.prepareTranscriptData(graduationDataStatus,gradResponse,false,accessToken,exception)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID,null, accessToken,gradResponse,exception)).thenReturn(gradResponse);
		Mockito.when(optionalProgramService.saveAndLogOptionalPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		try {
			AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
			assertNotNull(response);
		} catch (GradBusinessRuleException e) {
			List<String> errors = validation.getErrors();
			assertEquals(0, errors.size());

		}

	}

	@Test
	public void testGraduateStudent_withProjectedTypeFMR_excep3() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="FMR";
		String accessToken="accessToken";
		ExceptionMessage exception = new ExceptionMessage();
		ExceptionMessage em2 = new ExceptionMessage();
		em2.setExceptionName("RERE");
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
		sc.setStudentCourseList(new ArrayList<>());
		graduationDataStatus.setStudentCourses(sc);

		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<>());
		graduationDataStatus.setStudentAssessments(sA);

		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<>();
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
		ProcessorData pData = ProcessorData.builder().gradResponse(gradResponse).studentID(studentID).accessToken("accessToken").exception(exception).build();
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken,exception)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken,exception)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.prepareGraduationStatusObj(graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareTranscriptData(graduationDataStatus,gradResponse,false,accessToken,exception)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID,null, accessToken,gradResponse,exception)).thenReturn(gradResponse);
		Mockito.when(optionalProgramService.saveAndLogOptionalPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		Mockito.when(algorithmSupport.createStudentCertificateTranscriptReports(graduationDataStatus,gradResponse,gradResponse,list,exception,data, pData, "FMR")).thenReturn(em2);
		try {
			AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
			assertNotNull(response);
		} catch (GradBusinessRuleException e) {
			List<String> errors = validation.getErrors();
			assertEquals(0, errors.size());

		}

	}


	@Test
	public void testGraduateStudent_withProjectedTypeFMR_excep2() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="FMR";
		String accessToken="accessToken";
		ExceptionMessage exception = new ExceptionMessage();
		ExceptionMessage em2 = new ExceptionMessage();
		em2.setExceptionName("RERE");
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
		sc.setStudentCourseList(new ArrayList<>());
		graduationDataStatus.setStudentCourses(sc);

		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<>());
		graduationDataStatus.setStudentAssessments(sA);

		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<>();
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
		data.setException(em2);
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken,exception)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken,exception)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.prepareGraduationStatusObj(graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareTranscriptData(graduationDataStatus,gradResponse,false,accessToken,exception)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID,null, accessToken,gradResponse,exception)).thenReturn(gradResponse);
		Mockito.when(optionalProgramService.saveAndLogOptionalPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		try {
			AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
			assertNotNull(response);
		} catch (GradBusinessRuleException e) {
			List<String> errors = validation.getErrors();
			assertEquals(0, errors.size());

		}

	}

	@Test
	public void testGraduateStudent_withProjectedTypeFMR_programcompletionDate_notnull() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="GS";
		String accessToken="accessToken";
		validation.clear();
		ExceptionMessage exception = new ExceptionMessage();
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
		List<StudentOptionalProgram> list = new ArrayList<>();
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

		doNothing().when(this.tokenUtils).setAccessToken(any());
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken,exception)).thenReturn(gradResponse);
		try {
			AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
			assertNotNull(response);
		} catch (GradBusinessRuleException e) {
			List<String> errors = validation.getErrors();
			assertEquals(0, errors.size());
		}

	}

	@Test
	public void testGraduateStudent_withProjectedTypeFMR_programCompletionDate_notnull_program_sccp() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="GS";
		String accessToken="accessToken";
		ExceptionMessage exception = new ExceptionMessage();
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
		sc.setStudentCourseList(new ArrayList<>());
		graduationDataStatus.setStudentCourses(sc);

		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<>());
		graduationDataStatus.setStudentAssessments(sA);

		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<>();
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
		Mockito.when(reportService.prepareTranscriptData(graduationDataStatus,gradResponse,false,accessToken,exception)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID,null, accessToken,gradResponse,exception)).thenReturn(gradResponse);
		Mockito.when(optionalProgramService.saveAndLogOptionalPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		doNothing().when(this.tokenUtils).setAccessToken(any());
		AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
		assertNotNull(response);
	}

	@Test
	public void testGraduateStudent_withProjectedTypeFMR_Graduated() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="GS";
		String accessToken="accessToken";
		ExceptionMessage exception = new ExceptionMessage();
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
		sc.setStudentCourseList(new ArrayList<>());
		graduationDataStatus.setStudentCourses(sc);

		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<>());
		graduationDataStatus.setStudentAssessments(sA);

		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<>();
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

		List<ProgramCertificateTranscript> certificateList = new ArrayList<>();
		ProgramCertificateTranscript pc= new ProgramCertificateTranscript();
		pc.setCertificateTypeCode("E");
		certificateList.add(pc);

		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken,exception)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken,exception)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.prepareGraduationStatusObj(graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareTranscriptData(graduationDataStatus,gradResponse,false,accessToken,exception)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID,null, accessToken,gradResponse,exception)).thenReturn(gradResponse);
		Mockito.when(reportService.getCertificateList(gradResponse,graduationDataStatus,list,accessToken,exception)).thenReturn(certificateList);
		doNothing().when(reportService).saveStudentCertificateReportJasper(gradResponse,graduationDataStatus,accessToken,pc,false);
		Mockito.when(optionalProgramService.saveAndLogOptionalPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		doNothing().when(this.tokenUtils).setAccessToken(any());
		AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
		assertNotNull(response);
	}

	@Test
	public void testGraduateStudent_withProjectedTypeGS_withexception() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="GS";
		String accessToken="accessToken";
		ExceptionMessage exception = new ExceptionMessage();
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
		sc.setStudentCourseList(new ArrayList<>());
		graduationDataStatus.setStudentCourses(sc);

		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<>());
		graduationDataStatus.setStudentAssessments(sA);

		School school = new School();
		school.setMinCode("012345");
		school.setSchoolName("XYZ");
		school.setSchoolCategoryCode("01");
		school.setSchoolPhone("123456");
		school.setIndependentDesignation("2");
		graduationDataStatus.setSchool(school);

		GradSearchStudent gradStudent = new GradSearchStudent();
		gradStudent.setStudentID("00000000-0000-0001-0000-000000000001");
		gradStudent.setPen("123090109");
		gradStudent.setLegalFirstName("A");
		gradStudent.setEmail("abc@gmail.com");
		graduationDataStatus.setGradStudent(gradStudent);

		StudentCertificatesTranscript studentCertificatesTranscript = new StudentCertificatesTranscript();
		studentCertificatesTranscript.setTranscriptTypeCode("02");
		graduationDataStatus.setStudentCertificatesTranscript(studentCertificatesTranscript);

		ExceptionMessage ex = new ExceptionMessage();
		ex.setExceptionName("ALG");
		ex.setExceptionDetails("NULL POINTER");
		graduationDataStatus.setException(ex);

		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<>();
		list.add(spgm);

		StudentDemographics sD = new StudentDemographics();
		sD.setPen("123123123");
		sD.setLegalFirstName("ABC");
		sD.setLegalLastName("FDG");
		sD.setLegalMiddleNames("DER");
		sD.setSchoolOfRecord("12321321");

		GraduationMessages gM = new GraduationMessages();
		gM.setGradMessage("asdad");

		ReportData data = new ReportData();
		data.setGradMessage("ABC");
		doNothing().when(this.tokenUtils).setAccessToken(any());
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken,exception)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), accessToken,exception)).thenReturn(graduationDataStatus);
		Mockito.when(gradStatusService.prepareGraduationStatusObj(graduationDataStatus)).thenReturn(gradResponse);
		Mockito.when(reportService.prepareTranscriptData(graduationDataStatus,gradResponse,false,accessToken,exception)).thenReturn(data);
		Mockito.when(gradStatusService.saveStudentGradStatus(studentID,null, accessToken,gradResponse,exception)).thenReturn(gradResponse);
		Mockito.when(optionalProgramService.saveAndLogOptionalPrograms(graduationDataStatus,studentID,accessToken,new ArrayList<>())).thenReturn(list);
		AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
		assertNotNull(response);
	}

	@Test
	public void testGraduateStudent_withProjectedTypeFMR_withexception2() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="FMR";
		String accessToken="accessToken";
		ExceptionMessage exception = new ExceptionMessage();
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
		sc.setStudentCourseList(new ArrayList<>());
		graduationDataStatus.setStudentCourses(sc);

		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<>());
		graduationDataStatus.setStudentAssessments(sA);

		ExceptionMessage ex = new ExceptionMessage();
		ex.setExceptionName("ALG");
		ex.setExceptionDetails("NULL POINTER");
		graduationDataStatus.setException(ex);

		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<>();
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
		AlgorithmResponse response = graduationService.graduateStudent(studentID,null,accessToken,projectedType);
		assertNotNull(response);
	}

	@Test
	public void testcreateReportNCert() {
		String studentID = new UUID(1, 1).toString();
		ExceptionMessage exception = new ExceptionMessage();
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setStudentID(new UUID(1, 1));
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate("2020/08");
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("A");

		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate("2020/08");
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");

		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(true);
		StudentCourses sc = new StudentCourses();
		sc.setStudentCourseList(new ArrayList<>());
		graduationDataStatus.setStudentCourses(sc);

		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<>());
		graduationDataStatus.setStudentAssessments(sA);

		ExceptionMessage ex = new ExceptionMessage();
		ex.setExceptionName("ALG");
		ex.setExceptionDetails("NULL POINTER");
		graduationDataStatus.setException(ex);

		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<>();
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

		List<ProgramCertificateTranscript> pList = new ArrayList<>();
		ProgramCertificateTranscript pcr = new ProgramCertificateTranscript();
		pcr.setSchoolCategoryCode("01");
		pcr.setCertificateTypeCode("E");
		pList.add(pcr);

		ProcessorData pData = new ProcessorData();

		ReportData data = new ReportData();
		data.setGradMessage("ABC");
		Mockito.when(reportService.getCertificateList(gradResponse, graduationDataStatus, list, null, exception)).thenReturn(pList);

		algorithmSupports.createStudentCertificateTranscriptReports(graduationDataStatus,gradResponse,gradResponse,list,exception,data,pData, "GS");
		assertNotNull(data);
	}

	@Test
	public void testcreateReportNCert2() {
		String studentID = new UUID(1, 1).toString();
		ExceptionMessage exception = new ExceptionMessage();
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setStudentID(new UUID(1, 1));
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate("2020/08");
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("A");

		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate("2020/08");
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");

		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(true);
		StudentCourses sc = new StudentCourses();
		StudentCourse scs = new StudentCourse();
		scs.setCourseCode("ASA");
		List<StudentCourse> sList = new ArrayList<>();
		sList.add(scs);
		sc.setStudentCourseList(sList);
		graduationDataStatus.setStudentCourses(sc);

		StudentAssessments sA = new StudentAssessments();
		sA.setStudentAssessmentList(new ArrayList<>());
		graduationDataStatus.setStudentAssessments(sA);

		ExceptionMessage ex = new ExceptionMessage();
		ex.setExceptionName("ALG");
		ex.setExceptionDetails("NULL POINTER");
		graduationDataStatus.setException(ex);

		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<>();
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

		List<ProgramCertificateTranscript> pList = new ArrayList<>();
		ProgramCertificateTranscript pcr = new ProgramCertificateTranscript();
		pcr.setSchoolCategoryCode("01");
		pcr.setCertificateTypeCode("E");
		pList.add(pcr);

		ProcessorData pData = new ProcessorData();

		ReportData data = new ReportData();
		data.setGradMessage("ABC");
		Mockito.when(reportService.getCertificateList(gradResponse, graduationDataStatus, list, null, exception)).thenReturn(pList);

		algorithmSupports.createStudentCertificateTranscriptReports(graduationDataStatus,gradResponse,gradResponse,list,exception,data,pData, "GS");
		assertNotNull(data);
	}

	@Test
	public void testCreateAndStoreSchoolReports() {
		ExceptionMessage exception = new ExceptionMessage();
		String mincode = "1231231231";
		List<String> uniqueList = new ArrayList<>();
		uniqueList.add(mincode);

		List<GraduationStudentRecord> sList = new ArrayList<>();
		List<GradRequirement> nonList = new ArrayList<>();
		GradRequirement non = new GradRequirement();
		non.setRule("1");
		non.setDescription("ree");
		nonList.add(non);

		ProjectedRunClob pr = new ProjectedRunClob();
		pr.setGraduated(false);
		pr.setNonGradReasons(nonList);

		GraduationStudentRecord gsr = new GraduationStudentRecord();
		gsr.setLegalFirstName("My First Name");
		gsr.setLegalMiddleNames("My Middle Name");
		gsr.setLegalLastName("My Last Name");
		gsr.setStudentGrade("12");
		gsr.setStudentStatus("CUR");
		gsr.setProgramCompletionDate("10/20/2020");

		try {
			gsr.setStudentProjectedGradData(new ObjectMapper().writeValueAsString(pr));
		} catch (JsonProcessingException e) {
			e.getMessage();
		}

		sList.add(gsr);

		gsr = new GraduationStudentRecord();
		gsr.setLegalFirstName("Just Another My First Name");
		gsr.setLegalMiddleNames("Just Another My Middle Name");
		gsr.setLegalLastName("Just Another My Last Name");
		gsr.setStudentGrade("AD");
		gsr.setStudentStatus("CUR");
		gsr.setProgramCompletionDate(null);

		try {
			gsr.setStudentProjectedGradData(new ObjectMapper().writeValueAsString(pr));
		} catch (JsonProcessingException e) {
			e.getMessage();
		}

		sList.add(gsr);

		SchoolTrax sTrax = new SchoolTrax();
		sTrax.setAddress1("!23123");
		sTrax.setMinCode("1231231231");

		byte[] bytesSAR1 = "Any String you want".getBytes();

		when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
		when(this.graduationServiceWebClient.post()).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.uri(String.format(constants.getSchoolGraduation()))).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.uri(String.format(constants.getSchoolNonGraduation()))).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(byte[].class)).thenReturn(Mono.just(bytesSAR1));

		when(this.restService.post(any(String.class), any(), any(), any())).thenReturn(bytesSAR1);
		when(this.restService.post(any(String.class), any(), any())).thenReturn(bytesSAR1);

		when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.uri(String.format(constants.getUpdateSchoolReport()))).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(SchoolReports.class)).thenReturn(Mono.just(new SchoolReports()));

		when(this.tokenUtils.getAccessToken(any())).thenReturn(Pair.of("accessToken", System.currentTimeMillis()));
		when(this.tokenUtils.checkAndGetAccessToken(any())).thenReturn(Pair.of("accessToken", System.currentTimeMillis()));

		Mockito.when(gradStatusService.getStudentListByMinCode(mincode, "accessToken")).thenReturn(sList);
		Mockito.when(schoolService.getTraxSchoolDetails(mincode, "accessToken", exception)).thenReturn(sTrax);
		int numberOfRecord = graduationService.createAndStoreSchoolReports(uniqueList,"REGALG","accessToken");
		assertEquals(2,numberOfRecord);
	}

	@Test
	public void testCreateAndStoreStudentCertificates() {
		UUID studentID = UUID.randomUUID();
		String pen = "123456789";

		GraduationStudentRecord gsr = new GraduationStudentRecord();
		gsr.setStudentID(studentID);
		gsr.setPen(pen);
		gsr.setLegalFirstName("My First Name");
		gsr.setLegalMiddleNames("My Middle Name");
		gsr.setLegalLastName("My Last Name");
		gsr.setStudentGrade("12");
		gsr.setStudentStatus("CUR");
		gsr.setProgramCompletionDate("10/20/2020");

		GradAlgorithmOptionalStudentProgram optionalStudentProgram1 = new GradAlgorithmOptionalStudentProgram();
		optionalStudentProgram1.setOptionalProgramCode("FI");
		optionalStudentProgram1.setStudentID(studentID);
		optionalStudentProgram1.setOptionalProgramID(UUID.randomUUID());
		optionalStudentProgram1.setPen(pen);
		optionalStudentProgram1.setOptionalProgramCompletionDate("10/20/2020");

		GradAlgorithmOptionalStudentProgram optionalStudentProgram2 = new GradAlgorithmOptionalStudentProgram();
		optionalStudentProgram2.setOptionalProgramCode("DD");
		optionalStudentProgram2.setStudentID(studentID);
		optionalStudentProgram2.setOptionalProgramID(UUID.randomUUID());
		optionalStudentProgram2.setPen(pen);
		optionalStudentProgram2.setOptionalProgramCompletionDate("10/20/2020");

		ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationData = new ca.bc.gov.educ.api.graduation.model.dto.GraduationData();
		graduationData.setOptionalGradStatus(Arrays.asList(optionalStudentProgram1, optionalStudentProgram2));
		graduationData.setGraduated(true);

		try {
			gsr.setStudentGradData(new ObjectMapper().writeValueAsString(graduationData));
		} catch (JsonProcessingException e) {
			e.getMessage();
		}

		when(reportService.getGraduationStudentRecordAndGraduationData(pen, "accessToken")).thenReturn(Pair.of(gsr, graduationData));
		when(this.tokenUtils.getAccessToken(any())).thenReturn(Pair.of("accessToken", System.currentTimeMillis()));
		when(this.tokenUtils.checkAndGetAccessToken(any())).thenReturn(Pair.of("accessToken", System.currentTimeMillis()));

		List<ProgramCertificateTranscript> pList = new ArrayList<>();
		ProgramCertificateTranscript pcr = new ProgramCertificateTranscript();
		pcr.setSchoolCategoryCode("01");
		pcr.setCertificateTypeCode("E");
		pList.add(pcr);

		StudentOptionalProgram studentOptionalProgram1 = new StudentOptionalProgram();
		studentOptionalProgram1.setOptionalProgramCode(optionalStudentProgram1.getOptionalProgramCode());
		studentOptionalProgram1.setGraduated(true);

		StudentOptionalProgram studentOptionalProgram2 = new StudentOptionalProgram();
		studentOptionalProgram2.setOptionalProgramCode(optionalStudentProgram2.getOptionalProgramCode());
		studentOptionalProgram2.setGraduated(true);

		ExceptionMessage exception = new ExceptionMessage();
		Mockito.when(reportService.getCertificateList(gsr, graduationData, Arrays.asList(studentOptionalProgram1, studentOptionalProgram2), "accessToken", exception)).thenReturn(pList);

		var result = graduationService.createAndStoreStudentCertificates(pen, true, "accessToken"); // isOverwrite = true -> regenerate(delete and create certs)
		assertNotNull(result);
		assertEquals(Integer.valueOf(1), result);

	}

	@Test
	public void testGetSchoolReports() {
		ExceptionMessage exception = new ExceptionMessage();
		String mincode = "1231231231";
		List<String> uniqueList = new ArrayList<>();
		uniqueList.add(mincode);

		List<GraduationStudentRecord> sList = new ArrayList<>();
		List<GradRequirement> nonList = new ArrayList<>();
		GradRequirement non = new GradRequirement();
		non.setRule("1");
		non.setDescription("ree");
		nonList.add(non);

		ProjectedRunClob pr = new ProjectedRunClob();
		pr.setGraduated(false);
		pr.setNonGradReasons(nonList);

		GraduationStudentRecord gsr = new GraduationStudentRecord();
		gsr.setLegalFirstName("My First Name");
		gsr.setLegalMiddleNames("My Middle Name");
		gsr.setLegalLastName("My Last Name");
		gsr.setStudentGrade("12");
		gsr.setStudentStatus("CUR");
		gsr.setProgramCompletionDate("10/20/2020");

		try {
			gsr.setStudentProjectedGradData(new ObjectMapper().writeValueAsString(pr));
		} catch (JsonProcessingException e) {
			e.getMessage();
		}

		sList.add(gsr);

		gsr = new GraduationStudentRecord();
		gsr.setLegalFirstName("Just Another My First Name");
		gsr.setLegalMiddleNames("Just Another My Middle Name");
		gsr.setLegalLastName("Just Another My Last Name");
		gsr.setStudentGrade("AD");
		gsr.setStudentStatus("CUR");
		gsr.setProgramCompletionDate(null);

		try {
			gsr.setStudentProjectedGradData(new ObjectMapper().writeValueAsString(pr));
		} catch (JsonProcessingException e) {
			e.getMessage();
		}

		sList.add(gsr);

		SchoolTrax sTrax = new SchoolTrax();
		sTrax.setAddress1("!23123");
		sTrax.setMinCode("1231231231");

		byte[] bytesSAR1 = "Any String you want".getBytes();

		when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
		when(this.graduationServiceWebClient.post()).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.uri(String.format(constants.getStudentNonGradProjected()))).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.uri(String.format(constants.getSchoolGraduation()))).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.uri(String.format(constants.getSchoolNonGraduation()))).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(byte[].class)).thenReturn(Mono.just(bytesSAR1));

		when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
		when(this.graduationServiceWebClient.post()).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.uri(String.format(constants.getUpdateSchoolReport()))).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(SchoolReports.class)).thenReturn(Mono.just(new SchoolReports()));

		when(this.restService.post(any(String.class), any(), any(), any())).thenReturn(bytesSAR1);
		when(this.restService.post(any(String.class), any(), any())).thenReturn(bytesSAR1);

		when(gradStatusService.getStudentListByMinCode(mincode, "accessToken")).thenReturn(sList);
		when(schoolService.getTraxSchoolDetails(mincode, "accessToken", exception)).thenReturn(sTrax);
		when(this.tokenUtils.getAccessToken(any())).thenReturn(Pair.of("accessToken", System.currentTimeMillis()));

		byte[] result = graduationService.getSchoolReports(uniqueList,"GRADREG","accessToken");
		assertNotNull(result);
		result = graduationService.getSchoolReports(uniqueList,"NONGRADREG","accessToken");
		assertNotNull(result);
		result = graduationService.getSchoolReports(uniqueList,"NONGRADPRJ","accessToken");
		assertNotNull(result);
	}

	@Test
	public void testGetSchoolReportsException() {
		ExceptionMessage exception = new ExceptionMessage();
		String mincode = "1231231231";
		List<String> uniqueList = new ArrayList<>();
		uniqueList.add(mincode);

		when(this.tokenUtils.getAccessToken(any())).thenReturn(Pair.of("accessToken", System.currentTimeMillis()));

		byte[] result = graduationService.getSchoolReports(uniqueList,"GRADREG","accessToken");
		assertNotNull(result);
		assertEquals(0, result.length);
	}

	@Test
	public void testCreateAndStoreSchoolReports_TVR() {
		ExceptionMessage exception = new ExceptionMessage();
		String mincode = "1231231231";
		List<String> uniqueList = new ArrayList<>();
		uniqueList.add(mincode);

		List<GraduationStudentRecord> sList = new ArrayList<>();
		List<GradRequirement> nonList = new ArrayList<>();
		GradRequirement non = new GradRequirement();
		non.setRule("1");
		non.setDescription("ree");
		nonList.add(non);
		ProjectedRunClob pr = new ProjectedRunClob();
		pr.setGraduated(false);
		pr.setNonGradReasons(nonList);
		GraduationStudentRecord gsr = new GraduationStudentRecord();
		gsr.setLegalFirstName("ada");
		gsr.setLegalMiddleNames("qwe");
		gsr.setLegalLastName("asda");
		gsr.setStudentGrade("12");
		gsr.setStudentStatus("CUR");

		try {
			gsr.setStudentProjectedGradData(new ObjectMapper().writeValueAsString(pr));
		} catch (JsonProcessingException e) {
			e.getMessage();
		}

		sList.add(gsr);
		SchoolTrax sTrax = new SchoolTrax();
		sTrax.setAddress1("!23123");
		sTrax.setMinCode("1231231231");

		byte[] bytesSAR = "Any String you want".getBytes();

		when(this.restService.post(any(String.class), any(), any())).thenReturn(bytesSAR);

		when(this.graduationServiceWebClient.post()).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.uri(String.format(constants.getUpdateSchoolReport()))).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(SchoolReports.class)).thenReturn(Mono.just(new SchoolReports()));

		when(this.tokenUtils.getAccessToken(any())).thenReturn(Pair.of("accessToken", System.currentTimeMillis()));

		Mockito.when(gradStatusService.getStudentListByMinCode(mincode, "accessToken")).thenReturn(sList);
		Mockito.when(schoolService.getTraxSchoolDetails(mincode, "accessToken", exception)).thenReturn(sTrax);
		int numberOfRecord = graduationService.createAndStoreSchoolReports(uniqueList,"TVRRUN","accessToken");
		assertEquals(1,numberOfRecord);
	}

	@Test
	public void testPrepareReportData() {
		final String pen = "1231311313";
		final String accessToken = "accessToken";

		String type = "XML";
		testPrepateReportDataMultiple(pen,type.equalsIgnoreCase("XML"),accessToken,type);

		type = "TRAN";
		testPrepateReportDataMultiple(pen,type.equalsIgnoreCase("XML"),accessToken,type);

		type = "CERT";
		testPrepateReportDataMultiple(pen,type.equalsIgnoreCase("XML"),accessToken,type);

		type = "ACHV";
		testPrepateReportDataMultiple(pen,type.equalsIgnoreCase("XML"),accessToken,type);

	}

	private void testPrepateReportDataMultiple(String pen,boolean xml,String accessToken,String type) {
		ReportData data = new ReportData();
		ca.bc.gov.educ.api.graduation.model.report.GradProgram prg = new ca.bc.gov.educ.api.graduation.model.report.GradProgram();
		Code code = new Code();
		code.setCode("2018-EN");
		prg.setCode(code);
		data.setGradProgram(prg);
		Student std = new Student();
		Pen pens = new Pen();
		pens.setPen(pen);
		std.setPen(pens);
		data.setStudent(std);
		data.setParameters(new HashMap<>());

		if(!type.equalsIgnoreCase("CERT") && !type.equalsIgnoreCase("ACHV")) {
			Mockito.when(reportService.prepareTranscriptData(pen, xml, accessToken, new ExceptionMessage())).thenReturn(data);
		}
		if(type.equalsIgnoreCase("CERT")) {
			Mockito.when(reportService.prepareCertificateData(pen,accessToken, new ExceptionMessage())).thenReturn(data);
		}
		ReportData res = graduationService.prepareReportData(pen,type,accessToken);
		assertNotNull(res);
	}

	@Test
	public void testPrepareReportData_2() {
		final String pen = "1231311313";
		final String accessToken = "accessToken";

		String type = "XML";
		testPrepateReportDataMultiple_2(pen,type.equalsIgnoreCase("XML"),accessToken,type);

		type = "CERT";
		testPrepateReportDataMultiple_2(pen,type.equalsIgnoreCase("XML"),accessToken,type);

		type = "ACHV";
		testPrepateReportDataMultiple_2(pen,type.equalsIgnoreCase("XML"),accessToken,type);

		type = "TRAN";
		testPrepateReportDataMultiple_2(pen,type.equalsIgnoreCase("XML"),accessToken,type);
	}

	private void testPrepateReportDataMultiple_2(String pen,boolean xml,String accessToken,String type) {
		ReportData data = new ReportData();
		ca.bc.gov.educ.api.graduation.model.report.GradProgram prg = new ca.bc.gov.educ.api.graduation.model.report.GradProgram();
		Code code = new Code();
		code.setCode("2018-EN");

		prg.setCode(code);

		Student std = new Student();
		Pen pens = new Pen();
		pens.setPen(pen);
		std.setPen(pens);
		data.setStudent(std);
		data.setGradProgram(prg);
		data.setParameters(new HashMap<>());

		GraduationData graduationData = new GraduationData();
		graduationData.setGradMessage("123123");
		graduationData.setGraduated(true);

		if(!type.equalsIgnoreCase("CERT") && !type.equalsIgnoreCase("ACHV")) {
			Mockito.when(reportService.prepareTranscriptData(graduationData, xml, accessToken, new ExceptionMessage())).thenReturn(data);
		}
		if(type.equalsIgnoreCase("CERT")) {
			Mockito.when(reportService.prepareCertificateData(graduationData,accessToken, new ExceptionMessage())).thenReturn(data);
		}
		ReportData res = graduationService.prepareReportData(graduationData,type,accessToken);
		assertNotNull(res);
	}

	private String readFile(String jsonPath) throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(jsonPath);
		return readInputStream(inputStream);
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
