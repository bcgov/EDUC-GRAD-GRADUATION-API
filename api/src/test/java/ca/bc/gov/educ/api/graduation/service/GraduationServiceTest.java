package ca.bc.gov.educ.api.graduation.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

import com.fasterxml.jackson.core.JsonProcessingException;

import ca.bc.gov.educ.api.graduation.model.dto.AlgorithmResponse;
import ca.bc.gov.educ.api.graduation.model.dto.GradAlgorithmGraduationStatus;
import ca.bc.gov.educ.api.graduation.model.dto.GradStudentSpecialProgram;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationData;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationStatus;
import ca.bc.gov.educ.api.graduation.util.GradBusinessRuleException;
import ca.bc.gov.educ.api.graduation.util.GradValidation;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class GraduationServiceTest {

	@Autowired
	private GraduationService graduationService;
	
	@MockBean
	private GradStatusService gradStatusService;
	
	@MockBean
	private GradAlgorithmService gradAlgorithmService;
	
	@MockBean
	private SpecialProgramService specialProgramService;
	
	@Autowired
	GradValidation validation;
	
	@Mock
	WebClient webClient;
	
	@Test
	public void testGraduateStudent() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="REGFM";
		String accessToken="accessToken";
		
		GraduationStatus gradResponse = new GraduationStatus();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("A");
		
		GradAlgorithmGraduationStatus gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStatus();
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
		
		GradStudentSpecialProgram spgm = new GradStudentSpecialProgram();
		spgm.setPen("123090109");
		spgm.setSpecialProgramCode("BD");
		spgm.setSpecialProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<GradStudentSpecialProgram> list = new ArrayList<GradStudentSpecialProgram>();
		list.add(spgm);
		
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getPen(), gradResponse.getProgram(), accessToken)).thenReturn(graduationDataStatus);
		try {
			Mockito.when(specialProgramService.projectedSpecialPrograms(graduationDataStatus, studentID, accessToken)).thenReturn(list);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		AlgorithmResponse response = graduationService.graduateStudent(studentID,accessToken,projectedType);
		assertNotNull(response);
	}
	
	@Test
	public void testGraduateStudent_error() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="REGFM";
		String accessToken="accessToken";
		validation.clear();
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
		
		GraduationData graduationDataStatus = new GraduationData();
		graduationDataStatus.setDualDogwood(false);
		graduationDataStatus.setGradMessage("Not Graduated");
		graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
		graduationDataStatus.setGraduated(false);
		graduationDataStatus.setStudentCourses(null);
		
		GradStudentSpecialProgram spgm = new GradStudentSpecialProgram();
		spgm.setPen("123090109");
		spgm.setSpecialProgramCode("BD");
		spgm.setSpecialProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<GradStudentSpecialProgram> list = new ArrayList<GradStudentSpecialProgram>();
		list.add(spgm);
		
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken)).thenReturn(gradResponse);
		Mockito.when(gradAlgorithmService.runGradAlgorithm(gradResponse.getPen(), gradResponse.getProgram(), accessToken)).thenReturn(graduationDataStatus);
		try {
			Mockito.when(specialProgramService.projectedSpecialPrograms(graduationDataStatus, studentID, accessToken)).thenReturn(list);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			AlgorithmResponse response = graduationService.graduateStudent(studentID,accessToken,projectedType);
		} catch (GradBusinessRuleException e) {
			List<String> errors = validation.getErrors();
			assertEquals(1, errors.size());
			return;
		}
	}
	
	@Test
	public void testGraduateStudent_error2() {
		String studentID = new UUID(1, 1).toString();
		String projectedType="REGFM";
		String accessToken="accessToken";
		validation.clear();
		GraduationStatus gradResponse = new GraduationStatus();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("M");
		
		Mockito.when(gradStatusService.getGradStatus(studentID, accessToken)).thenReturn(gradResponse);
		try {
			AlgorithmResponse response = graduationService.graduateStudent(studentID,accessToken,projectedType);
		} catch (GradBusinessRuleException e) {
			List<String> errors = validation.getErrors();
			assertEquals(1, errors.size());
			return;
		}
	}
		
}
