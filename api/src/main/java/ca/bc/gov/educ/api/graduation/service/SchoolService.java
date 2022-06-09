package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.model.dto.ExceptionMessage;
import ca.bc.gov.educ.api.graduation.model.dto.SchoolTrax;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class SchoolService {

	@Autowired
    WebClient webClient;
	
	@Autowired
	EducGraduationApiConstants educGraduationApiConstants;
	
	public SchoolTrax getSchoolDetails(String mincode, String accessToken, ExceptionMessage exception) {
		try
		{
			return webClient.get().uri(String.format(educGraduationApiConstants.getSchoolDetails(),mincode)).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(SchoolTrax.class).block();
		} catch (Exception e) {
			exception.setExceptionName("GRAD-TRAX-API IS DOWN");
			exception.setExceptionDetails(e.getLocalizedMessage());
			return null;
		}
	}
}
