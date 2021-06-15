package ca.bc.gov.educ.api.graduation.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.commons.lang3.RandomUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.graduation.model.dto.CodeDTO;
import ca.bc.gov.educ.api.graduation.model.dto.GradAlgorithmGraduationStatus;
import ca.bc.gov.educ.api.graduation.model.dto.GradCertificateTypes;
import ca.bc.gov.educ.api.graduation.model.dto.GradProgram;
import ca.bc.gov.educ.api.graduation.model.dto.GradSearchStudent;
import ca.bc.gov.educ.api.graduation.model.dto.GradStudentCertificates;
import ca.bc.gov.educ.api.graduation.model.dto.GradStudentReports;
import ca.bc.gov.educ.api.graduation.model.dto.GradStudentSpecialProgram;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationData;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationMessages;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationStatus;
import ca.bc.gov.educ.api.graduation.model.dto.ReportData;
import ca.bc.gov.educ.api.graduation.model.dto.School;
import ca.bc.gov.educ.api.graduation.model.dto.SpecialGradAlgorithmGraduationStatus;
import ca.bc.gov.educ.api.graduation.model.dto.StudentAssessment;
import ca.bc.gov.educ.api.graduation.model.dto.StudentAssessments;
import ca.bc.gov.educ.api.graduation.model.dto.StudentCourse;
import ca.bc.gov.educ.api.graduation.model.dto.StudentCourses;
import ca.bc.gov.educ.api.graduation.model.dto.StudentDemographics;
import ca.bc.gov.educ.api.graduation.model.dto.StudentExam;
import ca.bc.gov.educ.api.graduation.model.dto.StudentExams;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import ca.bc.gov.educ.api.graduation.util.GradValidation;
import reactor.core.publisher.Mono;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class ReportServiceTest {
	
	@Autowired
	private ReportService reportService;
	
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
	
    @Before
    public void setUp() {
        openMocks(this);
    }    

	@After
    public void tearDown() {

    }
	
	@Test
	public void testSaveStudentAchievementReport() {
		String studentID = new UUID(1, 1).toString();
		String accessToken = "accessToken";
		String pen="212321123";
		ReportData data = new ReportData();
		data.setStudentName("ABC");
		
		GradStudentReports rep = new GradStudentReports();
		rep.setPen(pen);
		byte[] bytesSAR = RandomUtils.nextBytes(20);		
		
		when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.uri(constants.getAchievementReport())).thenReturn(this.requestBodyUriMock);
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
		reportService.saveStudentAchievementReport(pen, data, accessToken, UUID.fromString(studentID));	
       
	}
	
	@Test
	public void testSaveStudentCertificateReport() {
		String studentID = new UUID(1, 1).toString();
		String accessToken = "accessToken";
		String pen="212321123";
		String certificateType="E";
		ReportData data = new ReportData();
		data.setStudentName("ABC");
		
		GradStudentCertificates rep = new GradStudentCertificates();
		rep.setPen(pen);
		byte[] bytesSAR = RandomUtils.nextBytes(20);		
		
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
		reportService.saveStudentCertificateReport(pen, data, accessToken,certificateType, UUID.fromString(studentID));	
       
	}
	
	@Test
	public void testSaveStudentTranscriptReport() {
		String studentID = new UUID(1, 1).toString();
		String accessToken = "accessToken";
		String pen="212321123";
		ReportData data = new ReportData();
		data.setStudentName("ABC");
		
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
		reportService.saveStudentTranscriptReport(pen, data, accessToken, UUID.fromString(studentID));	
       
	}
	
	@Test
	public void testCheckSchoolForCertDecision() {
		GradAlgorithmGraduationStatus gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStatus();
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
		
		
		reportService.checkSchoolForCertDecision(graduationDataStatus, new ArrayList<>());
	}
	
	@Test
	public void testCheckSchoolForCertDecision_Desig_9() {
		GradAlgorithmGraduationStatus gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStatus();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");
		
		School schoolObj = new School();
		schoolObj.setMinCode("1231123");
		schoolObj.setIndependentDesignation("9");
		
		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setSchool(schoolObj);
		graduationDataStatus.setStudentCourses(null);
		
		
		reportService.checkSchoolForCertDecision(graduationDataStatus, new ArrayList<>());
	}
	
	@Test
	public void testCheckSchoolForCertDecision_Desig_2() {
		GradAlgorithmGraduationStatus gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStatus();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");
		
		School schoolObj = new School();
		schoolObj.setMinCode("1231123");
		schoolObj.setIndependentDesignation("2");
		
		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setSchool(schoolObj);
		graduationDataStatus.setStudentCourses(null);
		
		
		reportService.checkSchoolForCertDecision(graduationDataStatus, new ArrayList<>());
	}
	
	@Test
	public void testGetCertificateList() {
		String studentID = new UUID(1, 1).toString();
		GraduationStatus gradResponse = new GraduationStatus();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");
		
		GradAlgorithmGraduationStatus gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStatus();
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
		
		GradStudentSpecialProgram spgm = new GradStudentSpecialProgram();
		spgm.setPen("123090109");
		spgm.setSpecialProgramCode("DD");
		spgm.setSpecialProgramName("International Bacculaurette");
		spgm.setMainProgramCode("2018-EN");
		spgm.setStudentID(UUID.fromString(studentID));
		List<GradStudentSpecialProgram> list = new ArrayList<GradStudentSpecialProgram>();
		list.add(spgm);
		reportService.getCertificateList(new ArrayList<>(), gradResponse, graduationDataStatus, list);
	}
	
	@Test
	public void testGetCertificateList_PFProgram() {
		String studentID = new UUID(1, 1).toString();
		GraduationStatus gradResponse = new GraduationStatus();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-PF");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");
		
		GradAlgorithmGraduationStatus gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStatus();
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
		
		GradStudentSpecialProgram spgm = new GradStudentSpecialProgram();
		spgm.setPen("123090109");
		spgm.setSpecialProgramCode("DD");
		spgm.setSpecialProgramName("International Bacculaurette");
		spgm.setMainProgramCode("2018-EN");
		spgm.setStudentID(UUID.fromString(studentID));
		List<GradStudentSpecialProgram> list = new ArrayList<GradStudentSpecialProgram>();
		list.add(spgm);
		reportService.getCertificateList(new ArrayList<>(), gradResponse, graduationDataStatus, list);
	}
	
	@Test
	public void testGetCertificateList_PFProgram_nodogwood() {
		String studentID = new UUID(1, 1).toString();
		GraduationStatus gradResponse = new GraduationStatus();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-PF");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");
		
		GradAlgorithmGraduationStatus gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStatus();
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
		
		GradStudentSpecialProgram spgm = new GradStudentSpecialProgram();
		spgm.setPen("123090109");
		spgm.setSpecialProgramCode("DD");
		spgm.setSpecialProgramName("International Bacculaurette");
		spgm.setMainProgramCode("2018-EN");
		spgm.setStudentID(UUID.fromString(studentID));
		List<GradStudentSpecialProgram> list = new ArrayList<GradStudentSpecialProgram>();
		list.add(spgm);
		reportService.getCertificateList(new ArrayList<>(), gradResponse, graduationDataStatus, list);
	}
	
	@Test
	public void testGetCertificateList_emptySpecialProgram() {
		String studentID = new UUID(1, 1).toString();
		GraduationStatus gradResponse = new GraduationStatus();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");
		
		GradAlgorithmGraduationStatus gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStatus();
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
		
		List<GradStudentSpecialProgram> list = new ArrayList<GradStudentSpecialProgram>();
		reportService.getCertificateList(new ArrayList<>(), gradResponse, graduationDataStatus, list);
	}
	
	@Test
	public void testGetCertificateList_FrenchImmersion() {
		String studentID = new UUID(1, 1).toString();
		GraduationStatus gradResponse = new GraduationStatus();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");
		
		GradAlgorithmGraduationStatus gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStatus();
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
		
		GradStudentSpecialProgram spgm = new GradStudentSpecialProgram();
		spgm.setPen("123090109");
		spgm.setSpecialProgramCode("FI");
		spgm.setSpecialProgramName("International Bacculaurette");
		spgm.setMainProgramCode("2018-EN");
		spgm.setStudentID(UUID.fromString(studentID));
		spgm.setSpecialProgramCompletionDate("2020-09-01");
		List<GradStudentSpecialProgram> list = new ArrayList<GradStudentSpecialProgram>();
		list.add(spgm);
		reportService.getCertificateList(new ArrayList<>(), gradResponse, graduationDataStatus, list);
	}
	
	@Test
	public void testGetCertificateList_FrenchImmersion_nullProgramCompletionDate() {
		String studentID = new UUID(1, 1).toString();
		GraduationStatus gradResponse = new GraduationStatus();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");
		
		GradAlgorithmGraduationStatus gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStatus();
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
		
		GradStudentSpecialProgram spgm = new GradStudentSpecialProgram();
		spgm.setPen("123090109");
		spgm.setSpecialProgramCode("FI");
		spgm.setSpecialProgramName("International Bacculaurette");
		spgm.setMainProgramCode("2018-EN");
		spgm.setStudentID(UUID.fromString(studentID));
		spgm.setSpecialProgramCompletionDate(null);
		List<GradStudentSpecialProgram> list = new ArrayList<GradStudentSpecialProgram>();
		list.add(spgm);
		reportService.getCertificateList(new ArrayList<>(), gradResponse, graduationDataStatus, list);
	}
	
	@Test
	public void testPrepareReportData() {
		String accessToken = "accessToken";
		GradAlgorithmGraduationStatus gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStatus();
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
		List<StudentCourse> sList= new ArrayList<>();
		sList.add(sc);
		StudentCourses sCourses = new StudentCourses();
		sCourses.setStudentCourseList(sList);
		
		StudentAssessment sA= new StudentAssessment();
		sA.setAssessmentCode("FDFE");
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
		
		List<CodeDTO> specialProgram = new ArrayList<CodeDTO>();
		CodeDTO cDto = new CodeDTO();
		cDto.setCode("FI");
		cDto.setName("French Immersion");
		specialProgram.add(cDto);
		
		reportService.prepareReportData(graduationDataStatus, accessToken, specialProgram);
	}
	
	@Test
	public void testPrepareReportData_nullProgramData() {
		String accessToken = "accessToken";
		GradAlgorithmGraduationStatus gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStatus();
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
		List<StudentCourse> sList= new ArrayList<>();
		sList.add(sc);
		StudentCourses sCourses = new StudentCourses();
		sCourses.setStudentCourseList(sList);
		
		StudentAssessment sA= new StudentAssessment();
		sA.setAssessmentCode("FDFE");
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
		
		List<CodeDTO> specialProgram = new ArrayList<CodeDTO>();
		CodeDTO cDto = new CodeDTO();
		cDto.setCode("FI");
		cDto.setName("French Immersion");
		specialProgram.add(cDto);
		
		reportService.prepareReportData(graduationDataStatus, accessToken, specialProgram);
	}
	
	@Test
	public void testPrepareReportData_exams_notnull() {
		String accessToken = "accessToken";
		GradAlgorithmGraduationStatus gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStatus();
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
		
		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getProgramNameEndpoint(),gradAlgorithmGraduationStatus.getProgram()))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(GradProgram.class)).thenReturn(Mono.just(gP));
		
		List<CodeDTO> specialProgram = new ArrayList<CodeDTO>();
		CodeDTO cDto = new CodeDTO();
		cDto.setCode("FI");
		cDto.setName("French Immersion");
		specialProgram.add(cDto);
		
		reportService.prepareReportData(graduationDataStatus, accessToken, specialProgram);
	}
	
	@Test
	public void testPrepareReportData_Desig_3() {
		String accessToken = "accessToken";
		GradAlgorithmGraduationStatus gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStatus();
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
		List<StudentCourse> sList= new ArrayList<>();
		sList.add(sc);
		StudentCourses sCourses = new StudentCourses();
		sCourses.setStudentCourseList(sList);
		
		StudentAssessment sA= new StudentAssessment();
		sA.setAssessmentCode("FDFE");
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
		
		List<CodeDTO> specialProgram = new ArrayList<CodeDTO>();
		CodeDTO cDto = new CodeDTO();
		cDto.setCode("FI");
		cDto.setName("French Immersion");
		specialProgram.add(cDto);
		
		reportService.prepareReportData(graduationDataStatus, accessToken, specialProgram);
	}
	
	@Test
	public void testPrepareReportData_Desig_4() {
		String accessToken = "accessToken";
		GradAlgorithmGraduationStatus gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStatus();
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
		List<StudentCourse> sList= new ArrayList<>();
		sList.add(sc);
		StudentCourses sCourses = new StudentCourses();
		sCourses.setStudentCourseList(sList);
		
		StudentAssessment sA= new StudentAssessment();
		sA.setAssessmentCode("FDFE");
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
		
		List<CodeDTO> specialProgram = new ArrayList<CodeDTO>();
		CodeDTO cDto = new CodeDTO();
		cDto.setCode("FI");
		cDto.setName("French Immersion");
		specialProgram.add(cDto);
		
		reportService.prepareReportData(graduationDataStatus, accessToken, specialProgram);
	}
	
	@Test
	public void testPrepareReportData_Desig_2() {
		String accessToken = "accessToken";
		GradAlgorithmGraduationStatus gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStatus();
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
		List<StudentCourse> sList= new ArrayList<>();
		sList.add(sc);
		StudentCourses sCourses = new StudentCourses();
		sCourses.setStudentCourseList(sList);
		
		StudentAssessment sA= new StudentAssessment();
		sA.setAssessmentCode("FDFE");
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
		
		List<CodeDTO> specialProgram = new ArrayList<CodeDTO>();
		CodeDTO cDto = new CodeDTO();
		cDto.setCode("FI");
		cDto.setName("French Immersion");
		specialProgram.add(cDto);
		
		reportService.prepareReportData(graduationDataStatus, accessToken, specialProgram);
	}
	
	@Test
	public void testPrepareReportData_schoolNull() {
		String accessToken = "accessToken";
		GradAlgorithmGraduationStatus gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStatus();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");
		
		GradSearchStudent stuObj = new GradSearchStudent();
		stuObj.setPen("123123123");
		stuObj.setLegalFirstName("ABC");
		stuObj.setLegalLastName("FDG");
		stuObj.setSchoolOfRecord("12321321");
		
		StudentCourse sc= new StudentCourse();
		sc.setCourseCode("FDFE");
		List<StudentCourse> sList= new ArrayList<>();
		sList.add(sc);
		StudentCourses sCourses = new StudentCourses();
		sCourses.setStudentCourseList(sList);
		
		StudentAssessment sA= new StudentAssessment();
		sA.setAssessmentCode("FDFE");
		List<StudentAssessment> aList= new ArrayList<>();
		aList.add(sA);
		StudentAssessments sAssessments = new StudentAssessments();
		sAssessments.setStudentAssessmentList(aList);
		
		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setSchool(null);
		graduationDataStatus.setStudentCourses(sCourses);
		graduationDataStatus.setStudentAssessments(sAssessments);		
		graduationDataStatus.setGradStudent(stuObj);
		
		GradProgram gP = new GradProgram();
		gP.setProgramCode("2018-EN");
		gP.setProgramName("2018 Graduation Program");
		
		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getProgramNameEndpoint(),gradAlgorithmGraduationStatus.getProgram()))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(GradProgram.class)).thenReturn(Mono.just(gP));
		
		List<CodeDTO> specialProgram = new ArrayList<CodeDTO>();
		CodeDTO cDto = new CodeDTO();
		cDto.setCode("FI");
		cDto.setName("French Immersion");
		specialProgram.add(cDto);
		
		reportService.prepareReportData(graduationDataStatus, accessToken, specialProgram);
	}
	
	@Test
	public void testSetOtherRequiredData() {
		String accessToken = "accessToken";
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
		data.setStudentName("ABC");
		data.setDemographics(sD);
		data.setSchool(schoolObj);
		data.setGraduationMessages(gM);
		
		
		GraduationStatus gradResponse = new GraduationStatus();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");
		gradResponse.setUpdatedTimestamp(new Date(System.currentTimeMillis()));
		
		GradAlgorithmGraduationStatus gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStatus();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");
		
		GradSearchStudent stuObj = new GradSearchStudent();
		stuObj.setPen("123123123");
		stuObj.setLegalFirstName("ABC");
		stuObj.setLegalLastName("FDG");
		stuObj.setLegalMiddleNames("DER");
		stuObj.setSchoolOfRecord("12321321");
		
		StudentCourse sc= new StudentCourse();
		sc.setCourseCode("FDFE");
		List<StudentCourse> sList= new ArrayList<>();
		sList.add(sc);
		StudentCourses sCourses = new StudentCourses();
		sCourses.setStudentCourseList(sList);
		
		StudentAssessment sA= new StudentAssessment();
		sA.setAssessmentCode("FDFE");
		List<StudentAssessment> aList= new ArrayList<>();
		aList.add(sA);
		StudentAssessments sAssessments = new StudentAssessments();
		sAssessments.setStudentAssessmentList(aList);
		
		SpecialGradAlgorithmGraduationStatus algoSpGStatus = new SpecialGradAlgorithmGraduationStatus();
		algoSpGStatus.setPen("123090109");
		algoSpGStatus.setSpecialProgramID(new UUID(1, 1));
		algoSpGStatus.setSpecialGraduated(false);
		List<SpecialGradAlgorithmGraduationStatus> listAl = new ArrayList<SpecialGradAlgorithmGraduationStatus>();
		listAl.add(algoSpGStatus);
		
		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setSchool(null);
		graduationDataStatus.setStudentCourses(sCourses);
		graduationDataStatus.setStudentAssessments(sAssessments);		
		graduationDataStatus.setGradStudent(stuObj);
		graduationDataStatus.setSpecialGradStatus(listAl);
		
		List<String> certificateList = new ArrayList<String>();
		certificateList.add("E");
		
		GradCertificateTypes cType = new GradCertificateTypes();
		cType.setCode("E");
		cType.setDescription("English Dogwood");
		
		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getCertificateTypeEndpoint(),"E"))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(GradCertificateTypes.class)).thenReturn(Mono.just(cType));
		
		reportService.setOtherRequiredData(data, gradResponse, graduationDataStatus, certificateList, accessToken);
	}
}
