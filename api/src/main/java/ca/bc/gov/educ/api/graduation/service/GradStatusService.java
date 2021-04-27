package ca.bc.gov.educ.api.graduation.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.bc.gov.educ.api.graduation.model.dto.GraduationData;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationStatus;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import reactor.core.publisher.Mono;

@Service
public class GradStatusService {

	@Autowired
    WebClient webClient;
	
	@Value(EducGraduationApiConstants.ENDPOINT_GRAD_STATUS_READ_URL)
    private String readGradStatusForStudent;
	
	@Value(EducGraduationApiConstants.ENDPOINT_GRAD_STATUS_UPDATE_URL)
	private String updateGradStatusForStudent;
	
	public GraduationStatus getGradStatus(String studentID, String accessToken) {
		GraduationStatus gradResponse = webClient.get().uri(String.format(readGradStatusForStudent,studentID)).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(GraduationStatus.class).block();
		return gradResponse;
	}
	
	public GraduationStatus prepareGraduationStatusObj(GraduationData graduationDataStatus) {
		GraduationStatus obj = new GraduationStatus();
		BeanUtils.copyProperties(graduationDataStatus.getGradStatus(), obj);
		try {
			obj.setStudentGradData(new ObjectMapper().writeValueAsString(graduationDataStatus));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return obj;
	}
	
	public GraduationStatus saveStudentGradStatus(String studentID,String accessToken, GraduationStatus toBeSaved) {
		GraduationStatus graduationStatusResponse = webClient.post().uri(String.format(updateGradStatusForStudent,studentID)).headers(h -> h.setBearerAuth(accessToken)).body(Mono.just(toBeSaved), GraduationStatus.class).retrieve().bodyToMono(GraduationStatus.class).block();
		return graduationStatusResponse;
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
