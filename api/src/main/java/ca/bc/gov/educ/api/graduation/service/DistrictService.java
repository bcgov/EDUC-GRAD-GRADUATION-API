package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.model.dto.institute.District;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import ca.bc.gov.educ.api.graduation.util.JsonTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Service
public class DistrictService {
  EducGraduationApiConstants educGraduationApiConstants;
  RESTService restService;
  JsonTransformer jsonTransformer;
  WebClient graduationApiClient;

  @Autowired
  public DistrictService(EducGraduationApiConstants educGraduationApiConstants, RESTService restService,
                         JsonTransformer jsonTransformer, @Qualifier("graduationApiClient") WebClient graduationApiClient) {
    this.educGraduationApiConstants = educGraduationApiConstants;
    this.restService = restService;
    this.jsonTransformer = jsonTransformer;
    this.graduationApiClient = graduationApiClient;
  }

  public District getDistrictDetails(UUID districtId) {
    return this.restService.get(String.format(educGraduationApiConstants.getDistrictDetails(), districtId),
            District.class, graduationApiClient);
  }
}
