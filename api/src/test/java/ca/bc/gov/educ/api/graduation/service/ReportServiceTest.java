package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.model.report.ReportData;
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
       
	}
	
	@Test
	public void testSaveStudentTranscriptReport() {
		String studentID = new UUID(1, 1).toString();
		String accessToken = "accessToken";
		String pen="212321123";
		boolean isGraduated= false;
		ReportData data = new ReportData();
		data.setGradMessage("ABC");
		
		GradStudentReports rep = new GradStudentReports();
		rep.setPen(pen);
		byte[] bytesSAR = RandomUtils.nextBytes(20);		
		
		when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.uri(constants.getTranscriptReport())).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(byte[].class)).thenReturn(Mono.just(bytesSAR));
        
        when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.uri(String.format(constants.getUpdateGradStudentReport(), pen))).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(GradStudentReports.class)).thenReturn(Mono.just(rep));		
		reportService.saveStudentTranscriptReportJasper(pen, data, accessToken, UUID.fromString(studentID),exception,isGraduated);	
       
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
        
        
		reportService.getCertificateList(gradResponse, graduationDataStatus, list,accessToken,exception);
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

		reportService.getCertificateList(gradResponse, graduationDataStatus, list,accessToken,exception);
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
        
		reportService.getCertificateList(gradResponse, graduationDataStatus, list,accessToken,exception);
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
		reportService.getCertificateList(gradResponse, graduationDataStatus, list,accessToken,exception);
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
		reportService.getCertificateList(gradResponse, graduationDataStatus, list,accessToken,exception);
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

		reportService.getCertificateList(gradResponse, graduationDataStatus, list,accessToken,exception);
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
		
		reportService.prepareReportData(graduationDataStatus,gradResponse,accessToken);
	}
	
	@Test
	public void testPrepareReportData_nullProgramData() {
		String accessToken = "accessToken";
		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram(null);
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
		
		reportService.prepareReportData(graduationDataStatus,gradResponse,accessToken);
	}
	
	@Test
	public void testPrepareReportData_exams_notnull() {
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
		sc.setCourseCode("FDFE");
		sc.setCredits(4);
		sc.setCustomizedCourseName("SREE");
		sc.setSessionDate("2020/12");
		sc.setEquivOrChallenge("E");
		List<StudentCourse> sList= new ArrayList<>();
		sList.add(sc);
		StudentCourses sCourses = new StudentCourses();
		sCourses.setStudentCourseList(sList);
		
		StudentExam se= new StudentExam();
		sc.setCourseCode("FDFE");
		List<StudentExam> eList= new ArrayList<>();
		eList.add(se);
		StudentExams eCourses = new StudentExams();
		eCourses.setStudentExamList(eList);
		
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
		graduationDataStatus.setStudentExams(eCourses);		graduationDataStatus.setGradStudent(stuObj);
		
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
		reportService.prepareReportData(graduationDataStatus,gradResponse,accessToken);
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
		sc.setCourseCode("FDFE");
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
		
		reportService.prepareReportData(graduationDataStatus,gradResponse,accessToken);
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
		sc.setCourseCode("FDFE");
		sc.setCourseCode("FDFE");
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
		
		reportService.prepareReportData(graduationDataStatus,gradResponse,accessToken);
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
		
		reportService.prepareReportData(graduationDataStatus,gradResponse,accessToken);
	}

	@Test
	public void testStudentAchievementReport() throws Exception {
		GraduationData gradStatus = createGraduationData("json/gradstatus.json");
		List<StudentOptionalProgram> optionalProgram = createStudentOptionalProgramData("json/optionalprograms.json");
		ReportData data = reportService.prepareAchievementReportData(gradStatus,optionalProgram);
		assertNotNull(data);
	}

	protected GraduationData createGraduationData(String jsonPath) throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(jsonPath);
		String json = readInputStream(inputStream);
		return (GraduationData)jsonTransformer.unmarshall(json, GraduationData.class);
	}

	protected List<StudentOptionalProgram> createStudentOptionalProgramData(String jsonPath) throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(jsonPath);
		String json = readInputStream(inputStream);
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
