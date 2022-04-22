package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.model.report.Code;
import ca.bc.gov.educ.api.graduation.model.report.ReportData;
import ca.bc.gov.educ.api.graduation.model.report.Transcript;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import ca.bc.gov.educ.api.graduation.util.GradValidation;
import ca.bc.gov.educ.api.graduation.util.JsonTransformer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@SuppressWarnings({"unchecked","rawtypes"})
public class ReportServiceTest {
	
	@Autowired
	private ReportService reportService;

	@Autowired
	JsonTransformer jsonTransformer;
	
	@Autowired
	private ExceptionMessage exception;
	
	@Autowired
	GradValidation validation;
	
	@MockBean
    WebClient webClient;

    @Autowired
    private EducGraduationApiConstants constants;
    
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
    private Mono<GradCertificateTypes> monoResponse;
	
    @Before
    public void setUp() {
        openMocks(this);
    }    

	@After
    public void tearDown() {

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
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate("2020/02");
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("A");
		gradResponse.setUpdateDate(new Date(System.currentTimeMillis()));

		GradProgram gP = new GradProgram();
		gP.setProgramCode("2018-EN");
		gP.setProgramName("2018 Graduation Program");

		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getProgramNameEndpoint(),gradAlgorithmGraduationStatus.getProgram()))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(GradProgram.class)).thenReturn(Mono.just(gP));
		
		when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.uri(constants.getCertificateReport())).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(byte[].class)).thenReturn(Mono.just(bytesSAR));
        
        when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.uri(String.format(constants.getUpdateGradStudentCertificate(),pen))).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(GradStudentCertificates.class)).thenReturn(Mono.just(rep));		
		reportService.saveStudentCertificateReportJasper(gradResponse, graduationDataStatus, accessToken,pc,exception);
        assertThat(exception.getExceptionName()).isNull();
	}
	
	@Test
	public void testSaveStudentTranscriptReport() {
		String studentID = new UUID(1, 1).toString();
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

		CommonSchool commSch = new CommonSchool();
		commSch.setSchlNo("1231123");
		commSch.setSchoolCategoryCode("02");

		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolCategoryCode(),"06011033"))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(CommonSchool.class)).thenReturn(Mono.just(commSch));

		when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.uri(constants.getTranscriptReport())).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(byte[].class)).thenReturn(Mono.just(bytesSAR));
        
        when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.uri(String.format(constants.getUpdateGradStudentTranscript(),isGraduated))).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(GradStudentReports.class)).thenReturn(Mono.just(rep));		

		reportService.saveStudentTranscriptReportJasper(data, accessToken, UUID.fromString(studentID),exception,isGraduated);
		assertThat(exception.getExceptionName()).isNull();
	}

	@Test
	public void testGetCertificateList_whenAPIisDown_throwsException() {
		String studentID = new UUID(1, 1).toString();
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
		schoolObj.setMinCode("1231123");
		schoolObj.setIndependentDesignation("3");

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

		CommonSchool comSchObj = new CommonSchool();
		comSchObj.setDistNo("123");
		comSchObj.setSchlNo("1123");
		comSchObj.setSchoolCategoryCode("02");

		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolCategoryCode(),"06011033"))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(CommonSchool.class)).thenReturn(Mono.just(comSchObj));

		when(this.webClient.post()).thenThrow(new RuntimeException("Test - API is down"));

		var results = reportService.getCertificateList(gradResponse, graduationDataStatus, list,accessToken,exception);
		assertNotNull(results);
		assertThat(results.isEmpty()).isTrue();

		assertNotNull(exception);
		assertThat(exception.getExceptionName()).isEqualTo("GRAD-GRADUATION-REPORT-API IS DOWN");
	}
	
	@Test
	public void testGetCertificateList() {
		String studentID = new UUID(1, 1).toString();
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
		schoolObj.setMinCode("1231123");
		schoolObj.setIndependentDesignation("3");
		
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

		CommonSchool comSchObj = new CommonSchool();
		comSchObj.setDistNo("123");
		comSchObj.setSchlNo("1123");
		comSchObj.setSchoolCategoryCode("02");

		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolCategoryCode(),"06011033"))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(CommonSchool.class)).thenReturn(Mono.just(comSchObj));

		when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.uri(constants.getCertList())).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(new ParameterizedTypeReference<List<ProgramCertificateTranscript>>(){})).thenReturn(Mono.just(clist));
        
        List<ProgramCertificateTranscript> listCC =reportService.getCertificateList(gradResponse, graduationDataStatus, list,accessToken,exception);
		assertThat(listCC).hasSize(1);
	}
	
	@Test
	public void testGetCertificateList_PFProgram() {
		String studentID = new UUID(1, 1).toString();
		String accessToken = "accessToken";
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-PF");
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
		schoolObj.setMinCode("1231123");
		schoolObj.setIndependentDesignation("3");
		
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
		
		when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.uri(constants.getCertList())).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(new ParameterizedTypeReference<List<ProgramCertificateTranscript>>(){})).thenReturn(Mono.just(clist));
        
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("DD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setProgramCode("2018-EN");
		spgm.setStudentID(UUID.fromString(studentID));
		List<StudentOptionalProgram> list = new ArrayList<StudentOptionalProgram>();
		list.add(spgm);

		CommonSchool comSchObj = new CommonSchool();
		comSchObj.setDistNo("123");
		comSchObj.setSchlNo("1123");
		comSchObj.setSchoolCategoryCode("02");

		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolCategoryCode(),"06011033"))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(CommonSchool.class)).thenReturn(Mono.just(comSchObj));

		List<ProgramCertificateTranscript> listCC = reportService.getCertificateList(gradResponse, graduationDataStatus, list,accessToken,exception);
		assertThat(listCC).hasSize(1);
	}
	
	@Test
	public void testGetCertificateList_PFProgram_nodogwood() {
		String studentID = new UUID(1, 1).toString();
		String accessToken = "accessToken";
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-PF");
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
		schoolObj.setMinCode("1231123");
		schoolObj.setIndependentDesignation("3");
		
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

		CommonSchool comSchObj = new CommonSchool();
		comSchObj.setDistNo("123");
		comSchObj.setSchlNo("1123");
		comSchObj.setSchoolCategoryCode("02");

		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolCategoryCode(),"06011033"))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(CommonSchool.class)).thenReturn(Mono.just(comSchObj));

		List<ProgramCertificateTranscript> clist= new ArrayList<ProgramCertificateTranscript>();
		ProgramCertificateTranscript pc = new ProgramCertificateTranscript();
		pc.setCertificateTypeCode("E");
		pc.setSchoolCategoryCode(" ");
		clist.add(pc);
		
		when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.uri(constants.getCertList())).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(new ParameterizedTypeReference<List<ProgramCertificateTranscript>>(){})).thenReturn(Mono.just(clist));

		List<ProgramCertificateTranscript> listCC= reportService.getCertificateList(gradResponse, graduationDataStatus, list,accessToken,exception);
		assertThat(listCC).hasSize(1);
	}
	
	@Test
	public void testGetCertificateList_emptyOptionalProgram() {
		UUID studentID = new UUID(1, 1);
		String accessToken = "accessToken";
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setStudentID(studentID);
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");

		CommonSchool comSchObj = new CommonSchool();
		comSchObj.setDistNo("123");
		comSchObj.setSchlNo("1123");
		comSchObj.setSchoolCategoryCode("02");

		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolCategoryCode(),"06011033"))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(CommonSchool.class)).thenReturn(Mono.just(comSchObj));

		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");
		
		School schoolObj = new School();
		schoolObj.setMinCode("1231123");
		schoolObj.setIndependentDesignation("3");
		
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
		
		when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.uri(constants.getCertList())).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(new ParameterizedTypeReference<List<ProgramCertificateTranscript>>(){})).thenReturn(Mono.just(clist));
		
		List<StudentOptionalProgram> list = new ArrayList<StudentOptionalProgram>();
		List<ProgramCertificateTranscript> listCC = reportService.getCertificateList(gradResponse, graduationDataStatus, list,accessToken,exception);
		assertThat(listCC).hasSize(1);
	}
	
	@Test
	public void testGetCertificateList_FrenchImmersion() {
		String studentID = new UUID(1, 1).toString();
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
		schoolObj.setMinCode("1231123");
		schoolObj.setIndependentDesignation("3");
		
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
		
		when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.uri(constants.getCertList())).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(new ParameterizedTypeReference<List<ProgramCertificateTranscript>>(){})).thenReturn(Mono.just(clist));

		CommonSchool comSchObj = new CommonSchool();
		comSchObj.setDistNo("123");
		comSchObj.setSchlNo("1123");
		comSchObj.setSchoolCategoryCode("02");

		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolCategoryCode(),"06011033"))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(CommonSchool.class)).thenReturn(Mono.just(comSchObj));

		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("FI");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setProgramCode("2018-EN");
		spgm.setStudentID(UUID.fromString(studentID));
		spgm.setOptionalProgramCompletionDate("2020-09-01");
		List<StudentOptionalProgram> list = new ArrayList<StudentOptionalProgram>();
		list.add(spgm);
		List<ProgramCertificateTranscript> listCC =  reportService.getCertificateList(gradResponse, graduationDataStatus, list,accessToken,exception);
		assertThat(listCC).hasSize(1);
	}
	
	@Test
	public void testGetCertificateList_FrenchImmersion_nullProgramCompletionDate() {
		String studentID = new UUID(1, 1).toString();
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
		schoolObj.setMinCode("1231123");
		schoolObj.setIndependentDesignation("3");
		
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
		
		when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.uri(constants.getCertList())).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(new ParameterizedTypeReference<List<ProgramCertificateTranscript>>(){})).thenReturn(Mono.just(clist));

		CommonSchool comSchObj = new CommonSchool();
		comSchObj.setDistNo("123");
		comSchObj.setSchlNo("1123");
		comSchObj.setSchoolCategoryCode("02");

		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolCategoryCode(),"06011033"))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(CommonSchool.class)).thenReturn(Mono.just(comSchObj));

		List<ProgramCertificateTranscript> listCC = reportService.getCertificateList(gradResponse, graduationDataStatus, list,accessToken,exception);
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
		schoolObj.setMinCode("1231123");
		schoolObj.setIndependentDesignation("1");
		
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
		
		GradProgram gP = new GradProgram();
		gP.setProgramCode("2018-EN");
		gP.setProgramName("2018 Graduation Program");
		
		SpecialCase sp = new SpecialCase();
		sp.setSpCase("A");
		sp.setLabel("AEG");
		
		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getSpecialCase(),"A"))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(SpecialCase.class)).thenReturn(Mono.just(sp));
		
		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getProgramNameEndpoint(),gradAlgorithmGraduationStatus.getProgram()))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(GradProgram.class)).thenReturn(Mono.just(gP));

		CommonSchool commSch = new CommonSchool();
		commSch.setSchlNo("1231123");
		commSch.setSchoolCategoryCode("02");

		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolCategoryCode(),"06011033"))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(CommonSchool.class)).thenReturn(Mono.just(commSch));

		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");
		gradResponse.setUpdateDate(new Date(System.currentTimeMillis()));
		
		List<CodeDTO> optionalProgram = new ArrayList<CodeDTO>();
		CodeDTO cDto = new CodeDTO();
		cDto.setCode("FI");
		cDto.setName("French Immersion");
		optionalProgram.add(cDto);
		
		ReportData dta = reportService.prepareTranscriptData(graduationDataStatus,gradResponse,accessToken,exception);
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
		schoolObj.setMinCode("1231123");
		schoolObj.setIndependentDesignation("1");

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
		
		GradProgram gP = new GradProgram();
		gP.setProgramCode("2018-EN");
		gP.setProgramName("2018 Graduation Program");
		
		SpecialCase sp = new SpecialCase();
		sp.setSpCase("A");
		sp.setLabel("AEG");
		
		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getSpecialCase(),"A"))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(SpecialCase.class)).thenReturn(Mono.just(sp));
		
		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getProgramNameEndpoint(),gradAlgorithmGraduationStatus.getProgram()))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(GradProgram.class)).thenReturn(Mono.just(gP));

		CommonSchool commSch = new CommonSchool();
		commSch.setSchlNo("1231123");
		commSch.setSchoolCategoryCode("02");

		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolCategoryCode(),"06011033"))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(CommonSchool.class)).thenReturn(Mono.just(commSch));
		
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
		gradResponse.setUpdateDate(new Date(System.currentTimeMillis()));
		
		ReportData dta = reportService.prepareTranscriptData(graduationDataStatus,gradResponse,accessToken,exception);
		assertThat(dta).isNotNull();
	}
	
	@Test
	public void testPrepareReportData_exams_notnull() {
		String accessToken = "accessToken";
		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = getGradAlgorithmGraduationStatus("1996-EN");
		
		School schoolObj = new School();
		schoolObj.setMinCode("1231123");
		schoolObj.setIndependentDesignation("1");
		
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
		
		GradProgram gP = new GradProgram();
		gP.setProgramCode("2018-EN");
		gP.setProgramName("2018 Graduation Program");
		
		SpecialCase sp = new SpecialCase();
		sp.setSpCase("A");
		sp.setLabel("AEG");
		
		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getSpecialCase(),"A"))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(SpecialCase.class)).thenReturn(Mono.just(sp));
		
		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getProgramNameEndpoint(),gradAlgorithmGraduationStatus.getProgram()))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(GradProgram.class)).thenReturn(Mono.just(gP));

		CommonSchool commSch = new CommonSchool();
		commSch.setSchlNo("06011033");
		commSch.setSchoolCategoryCode("02");

		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolCategoryCode(),"06011033"))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(CommonSchool.class)).thenReturn(Mono.just(commSch));

		List<CodeDTO> optionalProgram = new ArrayList<CodeDTO>();
		CodeDTO cDto = new CodeDTO();
		cDto.setCode("FI");
		cDto.setName("French Immersion");
		optionalProgram.add(cDto);
		
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("1996-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");
		gradResponse.setUpdateDate(new Date(System.currentTimeMillis()));
		ReportData dta = reportService.prepareTranscriptData(graduationDataStatus,gradResponse,accessToken,exception);
		assertThat(dta).isNotNull();
	}

	@Test
	public void testPrepareReportData_exams_notnull_1950() {
		String accessToken = "accessToken";
		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = getGradAlgorithmGraduationStatus("1950");

		School schoolObj = new School();
		schoolObj.setMinCode("1231123");
		schoolObj.setIndependentDesignation("1");

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

		GradProgram gP = new GradProgram();
		gP.setProgramCode("1950");
		gP.setProgramName("2018 Graduation Program");

		SpecialCase sp = new SpecialCase();
		sp.setSpCase("A");
		sp.setLabel("AEG");

		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getSpecialCase(),"A"))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(SpecialCase.class)).thenReturn(Mono.just(sp));

		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getProgramNameEndpoint(),gradAlgorithmGraduationStatus.getProgram()))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(GradProgram.class)).thenReturn(Mono.just(gP));

		CommonSchool commSch = new CommonSchool();
		commSch.setSchlNo("06011033");
		commSch.setSchoolCategoryCode("02");

		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolCategoryCode(),"06011033"))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(CommonSchool.class)).thenReturn(Mono.just(commSch));

		List<CodeDTO> optionalProgram = new ArrayList<CodeDTO>();
		CodeDTO cDto = new CodeDTO();
		cDto.setCode("FI");
		cDto.setName("French Immersion");
		optionalProgram.add(cDto);

		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("1950");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");
		gradResponse.setUpdateDate(new Date(System.currentTimeMillis()));
		ReportData dta = reportService.prepareTranscriptData(graduationDataStatus,gradResponse,accessToken,exception);
		assertThat(dta).isNotNull();
	}

	@Test
	public void testPrepareReportData_exams_notnull_2004() {
		String accessToken = "accessToken";
		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = getGradAlgorithmGraduationStatus("2004-EN");

		School schoolObj = new School();
		schoolObj.setMinCode("1231123");
		schoolObj.setIndependentDesignation("1");

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

		GradProgram gP = new GradProgram();
		gP.setProgramCode("2018-EN");
		gP.setProgramName("2018 Graduation Program");

		SpecialCase sp = new SpecialCase();
		sp.setSpCase("A");
		sp.setLabel("AEG");

		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getSpecialCase(),"A"))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(SpecialCase.class)).thenReturn(Mono.just(sp));

		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getProgramNameEndpoint(),gradAlgorithmGraduationStatus.getProgram()))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(GradProgram.class)).thenReturn(Mono.just(gP));

		CommonSchool commSch = new CommonSchool();
		commSch.setSchlNo("06011033");
		commSch.setSchoolCategoryCode("02");

		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolCategoryCode(),"06011033"))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(CommonSchool.class)).thenReturn(Mono.just(commSch));

		List<CodeDTO> optionalProgram = new ArrayList<CodeDTO>();
		CodeDTO cDto = new CodeDTO();
		cDto.setCode("FI");
		cDto.setName("French Immersion");
		optionalProgram.add(cDto);

		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2004-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");
		gradResponse.setUpdateDate(new Date(System.currentTimeMillis()));
		ReportData dta = reportService.prepareTranscriptData(graduationDataStatus,gradResponse,accessToken,exception);
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
		schoolObj.setMinCode("1231123");
		schoolObj.setIndependentDesignation("3");
		
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
		
		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getSpecialCase(),"A"))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(SpecialCase.class)).thenReturn(Mono.just(sp));
		
		GradProgram gP = new GradProgram();
		gP.setProgramCode("2018-EN");
		gP.setProgramName("2018 Graduation Program");
		
		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getProgramNameEndpoint(),gradAlgorithmGraduationStatus.getProgram()))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(GradProgram.class)).thenReturn(Mono.just(gP));

		CommonSchool commSch = new CommonSchool();
		commSch.setSchlNo("1231123");
		commSch.setSchoolCategoryCode("02");

		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolCategoryCode(),"06011033"))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(CommonSchool.class)).thenReturn(Mono.just(commSch));

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
		gradResponse.setUpdateDate(new Date(System.currentTimeMillis()));
		
		ReportData dta = reportService.prepareTranscriptData(graduationDataStatus,gradResponse,accessToken,exception);
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
		schoolObj.setMinCode("1231123");
		schoolObj.setIndependentDesignation("4");
		
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
		
		GradProgram gP = new GradProgram();
		gP.setProgramCode("2018-EN");
		gP.setProgramName("2018 Graduation Program");
		
		SpecialCase sp = new SpecialCase();
		sp.setSpCase("A");
		sp.setLabel("AEG");
		
		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getSpecialCase(),"A"))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(SpecialCase.class)).thenReturn(Mono.just(sp));
		
		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getProgramNameEndpoint(),gradAlgorithmGraduationStatus.getProgram()))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(GradProgram.class)).thenReturn(Mono.just(gP));

		CommonSchool commSch = new CommonSchool();
		commSch.setSchlNo("1231123");
		commSch.setSchoolCategoryCode("02");

		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolCategoryCode(),"06011033"))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(CommonSchool.class)).thenReturn(Mono.just(commSch));

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
		gradResponse.setUpdateDate(new Date(System.currentTimeMillis()));
		
		ReportData dta = reportService.prepareTranscriptData(graduationDataStatus,gradResponse,accessToken,exception);
		assertThat(dta).isNotNull();
	}
	

	public void testPrepareReportData_Desig_2() {
		String accessToken = "accessToken";
		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");
		
		School schoolObj = new School();
		schoolObj.setMinCode("1231123");
		schoolObj.setIndependentDesignation("2");
		
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
		graduationDataStatus.setStudentAssessments(sAssessments);		graduationDataStatus.setGradStudent(stuObj);
		
		GradProgram gP = new GradProgram();
		gP.setProgramCode("2018-EN");
		gP.setProgramName("2018 Graduation Program");
		
		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getProgramNameEndpoint(),gradAlgorithmGraduationStatus.getProgram()))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(GradProgram.class)).thenReturn(Mono.just(gP));
		
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
		gradResponse.setUpdateDate(new Date(System.currentTimeMillis()));
		
		ReportData dta = reportService.prepareTranscriptData(graduationDataStatus,gradResponse,accessToken,exception);
		assertThat(dta).isNotNull();
	}

	@Test
	public void testSaveStudentAchievementReport() throws Exception {
		String studentID = new UUID(1, 1).toString();
		String accessToken = "accessToken";
		String pen = "212321123";
		boolean isGraduated = false;
		ReportData data = createReportData("json/reportdataAchv.json");
		GradStudentReports rep = new GradStudentReports();
		rep.setPen(pen);
		byte[] bytesSAR = RandomUtils.nextBytes(20);
		ExceptionMessage exception = new ExceptionMessage();
		CommonSchool commSch = new CommonSchool();
		commSch.setSchlNo("1231123");
		commSch.setSchoolCategoryCode("02");

		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolCategoryCode(), "06011033"))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(CommonSchool.class)).thenReturn(Mono.just(commSch));

		when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.uri(constants.getAchievementReport())).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(byte[].class)).thenReturn(Mono.just(bytesSAR));

		when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.uri(String.format(constants.getUpdateGradStudentReport(),isGraduated))).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(GradStudentReports.class)).thenReturn(Mono.just(rep));

		reportService.saveStudentAchivementReportJasper(pen, data, accessToken, UUID.fromString(studentID), exception, isGraduated);
		assertThat(exception.getExceptionName()).isNull();
	}

	@Test
	public void testStudentAchievementReport() throws Exception {
		GraduationData gradStatus = createGraduationData("json/gradstatus.json");
		List<StudentOptionalProgram> optionalProgram = createStudentOptionalProgramData("json/optionalprograms.json");

		GradProgram gradProgram = new GradProgram();
		gradProgram.setProgramCode("2018-EN");
		gradProgram.setProgramName("2018 Graduation Program");

		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getProgramNameEndpoint(),gradStatus.getGradStudent().getProgram()))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(GradProgram.class)).thenReturn(Mono.just(gradProgram));

		ReportData data = reportService.prepareAchievementReportData(gradStatus,optionalProgram,null, exception);
		assertNotNull(data);
	}

	@Test
	public void testReportDataByPen() throws Exception {
		GraduationData gradStatus = createGraduationData("json/gradstatus.json");
		assertNotNull(gradStatus);
		String pen = gradStatus.getGradStudent().getPen();
		GradSearchStudent gradSearchStudent = new GradSearchStudent();
		gradSearchStudent.setPen(pen);
		gradSearchStudent.setStudentID(gradStatus.getGradStudent().getStudentID());

		final ParameterizedTypeReference<List<GradSearchStudent>> gradSearchStudentResponseType = new ParameterizedTypeReference<>() {
		};

		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getPenStudentApiByPenUrl(),pen))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(gradSearchStudentResponseType)).thenReturn(Mono.just(List.of(gradSearchStudent)));

		GraduationStudentRecord graduationStudentRecord = new GraduationStudentRecord();
		graduationStudentRecord.setPen(pen);
		graduationStudentRecord.setProgramCompletionDate("2003/01");
		graduationStudentRecord.setStudentID(UUID.fromString(gradSearchStudent.getStudentID()));
		graduationStudentRecord.setUpdateDate(new Date(System.currentTimeMillis()));

		String studentGradData = readFile("json/gradstatus.json");
		assertNotNull(studentGradData);
		graduationStudentRecord.setStudentGradData(studentGradData);

		GradProgram gradProgram = new GradProgram();
		gradProgram.setProgramCode("2018-EN");
		gradProgram.setProgramName("2018 Graduation Program");

		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getProgramNameEndpoint(),gradStatus.getGradStudent().getProgram()))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(GradProgram.class)).thenReturn(Mono.just(gradProgram));

		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getReadGradStudentRecord(),graduationStudentRecord.getStudentID().toString()))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(GraduationStudentRecord.class)).thenReturn(Mono.just(graduationStudentRecord));

		CommonSchool commSch = new CommonSchool();
		commSch.setSchlNo("1231123");
		commSch.setSchoolCategoryCode("02");

		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolCategoryCode(), "00502001"))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(CommonSchool.class)).thenReturn(Mono.just(commSch));

		ProgramCertificateTranscript programCertificateTranscript = new ProgramCertificateTranscript();
		programCertificateTranscript.setPcId(UUID.randomUUID());
		programCertificateTranscript.setGraduationProgramCode(gradProgram.getProgramCode());
		programCertificateTranscript.setSchoolCategoryCode(commSch.getSchoolCategoryCode());
		programCertificateTranscript.setCertificateTypeCode("E");

		ProgramCertificateReq req = new ProgramCertificateReq();
		req.setProgramCode(gradProgram.getProgramCode());
		req.setSchoolCategoryCode(commSch.getSchoolCategoryCode());

		when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.uri(constants.getTranscript())).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(ProgramCertificateTranscript.class)).thenReturn(Mono.just(programCertificateTranscript));

		ReportData transcriptData = reportService.prepareTranscriptData(pen, "accessToken", exception);
		assertNotNull(transcriptData);
		assertNotNull(transcriptData.getStudent());
		assertNotNull(transcriptData.getTranscript());

		ReportData certificateData = reportService.prepareCertificateData(pen, "accessToken", exception);
		assertNotNull(certificateData);
		assertNotNull(certificateData.getStudent());
		assertNotNull(certificateData.getCertificate());

	}

	@Test
	public void testReportDataByGraduationData() throws Exception {
		GraduationData gradStatus = createGraduationData("json/gradstatus.json");
		assertNotNull(gradStatus);
		String pen = gradStatus.getGradStudent().getPen();

		GraduationStudentRecord graduationStudentRecord = new GraduationStudentRecord();
		graduationStudentRecord.setPen(pen);
		graduationStudentRecord.setProgramCompletionDate("2003/01");
		graduationStudentRecord.setStudentID(UUID.fromString(gradStatus.getGradStudent().getStudentID()));
		graduationStudentRecord.setUpdateDate(new Date(System.currentTimeMillis()));

		String studentGradData = readFile("json/gradstatus.json");
		assertNotNull(studentGradData);
		graduationStudentRecord.setStudentGradData(studentGradData);

		GradProgram gradProgram = new GradProgram();
		gradProgram.setProgramCode("2018-EN");
		gradProgram.setProgramName("2018 Graduation Program");

		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getProgramNameEndpoint(),gradStatus.getGradStudent().getProgram()))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(GradProgram.class)).thenReturn(Mono.just(gradProgram));

		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getReadGradStudentRecord(),graduationStudentRecord.getStudentID().toString()))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(GraduationStudentRecord.class)).thenReturn(Mono.just(graduationStudentRecord));

		CommonSchool commSch = new CommonSchool();
		commSch.setSchlNo("1231123");
		commSch.setSchoolCategoryCode("02");

		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolCategoryCode(), "00502001"))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(CommonSchool.class)).thenReturn(Mono.just(commSch));

		ProgramCertificateTranscript programCertificateTranscript = new ProgramCertificateTranscript();
		programCertificateTranscript.setPcId(UUID.randomUUID());
		programCertificateTranscript.setGraduationProgramCode(gradProgram.getProgramCode());
		programCertificateTranscript.setSchoolCategoryCode("02");
		programCertificateTranscript.setCertificateTypeCode("E");

		when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.uri(constants.getTranscript())).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(ProgramCertificateTranscript.class)).thenReturn(Mono.just(programCertificateTranscript));

		ReportData data = reportService.prepareTranscriptData(gradStatus, "accessToken", exception);
		assertNotNull(data);
		assertNotNull(data.getStudent());
		assertNotNull(data.getTranscript());

	}

	protected GraduationData createGraduationData(String jsonPath) throws Exception {
		String json = readFile(jsonPath);
		return (GraduationData)jsonTransformer.unmarshall(json, GraduationData.class);
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

	protected List<StudentOptionalProgram> createStudentOptionalProgramData(String jsonPath) throws Exception {
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
