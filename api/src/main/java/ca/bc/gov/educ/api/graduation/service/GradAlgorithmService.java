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
    WebClient webClient;
	
	@Autowired
    EducGraduationApiConstants educGraduationApiConstants;
	
	public GraduationData runGradAlgorithm(UUID studentID, String program,String accessToken,ExceptionMessage exception) {
		try {
			return webClient.get().uri(String.format(educGraduationApiConstants.getGradAlgorithmEndpoint(),studentID,program))
							.headers(h -> {
								h.setBearerAuth(accessToken);
								h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
							}).retrieve().bodyToMono(GraduationData.class).block();
		}catch(Exception e) {
			exception.setExceptionName("GRAD-ALGORITHM-API IS DOWN");
			exception.setExceptionDetails(e.getLocalizedMessage());
			return null;
		}
	}
	
	public GraduationData runProjectedAlgorithm(UUID studentID, String program,String accessToken) {
		return webClient.get().uri(String.format(educGraduationApiConstants.getGradProjectedAlgorithmEndpoint(), studentID,program, true))
						.headers(h -> {
							h.setBearerAuth(accessToken);
							h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
						}).retrieve().bodyToMono(GraduationData.class).block();
	}
}
