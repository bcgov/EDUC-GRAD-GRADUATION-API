package ca.bc.gov.educ.api.graduation.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.bc.gov.educ.api.graduation.model.dto.CodeDTO;
import ca.bc.gov.educ.api.graduation.model.dto.GradStudentSpecialProgram;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationData;
import ca.bc.gov.educ.api.graduation.model.dto.SpecialGradAlgorithmGraduationStatus;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import reactor.core.publisher.Mono;

@Service
public class SpecialProgramService {

	@Autowired
    WebClient webClient;
	
	@Value(EducGraduationApiConstants.ENDPOINT_SPECIAL_PROGRAM_DETAILS_URL)
    private String specialProgramDetails;
	
	@Value(EducGraduationApiConstants.ENDPOINT_SPECIAL_GRAD_STATUS_SAVE)
    private String saveSpecialGradStatusForStudent;
	
	public List<GradStudentSpecialProgram> saveAndLogSpecialPrograms(GraduationData graduationDataStatus, String studentID, String accessToken, List<CodeDTO> specialProgram) throws JsonProcessingException {
		List<GradStudentSpecialProgram> projectedSpecialGradResponse = new ArrayList<GradStudentSpecialProgram>();
		//Run Special Program Algorithm
		for(int i=0; i<graduationDataStatus.getSpecialGradStatus().size();i++) {
			CodeDTO specialProgramCode = new CodeDTO();
			SpecialGradAlgorithmGraduationStatus specialPrograms = graduationDataStatus.getSpecialGradStatus().get(i);
			GradStudentSpecialProgram gradSpecialProgram = webClient.get().uri(String.format(specialProgramDetails,studentID,specialPrograms.getSpecialProgramID())).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(GradStudentSpecialProgram.class).block();
			gradSpecialProgram.setSpecialProgramCompletionDate(specialPrograms.getSpecialProgramCompletionDate());
			gradSpecialProgram.setStudentSpecialProgramData(new ObjectMapper().writeValueAsString(specialPrograms));
			
			//Save Special Grad Status
			webClient.post().uri(saveSpecialGradStatusForStudent).headers(h -> h.setBearerAuth(accessToken)).body(Mono.just(gradSpecialProgram), GradStudentSpecialProgram.class).retrieve().bodyToMono(GradStudentSpecialProgram.class).block();
			specialProgramCode.setCode(gradSpecialProgram.getSpecialProgramCode());
			specialProgramCode.setName(gradSpecialProgram.getSpecialProgramName());
			specialProgram.add(specialProgramCode);
			projectedSpecialGradResponse.add(gradSpecialProgram);
		}
		return projectedSpecialGradResponse;
	}
	
	public List<GradStudentSpecialProgram> projectedSpecialPrograms(GraduationData graduationDataStatus, String studentID, String accessToken) throws JsonProcessingException {
		List<GradStudentSpecialProgram> projectedSpecialGradResponse = new ArrayList<GradStudentSpecialProgram>();
		for(int i=0; i<graduationDataStatus.getSpecialGradStatus().size();i++) {
			GradStudentSpecialProgram specialProgramProjectedObj = new GradStudentSpecialProgram();
			SpecialGradAlgorithmGraduationStatus specialPrograms = graduationDataStatus.getSpecialGradStatus().get(i);
			GradStudentSpecialProgram gradSpecialProgram = webClient.get().uri(String.format(specialProgramDetails,studentID,specialPrograms.getSpecialProgramID())).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(GradStudentSpecialProgram.class).block();
			specialProgramProjectedObj.setSpecialProgramCompletionDate(specialPrograms.getSpecialProgramCompletionDate());
			specialProgramProjectedObj.setStudentSpecialProgramData(new ObjectMapper().writeValueAsString(specialPrograms));
			specialProgramProjectedObj.setPen(gradSpecialProgram.getPen());
			specialProgramProjectedObj.setMainProgramCode(gradSpecialProgram.getMainProgramCode());
			specialProgramProjectedObj.setSpecialProgramCode(gradSpecialProgram.getSpecialProgramCode());
			specialProgramProjectedObj.setSpecialProgramName(gradSpecialProgram.getSpecialProgramName());
			projectedSpecialGradResponse.add(specialProgramProjectedObj);
		}
		return projectedSpecialGradResponse;
	}
}
