package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.model.dto.*;
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

	public School getSchoolDetails(String mincode) {
		return this.restService.get(String.format(educGraduationApiConstants.getSchoolDetails(),mincode),
				School.class);
	}

	public District getDistrictDetails(String districtCode) {
		return this.restService.get(String.format(educGraduationApiConstants.getDistrictDetails(), districtCode),
				District.class);
	}

}

