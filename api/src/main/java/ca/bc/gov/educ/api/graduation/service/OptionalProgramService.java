package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.model.dto.CodeDTO;
import ca.bc.gov.educ.api.graduation.model.dto.GradAlgorithmOptionalStudentProgram;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationData;
import ca.bc.gov.educ.api.graduation.model.dto.StudentOptionalProgram;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import ca.bc.gov.educ.api.graduation.util.JsonTransformer;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OptionalProgramService {

	RESTService restService;
	JsonTransformer jsonTransformer;
	EducGraduationApiConstants educGraduationApiConstants;
	WebClient graduationApiClient;
	WebClient educStudentApiClient;

	@Autowired
	public OptionalProgramService(RESTService restService, JsonTransformer jsonTransformer,
								  EducGraduationApiConstants educGraduationApiConstants,
								  @Qualifier("graduationApiClient") WebClient graduationApiClient,
								  @Qualifier("gradEducStudentApiClient") WebClient educStudentApiClient) {
		this.restService = restService;
		this.jsonTransformer = jsonTransformer;
		this.educGraduationApiConstants = educGraduationApiConstants;
		this.graduationApiClient = graduationApiClient;
		this.educStudentApiClient = educStudentApiClient;
	}
	
	public List<StudentOptionalProgram> saveAndLogOptionalPrograms(GraduationData graduationDataStatus, String studentID, List<CodeDTO> optionalProgram) {
		List<StudentOptionalProgram> projectedOptionalGradResponse = new ArrayList<>();
		//Run Optional Program Algorithm
		for(int i=0; i<graduationDataStatus.getOptionalGradStatus().size();i++) {
			CodeDTO optionalProgramCode = new CodeDTO();
			GradAlgorithmOptionalStudentProgram optionalPrograms = graduationDataStatus.getOptionalGradStatus().get(i);
			
			StudentOptionalProgram gradOptionalProgram = restService
					.get(String.format(educGraduationApiConstants.getGetOptionalProgramDetails(),studentID,optionalPrograms.getOptionalProgramID()),
							StudentOptionalProgram.class, graduationApiClient);
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
				gradOptionalProgram.setStudentOptionalProgramData(jsonTransformer.marshall(optionalPrograms));
				optionalProgramCode.setCode(gradOptionalProgram.getOptionalProgramCode());
				optionalProgramCode.setName(gradOptionalProgram.getOptionalProgramName());
				//Save Optional Grad Status
				restService.post(educGraduationApiConstants.getSaveOptionalProgramGradStatus(), gradOptionalProgram,
						StudentOptionalProgram.class, graduationApiClient);
			}			
			optionalProgram.add(optionalProgramCode);
			projectedOptionalGradResponse.add(gradOptionalProgram);
		}
		return projectedOptionalGradResponse;
	}
	
	public List<StudentOptionalProgram> projectedOptionalPrograms(GraduationData graduationDataStatus, String studentID) {
		List<StudentOptionalProgram> projectedOptionalGradResponse = new ArrayList<>();
		for(int i=0; i<graduationDataStatus.getOptionalGradStatus().size();i++) {
			StudentOptionalProgram optionalProgramProjectedObj = new StudentOptionalProgram();
			GradAlgorithmOptionalStudentProgram optionalPrograms = graduationDataStatus.getOptionalGradStatus().get(i);
			StudentOptionalProgram gradOptionalProgram = restService
					.get(String.format(educGraduationApiConstants.getGetOptionalProgramDetails(),studentID,optionalPrograms.getOptionalProgramID()),
							StudentOptionalProgram.class, graduationApiClient);
			if(gradOptionalProgram != null) {
				if(optionalPrograms.isOptionalGraduated() && gradOptionalProgram.getOptionalProgramCode().compareTo("DD") == 0) {
					graduationDataStatus.setDualDogwood(true);
				}
				optionalProgramProjectedObj.setOptionalProgramCompletionDate(optionalPrograms.getOptionalProgramCompletionDate());
				optionalProgramProjectedObj.setStudentOptionalProgramData(jsonTransformer.marshall(optionalPrograms));
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

	public List<StudentOptionalProgram> getStudentOptionalPrograms(UUID studentID) {
		var response = restService.get(String.format(educGraduationApiConstants.getStudentOptionalPrograms(),
				studentID), List.class, graduationApiClient);
		return jsonTransformer.convertValue(response, new TypeReference<>() {
        });
	}
}
