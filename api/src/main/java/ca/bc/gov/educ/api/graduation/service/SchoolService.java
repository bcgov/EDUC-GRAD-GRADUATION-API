package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.model.dto.DistrictTrax;
import ca.bc.gov.educ.api.graduation.model.dto.ExceptionMessage;
import ca.bc.gov.educ.api.graduation.model.dto.SchoolTrax;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import ca.bc.gov.educ.api.graduation.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SchoolService {
	EducGraduationApiConstants educGraduationApiConstants;
	RESTService restService;
	TokenUtils tokenUtils;

	@Autowired
	public SchoolService(EducGraduationApiConstants educGraduationApiConstants, RESTService restService, TokenUtils tokenUtils) {
		this.educGraduationApiConstants = educGraduationApiConstants;
		this.restService = restService;
		this.tokenUtils = tokenUtils;
	}

	public SchoolTrax getTraxSchoolDetails(String mincode, ExceptionMessage message) {
		return this.restService.get(String.format(educGraduationApiConstants.getSchoolDetails(),mincode),
				SchoolTrax.class);
	}

	public SchoolTrax getTraxSchoolDetails(String mincode) {
		return this.restService.get(String.format(educGraduationApiConstants.getSchoolDetails(),mincode),
				SchoolTrax.class);
	}

	public DistrictTrax getTraxDistrictDetails(String districtCode) {
		return this.restService.get(String.format(educGraduationApiConstants.getDistrictDetails(), districtCode),
				DistrictTrax.class);
	}

}
