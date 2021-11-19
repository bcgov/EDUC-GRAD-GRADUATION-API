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
public class OptionalProgramService {

	@Autowired
    WebClient webClient;
	
	@Autowired
    EducGraduationApiConstants educGraduationApiConstants;
	
	public List<StudentOptionalProgram> saveAndLogOptionalPrograms(GraduationData graduationDataStatus, String studentID, String accessToken, List<CodeDTO> optionalProgram) {
		List<StudentOptionalProgram> projectedOptionalGradResponse = new ArrayList<>();
		//Run Optional Program Algorithm
		for(int i=0; i<graduationDataStatus.getOptionalGradStatus().size();i++) {
			CodeDTO optionalProgramCode = new CodeDTO();
			GradAlgorithmOptionalStudentProgram optionalPrograms = graduationDataStatus.getOptionalGradStatus().get(i);
			
			StudentOptionalProgram gradOptionalProgram = webClient.get().uri(String.format(educGraduationApiConstants.getGetOptionalProgramDetails(),studentID,optionalPrograms.getOptionalProgramID())).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(StudentOptionalProgram.class).block();
			if(gradOptionalProgram != null) {
				if(optionalPrograms.isOptionalGraduated()) {
					gradOptionalProgram.setGraduated(true);
					if(gradOptionalProgram.getOptionalProgramCode().compareTo("DD") == 0) {
						graduationDataStatus.setDualDogwood(true);
					}
				}
				gradOptionalProgram.setOptionalProgramID(optionalPrograms.getOptionalProgramID());
				gradOptionalProgram.setStudentID(optionalPrograms.getStudentID());
				gradOptionalProgram.setOptionalProgramCompletionDate(optionalPrograms.getOptionalProgramCompletionDate());
				try {
					gradOptionalProgram.setStudentOptionalProgramData(new ObjectMapper().writeValueAsString(optionalPrograms));
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				optionalProgramCode.setCode(gradOptionalProgram.getOptionalProgramCode());
				optionalProgramCode.setName(gradOptionalProgram.getOptionalProgramName());
				//Save Optional Grad Status
				webClient.post().uri(educGraduationApiConstants.getSaveOptionalProgramGradStatus()).headers(h -> h.setBearerAuth(accessToken)).body(BodyInserters.fromValue(gradOptionalProgram)).retrieve().bodyToMono(StudentOptionalProgram.class).block();
			}			
			optionalProgram.add(optionalProgramCode);
			projectedOptionalGradResponse.add(gradOptionalProgram);
		}
		return projectedOptionalGradResponse;
	}
	
	public List<StudentOptionalProgram> projectedOptionalPrograms(GraduationData graduationDataStatus, String studentID, String accessToken) {
		List<StudentOptionalProgram> projectedOptionalGradResponse = new ArrayList<>();
		for(int i=0; i<graduationDataStatus.getOptionalGradStatus().size();i++) {
			StudentOptionalProgram optionalProgramProjectedObj = new StudentOptionalProgram();
			GradAlgorithmOptionalStudentProgram optionalPrograms = graduationDataStatus.getOptionalGradStatus().get(i);
			StudentOptionalProgram gradOptionalProgram = webClient.get().uri(String.format(educGraduationApiConstants.getGetOptionalProgramDetails(),studentID,optionalPrograms.getOptionalProgramID())).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(StudentOptionalProgram.class).block();
			if(gradOptionalProgram != null) {
				if(optionalPrograms.isOptionalGraduated() && gradOptionalProgram.getOptionalProgramCode().compareTo("DD") == 0) {
					graduationDataStatus.setDualDogwood(true);
				}
				optionalProgramProjectedObj.setOptionalProgramCompletionDate(optionalPrograms.getOptionalProgramCompletionDate());
				try {
					optionalProgramProjectedObj.setStudentOptionalProgramData(new ObjectMapper().writeValueAsString(optionalPrograms));
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
				optionalProgramProjectedObj.setOptionalProgramID(gradOptionalProgram.getOptionalProgramID());
				optionalProgramProjectedObj.setStudentID(gradOptionalProgram.getStudentID());
				optionalProgramProjectedObj.setId(gradOptionalProgram.getId());
				optionalProgramProjectedObj.setProgramCode(gradOptionalProgram.getProgramCode());
				optionalProgramProjectedObj.setOptionalProgramCode(gradOptionalProgram.getOptionalProgramCode());
				optionalProgramProjectedObj.setOptionalProgramName(gradOptionalProgram.getOptionalProgramName());
			}
			projectedOptionalGradResponse.add(optionalProgramProjectedObj);
		}
		return projectedOptionalGradResponse;
	}
}
