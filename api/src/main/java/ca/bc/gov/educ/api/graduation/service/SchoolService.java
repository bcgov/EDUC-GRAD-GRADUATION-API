package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import ca.bc.gov.educ.api.graduation.util.JsonTransformer;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SchoolService {
	EducGraduationApiConstants educGraduationApiConstants;
	RESTService restService;

	JsonTransformer jsonTransformer;
	@Autowired
	public SchoolService(EducGraduationApiConstants educGraduationApiConstants, RESTService restService, JsonTransformer jsonTransformer) {
		this.educGraduationApiConstants = educGraduationApiConstants;
		this.restService = restService;
		this.jsonTransformer = jsonTransformer;
	}

	public ca.bc.gov.educ.api.graduation.model.dto.institute.School getSchoolDetails(UUID schoolId) {
		if (schoolId == null) return null;
		return this.restService.get(String.format(educGraduationApiConstants.getSchoolDetails(),schoolId), ca.bc.gov.educ.api.graduation.model.dto.institute.School.class);
	}

	public School getSchoolClob(String schoolId) {
		if (StringUtils.isBlank(schoolId)) return null;
		return getSchoolClob(UUID.fromString(schoolId));
	}

	public School getSchoolClob(UUID schoolId) {
		if (schoolId == null) return null;
		return this.restService.get(String.format(educGraduationApiConstants.getSchoolClobBySchoolIdUrl(),schoolId),
				School.class);
	}

	public District getDistrictDetails(String districtCode) {
		return this.restService.get(String.format(educGraduationApiConstants.getDistrictDetails(), districtCode),
				District.class);
	}

}

