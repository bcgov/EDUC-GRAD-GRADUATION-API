package ca.bc.gov.educ.api.graduation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

import ca.bc.gov.educ.api.graduation.model.dto.*;
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

import com.fasterxml.jackson.core.JsonProcessingException;

import ca.bc.gov.educ.api.graduation.model.dto.GraduationStudentRecord;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import ca.bc.gov.educ.api.graduation.util.GradValidation;
import reactor.core.publisher.Mono;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class GradStatusServiceTest {
	
	@Autowired
	private GradStatusService gradStatusService;
	
	@Autowired
	private ExceptionMessage exception;
	
	@MockBean
	private GradAlgorithmService gradAlgorithmService;
	
	@MockBean
	private OptionalProgramService optionalProgramService;
	
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
    private Mono<GraduationStudentRecord> monoResponse;
	
    @Before
    public void setUp() {
        openMocks(this);
    }    

	@After
    public void tearDown() {

    }

	@Test
	public void testGetGradStatus_whenAPIisDown_throwsException() {
		String studentID = new UUID(1, 1).toString();
		String accessToken = "accessToken";
		exception = new ExceptionMessage();
		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getReadGradStudentRecord(),studentID))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(Exception.class)).thenReturn(Mono.just(new Exception()));

		GraduationStudentRecord res = gradStatusService.getGradStatus(studentID, accessToken,exception);
		assertNull(res);
	}
	
	@Test
	public void testGetGradStatus() {
		String studentID = new UUID(1, 1).toString();
		String accessToken = "accessToken";
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");
		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getReadGradStudentRecord(), studentID))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(GraduationStudentRecord.class)).thenReturn(monoResponse);
		when(this.monoResponse.block()).thenReturn(gradResponse); 
		
		GraduationStudentRecord res = gradStatusService.getGradStatus(studentID, accessToken,exception);
		assertNotNull(res);
		assertEquals(res.getPen(), gradResponse.getPen());
       
	}

	@Test
	public void testPrepareGraduationStatusObj() {
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

		GraduationStudentRecord obj = gradStatusService.prepareGraduationStatusObj(graduationDataStatus);
		assertNotNull(obj.getStudentGradData());
	}

	@Test
	public void testSaveStudentGradStatus_whenAPIisDown_throwsException() {
		String studentID = new UUID(1, 1).toString();
		String accessToken = "accessToken";
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");
		
		when(this.webClient.post()).thenThrow(new RuntimeException("Test - API is down"));
		
		GraduationStudentRecord res = gradStatusService.saveStudentGradStatus(studentID, null,accessToken,gradResponse,exception);
		assertNull(res);
	}

	@Test
	public void testSaveStudentGradStatus() {
		String studentID = new UUID(1, 1).toString();
		String accessToken = "accessToken";
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");

		when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.uri(String.format(constants.getUpdateGradStatus(), studentID))).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(GraduationStudentRecord.class)).thenReturn(Mono.just(gradResponse));

		GraduationStudentRecord res = gradStatusService.saveStudentGradStatus(studentID,null, accessToken,gradResponse,exception);
		assertNotNull(res);
		assertEquals(res.getPen(), gradResponse.getPen());
	}
	
	@Test
	public void testProcessProjectedResults() {
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
		
		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setStudentCourses(null);
		
		GraduationStudentRecord res;
		res = gradStatusService.processProjectedResults(gradResponse, graduationDataStatus);
		assertNotNull(res);
		assertEquals(res.getPen(), gradResponse.getPen());

		
		
	}

	@Test
	public void testSaveProjectedGradStatus() {
		String studentID = new UUID(1, 1).toString();
		String accessToken = "accessToken";
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");
		ProjectedRunClob projectedRunClob = ProjectedRunClob.builder().graduated(true).gradMessage("asdsada").nonGradReasons(new ArrayList<>()).requirementsMet(new ArrayList<>()).dualDogwood(false).build();

		when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.uri(String.format(constants.getSaveStudentRecordProjectedRun(), studentID))).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(GraduationStudentRecord.class)).thenReturn(Mono.just(gradResponse));

		GraduationStudentRecord res = gradStatusService.saveStudentRecordProjectedRun(projectedRunClob,studentID,null, accessToken,exception);
		assertNotNull(res);
		assertEquals(res.getPen(), gradResponse.getPen());
	}
		
}
