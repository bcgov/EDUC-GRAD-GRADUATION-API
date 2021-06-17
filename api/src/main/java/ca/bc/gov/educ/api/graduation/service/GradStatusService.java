package ca.bc.gov.educ.api.graduation.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.bc.gov.educ.api.graduation.model.dto.GraduationData;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationStatus;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;

@Service
public class GradStatusService {

	@Autowired
    WebClient webClient;
	
	@Autowired
    EducGraduationApiConstants educGraduationApiConstants;
	
	public GraduationStatus getGradStatus(String studentID, String accessToken) {
		return webClient.get().uri(String.format(educGraduationApiConstants.getReadGradStatus(),studentID)).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(GraduationStatus.class).block();
	}
	
	public GraduationStatus prepareGraduationStatusObj(GraduationData graduationDataStatus) {
		GraduationStatus obj = new GraduationStatus();
		BeanUtils.copyProperties(graduationDataStatus.getGradStatus(), obj);
		try {
			obj.setStudentGradData(new ObjectMapper().writeValueAsString(graduationDataStatus));
		} catch (JsonProcessingException e) {
			e.getMessage();
		}
		return obj;
	}
	
	public GraduationStatus saveStudentGradStatus(String studentID,String accessToken, GraduationStatus toBeSaved) {
		return webClient.post().uri(String.format(educGraduationApiConstants.getUpdateGradStatus(),studentID)).headers(h -> h.setBearerAuth(accessToken)).body(BodyInserters.fromValue(toBeSaved)).retrieve().bodyToMono(GraduationStatus.class).block();
	}

	public GraduationStatus processProjectedResults(GraduationStatus gradResponse, GraduationData graduationDataStatus) throws JsonProcessingException {
		gradResponse.setStudentGradData(new ObjectMapper().writeValueAsString(graduationDataStatus));
		gradResponse.setProgramCompletionDate(graduationDataStatus.getGradStatus().getProgramCompletionDate());
		gradResponse.setGpa(graduationDataStatus.getGradStatus().getGpa());
		gradResponse.setHonoursStanding(graduationDataStatus.getGradStatus().getHonoursStanding());
		gradResponse.setRecalculateGradStatus(graduationDataStatus.getGradStatus().getRecalculateGradStatus());
		return gradResponse;
	}
}
