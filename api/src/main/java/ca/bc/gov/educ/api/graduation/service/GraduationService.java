package ca.bc.gov.educ.api.graduation.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import ca.bc.gov.educ.api.graduation.model.dto.GradStudent;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationStatus;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;

@Service
public class GraduationService {

	private static Logger logger = LoggerFactory.getLogger(GraduationService.class);

    @Autowired
    RestTemplate restTemplate;
    
    @Value(EducGraduationApiConstants.ENDPOINT_GRADUATION_ALGORITHM_URL)
    private String graduateStudent;
    
    @Value(EducGraduationApiConstants.ENDPOINT_GRAD_STATUS_UPDATE_URL)
    private String updateGradStatusForStudent;    

    
	public GraduationStatus graduateStudentByPen(String pen) {
		logger.info("graduateStudentByPen");
		GradStudent gradStudent = restTemplate.getForObject(String.format(graduateStudent,pen), GradStudent.class);
		logger.info(gradStudent.getPen());
		GraduationStatus forTestingObj = new GraduationStatus();
		forTestingObj.setPen("124411075");
		forTestingObj.setGraduationDate("2020-06-01");		
		ResponseEntity<GraduationStatus> response = restTemplate.postForEntity(String.format(updateGradStatusForStudent,pen), forTestingObj, GraduationStatus.class);
		logger.info(response.getBody().getPen());
		return response.getBody();
	    
	}

    
}
