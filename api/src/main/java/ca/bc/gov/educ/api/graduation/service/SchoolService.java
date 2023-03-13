package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.model.dto.ExceptionMessage;
import ca.bc.gov.educ.api.graduation.model.dto.SchoolTrax;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SchoolService {
	EducGraduationApiConstants educGraduationApiConstants;
	RESTService restService;

	@Autowired
	public SchoolService(EducGraduationApiConstants educGraduationApiConstants, RESTService restService) {
		this.educGraduationApiConstants = educGraduationApiConstants;
		this.restService = restService;
	}

	public SchoolTrax getSchoolDetails(String mincode, String accessToken, ExceptionMessage message) {
		return this.restService.get(String.format(educGraduationApiConstants.getSchoolDetails(),mincode, accessToken),
				SchoolTrax.class,
				accessToken);
	}
}
