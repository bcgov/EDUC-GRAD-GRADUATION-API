package ca.bc.gov.educ.api.graduation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import ca.bc.gov.educ.api.graduation.model.dto.ExceptionMessage;
import ca.bc.gov.educ.api.graduation.model.dto.GradAlgorithmGraduationStudentRecord;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationData;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import ca.bc.gov.educ.api.graduation.util.GradValidation;
import org.springframework.web.reactive.function.client.WebClient;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class GradAlgorithmServiceTest {

	@Autowired
	private GradAlgorithmService gradAlgorithmService;
	
	@Autowired
	GradValidation validation;
	
	@Autowired
	private ExceptionMessage exception;

	@MockBean
	RESTService restService;

	@MockBean
	@Qualifier("graduationApiClient")
	WebClient graduationApiClient;

	@MockBean
	@Qualifier("gradEducStudentApiClient")
	WebClient gradEducStudentApiClient;

	@TestConfiguration
	static class TestConfig {
		@Bean
		public ClientRegistrationRepository clientRegistrationRepository() {
			return new ClientRegistrationRepository() {
				@Override
				public ClientRegistration findByRegistrationId(String registrationId) {
					return null;
				}
			};
		}
	}

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
	public void testRunGradAlgorithm_whenAPIisDown_throwsException() {
		UUID studentID = new UUID(1, 1);
		String programCode="2018-EN";

		when(this.restService.get(String.format(constants.getGradAlgorithmEndpoint(),studentID,programCode),
				GraduationData.class, graduationApiClient)).thenThrow(new RuntimeException("Test - API is down"));
		GraduationData res = gradAlgorithmService.runGradAlgorithm(studentID, programCode,exception);
		assertNull(res);

		assertNotNull(exception);
		assertThat(exception.getExceptionName()).isEqualTo("GRAD-ALGORITHM-API IS DOWN");
	}
	
	@Test
	public void testRunGradAlgorithm() {
		UUID studentID = new UUID(1, 1);
		String programCode="2018-EN";

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
		
		when(this.restService.get(String.format(constants.getGradAlgorithmEndpoint(),studentID,programCode),
				GraduationData.class, graduationApiClient)).thenReturn(graduationDataStatus);
        
		GraduationData res = gradAlgorithmService.runGradAlgorithm(studentID, programCode,exception);
		assertNotNull(res);
	}
	
	@Test
	public void testRunProjectedGradAlgorithm() {
		UUID studentID = new UUID(1, 1);
		String programCode="2018-EN";

		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");
		gradAlgorithmGraduationStatus.setStudentID(studentID);
		
		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setStudentCourses(null);
		
		when(this.restService.get(String.format(constants.getGradProjectedAlgorithmEndpoint(),studentID,programCode, true),
				GraduationData.class, graduationApiClient)).thenReturn(graduationDataStatus);

		GraduationData res = gradAlgorithmService.runProjectedAlgorithm(studentID, programCode);
		assertNotNull(res);       
	}

	@Test
	public void testRunHypotheticalGradAlgorithm() {
		UUID studentID = new UUID(1, 1);
		String programCode="2018-EN";

		GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStudentRecord();
		gradAlgorithmGraduationStatus.setPen("123090109");
		gradAlgorithmGraduationStatus.setProgram("2018-EN");
		gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
		gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
		gradAlgorithmGraduationStatus.setStudentGrade("11");
		gradAlgorithmGraduationStatus.setStudentStatus("A");
		gradAlgorithmGraduationStatus.setStudentID(studentID);

		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setStudentCourses(null);

		when(this.restService.get(String.format(constants.getGradHypotheticalAlgorithmEndpoint(),studentID,programCode, "2023"),
				GraduationData.class, graduationApiClient)).thenReturn(graduationDataStatus);

		GraduationData res = gradAlgorithmService.runHypotheticalGraduatedAlgorithm(studentID, programCode, "2023");
		assertNotNull(res);
	}
}
