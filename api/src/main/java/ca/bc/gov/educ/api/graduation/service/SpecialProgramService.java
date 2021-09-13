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
import ca.bc.gov.educ.api.graduation.model.dto.GradAlgorithmOptionalStudentProgram;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationData;
import ca.bc.gov.educ.api.graduation.model.dto.StudentOptionalProgram;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;

@Service
public class SpecialProgramService {

	@Autowired
    WebClient webClient;
	
	@Autowired
    EducGraduationApiConstants educGraduationApiConstants;
	
	public List<StudentOptionalProgram> saveAndLogSpecialPrograms(GraduationData graduationDataStatus, String studentID, String accessToken, List<CodeDTO> specialProgram) {
		List<StudentOptionalProgram> projectedSpecialGradResponse = new ArrayList<>();
		//Run Special Program Algorithm
		for(int i=0; i<graduationDataStatus.getSpecialGradStatus().size();i++) {
			CodeDTO specialProgramCode = new CodeDTO();
			GradAlgorithmOptionalStudentProgram specialPrograms = graduationDataStatus.getSpecialGradStatus().get(i);
			
			StudentOptionalProgram gradSpecialProgram = webClient.get().uri(String.format(educGraduationApiConstants.getGetSpecialProgramDetails(),studentID,specialPrograms.getOptionalProgramID())).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(StudentOptionalProgram.class).block();
			if(gradSpecialProgram != null) {
				if(specialPrograms.isSpecialGraduated()) {
					gradSpecialProgram.setGraduated(true);
					if(gradSpecialProgram.getSpecialProgramCode().compareTo("DD") == 0) {
						graduationDataStatus.setDualDogwood(true);
					}
				}
				gradSpecialProgram.setOptionalProgramID(specialPrograms.getOptionalProgramID());
				gradSpecialProgram.setStudentID(specialPrograms.getStudentID());
				gradSpecialProgram.setSpecialProgramCompletionDate(specialPrograms.getOptionalProgramCompletionDate());
				try {
					gradSpecialProgram.setStudentSpecialProgramData(new ObjectMapper().writeValueAsString(specialPrograms));
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				specialProgramCode.setCode(gradSpecialProgram.getSpecialProgramCode());
				specialProgramCode.setName(gradSpecialProgram.getSpecialProgramName());
				//Save Special Grad Status
				webClient.post().uri(educGraduationApiConstants.getSaveSpecialProgramGradStatus()).headers(h -> h.setBearerAuth(accessToken)).body(BodyInserters.fromValue(gradSpecialProgram)).retrieve().bodyToMono(StudentOptionalProgram.class).block();
			}			
			specialProgram.add(specialProgramCode);
			projectedSpecialGradResponse.add(gradSpecialProgram);
		}
		return projectedSpecialGradResponse;
	}
	
	public List<StudentOptionalProgram> projectedSpecialPrograms(GraduationData graduationDataStatus, String studentID, String accessToken) throws JsonProcessingException {
		List<StudentOptionalProgram> projectedSpecialGradResponse = new ArrayList<>();
		for(int i=0; i<graduationDataStatus.getSpecialGradStatus().size();i++) {
			StudentOptionalProgram specialProgramProjectedObj = new StudentOptionalProgram();
			GradAlgorithmOptionalStudentProgram specialPrograms = graduationDataStatus.getSpecialGradStatus().get(i);
			StudentOptionalProgram gradSpecialProgram = webClient.get().uri(String.format(educGraduationApiConstants.getGetSpecialProgramDetails(),studentID,specialPrograms.getOptionalProgramID())).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(StudentOptionalProgram.class).block();
			if(gradSpecialProgram != null) {
				if(specialPrograms.isSpecialGraduated() && gradSpecialProgram.getSpecialProgramCode().compareTo("DD") == 0) {
					graduationDataStatus.setDualDogwood(true);
				}
				specialProgramProjectedObj.setSpecialProgramCompletionDate(specialPrograms.getOptionalProgramCompletionDate());
				specialProgramProjectedObj.setStudentSpecialProgramData(new ObjectMapper().writeValueAsString(specialPrograms));
				specialProgramProjectedObj.setOptionalProgramID(gradSpecialProgram.getOptionalProgramID());
				specialProgramProjectedObj.setStudentID(gradSpecialProgram.getStudentID());
				specialProgramProjectedObj.setId(gradSpecialProgram.getId());
				specialProgramProjectedObj.setProgramCode(gradSpecialProgram.getProgramCode());
				specialProgramProjectedObj.setSpecialProgramCode(gradSpecialProgram.getSpecialProgramCode());
				specialProgramProjectedObj.setSpecialProgramName(gradSpecialProgram.getSpecialProgramName());
			}
			projectedSpecialGradResponse.add(specialProgramProjectedObj);
		}
		return projectedSpecialGradResponse;
	}
}
