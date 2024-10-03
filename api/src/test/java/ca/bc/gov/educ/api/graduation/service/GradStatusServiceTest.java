package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import ca.bc.gov.educ.api.graduation.util.GradValidation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;


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
	public void testGetGradStatus_whenAPIisDown_throwsException() {
		String studentID = new UUID(1, 1).toString();
		exception = new ExceptionMessage();

		when(this.restService.get(String.format(constants.getReadGradStudentRecord(),studentID), GraduationStudentRecord.class)).thenThrow(new RuntimeException("Test - API is down"));

		GraduationStudentRecord res = gradStatusService.getGradStatus(studentID,exception);
		assertNull(res);
	}
	
	@Test
	public void testGetGradStatus() {
		String studentID = new UUID(1, 1).toString();
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");

		when(this.restService.get(String.format(constants.getReadGradStudentRecord(), studentID), GraduationStudentRecord.class)).thenReturn(gradResponse);
		
		GraduationStudentRecord res = gradStatusService.getGradStatus(studentID, exception);
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
	public void testPrepareGraduationStatusData() {
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");

		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setStudentCourses(null);

		gradStatusService.prepareGraduationStatusData(gradResponse, graduationDataStatus);
		assertNotNull(gradResponse.getStudentGradData());

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

		when(this.restService.post(String.format(constants.getUpdateGradStatus(),studentID), gradResponse, GraduationStudentRecord.class)).thenThrow(new RuntimeException("Test - API is down"));
		
		GraduationStudentRecord res = gradStatusService.saveStudentGradStatus(studentID, null,gradResponse,exception);
		assertNotNull(res);
	}

	@Test
	public void testSaveStudentGradStatus() {
		String studentID = new UUID(1, 1).toString();
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");

		when(this.restService.post(String.format(constants.getUpdateGradStatus(), studentID, null), gradResponse, GraduationStudentRecord.class)).thenReturn(gradResponse);

		GraduationStudentRecord res = gradStatusService.saveStudentGradStatus(studentID,null,gradResponse,exception);
		assertNotNull(res);
		assertEquals(res.getPen(), gradResponse.getPen());
	}

	@Test
	public void testSaveStudentGradStatuswithBatch() {
		String studentID = new UUID(1, 1).toString();
		Long batchId= 4546L;
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");

		String url = constants.getUpdateGradStatus() + "?batchId=%s";

		when(this.restService.post(String.format(url, studentID,batchId), gradResponse, GraduationStudentRecord.class)).thenReturn(gradResponse);

		GraduationStudentRecord res = gradStatusService.saveStudentGradStatus(studentID,batchId,gradResponse,exception);
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
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");
		ProjectedRunClob projectedRunClob = ProjectedRunClob.builder().graduated(true).nonGradReasons(new ArrayList<>()).build();

		when(this.restService.post(String.format(constants.getSaveStudentRecordProjectedRun(), studentID), projectedRunClob, GraduationStudentRecord.class)).thenReturn(gradResponse);

		GraduationStudentRecord res = gradStatusService.saveStudentRecordProjectedRun(projectedRunClob,studentID,null, exception);
		assertNotNull(res);
		assertEquals(res.getPen(), gradResponse.getPen());
	}

	@Test
	public void testSaveProjectedGradStatus_withbatch() {
		String studentID = new UUID(1, 1).toString();
		Long batchId = 45343L;
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");
		ProjectedRunClob projectedRunClob = ProjectedRunClob.builder().graduated(true).nonGradReasons(new ArrayList<>()).build();

		String url = constants.getSaveStudentRecordProjectedRun() + "?batchId=%s";;

		when(this.restService.post(String.format(url, studentID, batchId), projectedRunClob, GraduationStudentRecord.class)).thenReturn(gradResponse);

		GraduationStudentRecord res = gradStatusService.saveStudentRecordProjectedRun(projectedRunClob,studentID,batchId,exception);
		assertNotNull(res);
		assertEquals(res.getPen(), gradResponse.getPen());
	}

	@Test
	public void testSaveProjectedGradStatus_witherror() {
		String studentID = new UUID(1, 1).toString();
		GraduationStudentRecord gradResponse = new GraduationStudentRecord();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("D");
		ExceptionMessage exception = new ExceptionMessage();
		ProjectedRunClob projectedRunClob = ProjectedRunClob.builder().graduated(true).nonGradReasons(new ArrayList<>()).build();

		when(this.restService.post(String.format(constants.getSaveStudentRecordProjectedRun(), studentID), projectedRunClob, GraduationStudentRecord.class)).thenThrow(new RuntimeException("Unexpected Error!"));

		GraduationStudentRecord res = gradStatusService.saveStudentRecordProjectedRun(projectedRunClob,studentID,null,exception);
		assertNull(res);
	}

	@Test
	public void testRestoreStudentGradStatus() {
		String studentID = new UUID(1, 1).toString();
		boolean isGraduated = false;

		when(this.restService.get(String.format(constants.getUpdateGradStatusAlgoError(),studentID,isGraduated), Boolean.class)).thenReturn(false);

		gradStatusService.restoreStudentGradStatus(studentID, isGraduated);
		assertThat(isGraduated).isFalse();
	}

	@Test
	public void testGetStudentsbyMincode() {
		String mincode = "12312311";

		GraduationStudentRecord gsr = new GraduationStudentRecord();
		gsr.setLegalLastName("qweqw");
		when(this.restService.get(any(String.class), any())).thenReturn(List.of(gsr));

		List<GraduationStudentRecord> res = gradStatusService.getStudentListByMinCode(mincode);
		assertThat(res).isNotEmpty().hasSize(1);
	}
		
}
