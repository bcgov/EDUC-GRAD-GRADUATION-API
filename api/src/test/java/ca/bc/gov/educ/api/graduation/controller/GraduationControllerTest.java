package ca.bc.gov.educ.api.graduation.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

import ca.bc.gov.educ.api.graduation.model.dto.AlgorithmResponse;
import ca.bc.gov.educ.api.graduation.model.dto.GradProgram;
import ca.bc.gov.educ.api.graduation.model.dto.GradStudentSpecialProgram;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationStatus;
import ca.bc.gov.educ.api.graduation.service.GraduationService;
import ca.bc.gov.educ.api.graduation.util.GradValidation;
import ca.bc.gov.educ.api.graduation.util.MessageHelper;
import ca.bc.gov.educ.api.graduation.util.ResponseHelper;


@ExtendWith(MockitoExtension.class)
public class GraduationControllerTest {

	@Mock
	private GraduationService graduationService;
	
	@Mock
	ResponseHelper response;
	
	@InjectMocks
	private GraduationController graduationController;
	
	@Mock
	GradValidation validation;
	
	@Mock
	MessageHelper messagesHelper;
	
	@Mock
	OAuth2AuthenticationDetails oAuth2AuthenticationDetails;
	
	@Mock
	SecurityContextHolder securityContextHolder;
	
	@Test
	public void testGraduateStudentNew() {
		String studentID = new UUID(1, 1).toString();
		String projectedType = "REGFM";
		
		GraduationStatus gradResponse = new GraduationStatus();
		gradResponse.setPen("123090109");
		gradResponse.setProgram("2018-EN");
		gradResponse.setProgramCompletionDate(null);
		gradResponse.setSchoolOfRecord("06011033");
		gradResponse.setStudentGrade("11");
		gradResponse.setStudentStatus("A");
		
		GradStudentSpecialProgram spgm = new GradStudentSpecialProgram();
		spgm.setPen("123090109");
		spgm.setSpecialProgramCode("BD");
		spgm.setSpecialProgramName("International Bacculaurette");
		spgm.setStudentID(UUID.fromString(studentID));
		List<GradStudentSpecialProgram> list = new ArrayList<GradStudentSpecialProgram>();
		list.add(spgm);
		
		Authentication authentication = Mockito.mock(Authentication.class);
		OAuth2AuthenticationDetails details = Mockito.mock(OAuth2AuthenticationDetails.class);
		// Mockito.whens() for your authorization object
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		Mockito.when(authentication.getDetails()).thenReturn(details);
		SecurityContextHolder.setContext(securityContext);
		
		AlgorithmResponse alRes = new AlgorithmResponse();
		alRes.setGraduationStatus(gradResponse);
		alRes.setSpecialGraduationStatus(list);
		Mockito.when(graduationService.graduateStudent(studentID,null,projectedType)).thenReturn(alRes);
		graduationController.graduateStudentNew(studentID,projectedType);
		Mockito.verify(graduationService).graduateStudent(studentID,null,projectedType);
	}
	
	
}
