package ca.bc.gov.educ.api.graduation.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import ca.bc.gov.educ.api.graduation.model.dto.GraduationStatus;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiUtils;
import ca.bc.gov.educ.api.graduation.util.GradBusinessRuleException;

@Service
public class GraduationService {

	private static Logger logger = LoggerFactory.getLogger(GraduationService.class);

    @Autowired
    RestTemplate restTemplate;
    
    @Value(EducGraduationApiConstants.ENDPOINT_GRADUATION_ALGORITHM_URL)
    private String graduateStudent;
    
    @Value(EducGraduationApiConstants.ENDPOINT_GRAD_STATUS_UPDATE_URL)
    private String updateGradStatusForStudent;    

    
	public GraduationStatus graduateStudentByPen(String pen, String accessToken) {
		logger.info("graduateStudentByPen");
		HttpHeaders httpHeaders = EducGraduationApiUtils.getHeaders(accessToken);
		try {
		GraduationStatus graduationStatus = restTemplate.exchange(String.format(graduateStudent,pen), HttpMethod.GET,
				new HttpEntity<>(httpHeaders), GraduationStatus.class).getBody();
		GraduationStatus graduationStatusResponse = restTemplate.exchange(String.format(updateGradStatusForStudent,pen), HttpMethod.POST,
				new HttpEntity<>(graduationStatus,httpHeaders), GraduationStatus.class).getBody();
		//create reportparameter and call report api.		
		//Save generated reports in Student Report Table		
		return graduationStatusResponse;	
		}catch(Exception e) {
			new GradBusinessRuleException("Error Graduating Student. Please try again...");
		}
		return null;
	}

    
}
