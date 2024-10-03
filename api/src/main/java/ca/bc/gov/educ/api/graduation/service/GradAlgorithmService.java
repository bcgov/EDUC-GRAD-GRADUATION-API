package ca.bc.gov.educ.api.graduation.service;

import java.util.UUID;

import ca.bc.gov.educ.api.graduation.util.ThreadLocalStateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.graduation.model.dto.ExceptionMessage;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationData;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;

@Service
public class GradAlgorithmService {

	@Autowired
    RESTService restService;
	
	@Autowired
    EducGraduationApiConstants educGraduationApiConstants;
	
	public GraduationData runGradAlgorithm(UUID studentID, String program, ExceptionMessage exception) {
		try {
			return restService.get(String.format(educGraduationApiConstants.getGradAlgorithmEndpoint(),studentID,program), GraduationData.class);
		}catch(Exception e) {
			exception.setExceptionName("GRAD-ALGORITHM-API IS DOWN");
			exception.setExceptionDetails(e.getLocalizedMessage());
			return null;
		}
	}
	
	public GraduationData runProjectedAlgorithm(UUID studentID, String program) {
		return restService.get(String.format(educGraduationApiConstants.getGradProjectedAlgorithmEndpoint(), studentID,program, true), GraduationData.class);
	}

	public GraduationData runHypotheticalGraduatedAlgorithm(UUID studentID, String program, String hypotheticalGradYear) {
		return restService.get(String.format(educGraduationApiConstants.getGradHypotheticalAlgorithmEndpoint(), studentID, program, hypotheticalGradYear), GraduationData.class);
	}
}
