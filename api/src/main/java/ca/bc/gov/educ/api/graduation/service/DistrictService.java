package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.model.dto.institute.District;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import ca.bc.gov.educ.api.graduation.util.JsonTransformer;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DistrictService {
  EducGraduationApiConstants educGraduationApiConstants;
  RESTService restService;

  JsonTransformer jsonTransformer;

  public DistrictService(EducGraduationApiConstants educGraduationApiConstants, RESTService restService, JsonTransformer jsonTransformer) {
    this.educGraduationApiConstants = educGraduationApiConstants;
    this.restService = restService;
    this.jsonTransformer = jsonTransformer;
  }

  public District getDistrictDetails(UUID districtId) {
    return this.restService.get(String.format(educGraduationApiConstants.getDistrictDetails(), districtId), District.class);
  }
}
