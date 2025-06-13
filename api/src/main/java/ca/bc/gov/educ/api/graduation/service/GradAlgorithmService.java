package ca.bc.gov.educ.api.graduation.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import ca.bc.gov.educ.api.graduation.model.dto.ExceptionMessage;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationData;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GradAlgorithmService {

	RESTService restService;
	EducGraduationApiConstants educGraduationApiConstants;
	WebClient graduationApiClient;

	@Autowired
	public GradAlgorithmService(RESTService restService, EducGraduationApiConstants educGraduationApiConstants,
								@Qualifier("graduationApiClient")WebClient graduationApiClient) {
		this.restService = restService;
		this.educGraduationApiConstants = educGraduationApiConstants;
		this.graduationApiClient = graduationApiClient;
	}
	
	public GraduationData runGradAlgorithm(UUID studentID, String program, ExceptionMessage exception) {
		try {
			return restService.get(String.format(educGraduationApiConstants.getGradAlgorithmEndpoint(),
					studentID,program), GraduationData.class, graduationApiClient);
		}catch(Exception e) {
			exception.setExceptionName("GRAD-ALGORITHM-API IS DOWN");
			exception.setExceptionDetails(e.getLocalizedMessage());
			return null;
		}
	}
	
	public GraduationData runProjectedAlgorithm(UUID studentID, String program) {
		return restService.get(String.format(educGraduationApiConstants.getGradProjectedAlgorithmEndpoint(), studentID,program, true),
				GraduationData.class, graduationApiClient);
	}

	public GraduationData runHypotheticalGraduatedAlgorithm(UUID studentID, String program, String hypotheticalGradYear) {
		return restService.get(String.format(educGraduationApiConstants.getGradHypotheticalAlgorithmEndpoint(), studentID, program, hypotheticalGradYear),
				GraduationData.class, graduationApiClient);
	}
}
