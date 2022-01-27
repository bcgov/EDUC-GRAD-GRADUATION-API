package ca.bc.gov.educ.api.graduation.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

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
import ca.bc.gov.educ.api.graduation.model.dto.GradAlgorithmGraduationStudentRecord;
import ca.bc.gov.educ.api.graduation.model.dto.GradAlgorithmOptionalStudentProgram;
import ca.bc.gov.educ.api.graduation.model.dto.StudentOptionalProgram;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationData;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationStatus;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import ca.bc.gov.educ.api.graduation.util.GradValidation;
import reactor.core.publisher.Mono;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class OptionalProgramServiceTest {
	
	@Autowired
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
    private Mono<GraduationStatus> monoResponse;
    
    @Mock
    private Mono<StudentOptionalProgram> monoResponseGradStudentOptionalProgram;
	
    @Before
    public void setUp() {
        openMocks(this);
    }    

	@After
    public void tearDown() {

    }
	
	@Test
	public void testSaveAndLogOptionalPrograms() {
		String studentID = new UUID(1, 1).toString();
		String accessToken = "accessToken";
		
		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");
		
		GradAlgorithmOptionalStudentProgram algoSpGStatus = new GradAlgorithmOptionalStudentProgram();
		algoSpGStatus.setPen("123090109");
		algoSpGStatus.setOptionalProgramID(new UUID(1, 1));
		algoSpGStatus.setOptionalGraduated(false);
		List<GradAlgorithmOptionalStudentProgram> listAl = new ArrayList<GradAlgorithmOptionalStudentProgram>();
		listAl.add(algoSpGStatus);
		
		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setStudentCourses(null);
		graduationDataStatus.setOptionalGradStatus(listAl);
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("BD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		
		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getGetOptionalProgramDetails(), studentID,new UUID(1, 1)))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(StudentOptionalProgram.class)).thenReturn(monoResponseGradStudentOptionalProgram);
		when(this.monoResponseGradStudentOptionalProgram.block()).thenReturn(spgm); 
		
		when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.uri(constants.getSaveOptionalProgramGradStatus())).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(StudentOptionalProgram.class)).thenReturn(Mono.just(spgm));
		
		List<StudentOptionalProgram> spList;
		spList = optionalProgramService.saveAndLogOptionalPrograms(graduationDataStatus, studentID, accessToken, new ArrayList<>());
		assertEquals(1,spList.size());
		
       
	}	
	
	@Test
	public void testSaveAndLogOptionalProgramsDualDogwood_nooptionalProgram() {
		String studentID = new UUID(1, 1).toString();
		String accessToken = "accessToken";
		
		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");
		
		GradAlgorithmOptionalStudentProgram algoSpGStatus = new GradAlgorithmOptionalStudentProgram();
		algoSpGStatus.setPen("123090109");
		algoSpGStatus.setOptionalProgramID(new UUID(1, 1));
		algoSpGStatus.setOptionalGraduated(true);
		List<GradAlgorithmOptionalStudentProgram> listAl = new ArrayList<GradAlgorithmOptionalStudentProgram>();
		listAl.add(algoSpGStatus);
		
		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setStudentCourses(null);
		graduationDataStatus.setOptionalGradStatus(listAl);
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("DD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		
		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getGetOptionalProgramDetails(), studentID,new UUID(1, 1)))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(StudentOptionalProgram.class)).thenReturn(monoResponseGradStudentOptionalProgram);
		when(this.monoResponseGradStudentOptionalProgram.block()).thenReturn(spgm); 
		
		when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.uri(constants.getSaveOptionalProgramGradStatus())).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(StudentOptionalProgram.class)).thenReturn(Mono.just(spgm));
		
		List<StudentOptionalProgram> spList;
		spList = optionalProgramService.saveAndLogOptionalPrograms(graduationDataStatus, studentID, accessToken, new ArrayList<>());
		assertEquals(1,spList.size());
		
       
	}
	
	
	@Test
	public void testSaveAndLogOptionalProgramsDualDogwood() {
		String studentID = new UUID(1, 1).toString();
		String accessToken = "accessToken";
		
		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");
		
		GradAlgorithmOptionalStudentProgram algoSpGStatus = new GradAlgorithmOptionalStudentProgram();
		algoSpGStatus.setPen("123090109");
		algoSpGStatus.setOptionalProgramID(new UUID(1, 1));
		algoSpGStatus.setOptionalGraduated(true);
		List<GradAlgorithmOptionalStudentProgram> listAl = new ArrayList<GradAlgorithmOptionalStudentProgram>();
		listAl.add(algoSpGStatus);
		
		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setStudentCourses(null);
		graduationDataStatus.setOptionalGradStatus(listAl);
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("DD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		
		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getGetOptionalProgramDetails(), studentID,new UUID(1, 1)))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(StudentOptionalProgram.class)).thenReturn(monoResponseGradStudentOptionalProgram);
		when(this.monoResponseGradStudentOptionalProgram.block()).thenReturn(null); 
		
		
		
		List<StudentOptionalProgram> spList;
		spList = optionalProgramService.saveAndLogOptionalPrograms(graduationDataStatus, studentID, accessToken, new ArrayList<>());
		assertEquals(1,spList.size());
		
       
	}
	
	@Test
	public void testProjectedOptionalPrograms() {
		String studentID = new UUID(1, 1).toString();
		String accessToken = "accessToken";
		
		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");
		
		GradAlgorithmOptionalStudentProgram algoSpGStatus = new GradAlgorithmOptionalStudentProgram();
		algoSpGStatus.setPen("123090109");
		algoSpGStatus.setOptionalProgramID(new UUID(1, 1));
		algoSpGStatus.setOptionalGraduated(true);
		List<GradAlgorithmOptionalStudentProgram> listAl = new ArrayList<GradAlgorithmOptionalStudentProgram>();
		listAl.add(algoSpGStatus);
		
		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setStudentCourses(null);
		graduationDataStatus.setOptionalGradStatus(listAl);
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("DD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setProgramCode("2018-EN");
		spgm.setStudentID(UUID.fromString(studentID));
		
		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getGetOptionalProgramDetails(), studentID,new UUID(1, 1)))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(StudentOptionalProgram.class)).thenReturn(monoResponseGradStudentOptionalProgram);
		when(this.monoResponseGradStudentOptionalProgram.block()).thenReturn(spgm); 
		
		
		
		List<StudentOptionalProgram> spList;
		spList = optionalProgramService.projectedOptionalPrograms(graduationDataStatus, studentID, accessToken);
		assertEquals(1,spList.size());

		
       
	}
	
	@Test
	public void testProjectedOptionalPrograms_nooptionalProgram() {
		String studentID = new UUID(1, 1).toString();
		String accessToken = "accessToken";
		
		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");
		
		GradAlgorithmOptionalStudentProgram algoSpGStatus = new GradAlgorithmOptionalStudentProgram();
		algoSpGStatus.setPen("123090109");
		algoSpGStatus.setOptionalProgramID(new UUID(1, 1));
		algoSpGStatus.setOptionalGraduated(true);
		List<GradAlgorithmOptionalStudentProgram> listAl = new ArrayList<GradAlgorithmOptionalStudentProgram>();
		listAl.add(algoSpGStatus);
		
		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setStudentCourses(null);
		graduationDataStatus.setOptionalGradStatus(listAl);
		
		StudentOptionalProgram spgm = new StudentOptionalProgram();
		spgm.setPen("123090109");
		spgm.setOptionalProgramCode("DD");
		spgm.setOptionalProgramName("International Bacculaurette");
		spgm.setProgramCode("2018-EN");
		spgm.setStudentID(UUID.fromString(studentID));
		
		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getGetOptionalProgramDetails(), studentID,new UUID(1, 1)))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(StudentOptionalProgram.class)).thenReturn(monoResponseGradStudentOptionalProgram);
		when(this.monoResponseGradStudentOptionalProgram.block()).thenReturn(null); 
		
		
		
		List<StudentOptionalProgram> spList;
		spList = optionalProgramService.projectedOptionalPrograms(graduationDataStatus, studentID, accessToken);
		assertEquals(1,spList.size());

		
       
	}
	
}
