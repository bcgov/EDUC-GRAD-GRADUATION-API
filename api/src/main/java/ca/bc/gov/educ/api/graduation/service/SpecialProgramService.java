package ca.bc.gov.educ.api.graduation.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
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
	
	@Autowired
    EducGraduationApiConstants educGraduationApiConstants;
	
	public List<GradStudentSpecialProgram> saveAndLogSpecialPrograms(GraduationData graduationDataStatus, String studentID, String accessToken, List<CodeDTO> specialProgram) throws JsonProcessingException {
		List<GradStudentSpecialProgram> projectedSpecialGradResponse = new ArrayList<>();
		//Run Special Program Algorithm
		for(int i=0; i<graduationDataStatus.getSpecialGradStatus().size();i++) {
			CodeDTO specialProgramCode = new CodeDTO();
			SpecialGradAlgorithmGraduationStatus specialPrograms = graduationDataStatus.getSpecialGradStatus().get(i);
			
			GradStudentSpecialProgram gradSpecialProgram = webClient.get().uri(String.format(educGraduationApiConstants.getGetSpecialProgramDetails(),studentID,specialPrograms.getSpecialProgramID())).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(GradStudentSpecialProgram.class).block();
			if(gradSpecialProgram != null) {
				if(specialPrograms.isSpecialGraduated() && gradSpecialProgram.getSpecialProgramCode().compareTo("DD") == 0) {
					graduationDataStatus.setDualDogwood(true);
				}
				gradSpecialProgram.setSpecialProgramCompletionDate(specialPrograms.getSpecialProgramCompletionDate());
				gradSpecialProgram.setStudentSpecialProgramData(new ObjectMapper().writeValueAsString(specialPrograms));
				specialProgramCode.setCode(gradSpecialProgram.getSpecialProgramCode());
				specialProgramCode.setName(gradSpecialProgram.getSpecialProgramName());
				//Save Special Grad Status
				webClient.post().uri(educGraduationApiConstants.getSaveSpecialProgramGradStatus()).headers(h -> h.setBearerAuth(accessToken)).body(BodyInserters.fromValue(gradSpecialProgram)).retrieve().bodyToMono(GradStudentSpecialProgram.class).block();
			}			
			specialProgram.add(specialProgramCode);
			projectedSpecialGradResponse.add(gradSpecialProgram);
		}
		return projectedSpecialGradResponse;
	}
	
	public List<GradStudentSpecialProgram> projectedSpecialPrograms(GraduationData graduationDataStatus, String studentID, String accessToken) throws JsonProcessingException {
		List<GradStudentSpecialProgram> projectedSpecialGradResponse = new ArrayList<>();
		for(int i=0; i<graduationDataStatus.getSpecialGradStatus().size();i++) {
			GradStudentSpecialProgram specialProgramProjectedObj = new GradStudentSpecialProgram();
			SpecialGradAlgorithmGraduationStatus specialPrograms = graduationDataStatus.getSpecialGradStatus().get(i);
			GradStudentSpecialProgram gradSpecialProgram = webClient.get().uri(String.format(educGraduationApiConstants.getGetSpecialProgramDetails(),studentID,specialPrograms.getSpecialProgramID())).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(GradStudentSpecialProgram.class).block();
			if(gradSpecialProgram != null) {
				if(specialPrograms.isSpecialGraduated() && gradSpecialProgram.getSpecialProgramCode().compareTo("DD") == 0) {
					graduationDataStatus.setDualDogwood(true);
				}
				specialProgramProjectedObj.setSpecialProgramCompletionDate(specialPrograms.getSpecialProgramCompletionDate());
				specialProgramProjectedObj.setStudentSpecialProgramData(new ObjectMapper().writeValueAsString(specialPrograms));
				specialProgramProjectedObj.setPen(gradSpecialProgram.getPen());
				specialProgramProjectedObj.setStudentID(gradSpecialProgram.getStudentID());
				specialProgramProjectedObj.setId(gradSpecialProgram.getId());
				specialProgramProjectedObj.setMainProgramCode(gradSpecialProgram.getMainProgramCode());
				specialProgramProjectedObj.setSpecialProgramCode(gradSpecialProgram.getSpecialProgramCode());
				specialProgramProjectedObj.setSpecialProgramName(gradSpecialProgram.getSpecialProgramName());
			}
			projectedSpecialGradResponse.add(specialProgramProjectedObj);
		}
		return projectedSpecialGradResponse;
	}
}
