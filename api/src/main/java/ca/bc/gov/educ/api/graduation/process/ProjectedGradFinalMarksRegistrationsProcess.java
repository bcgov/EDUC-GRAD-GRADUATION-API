package ca.bc.gov.educ.api.graduation.process;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.bc.gov.educ.api.graduation.model.dto.AlgorithmResponse;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationData;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationStudentRecord;
import ca.bc.gov.educ.api.graduation.model.dto.ProcessorData;
import ca.bc.gov.educ.api.graduation.model.dto.StudentOptionalProgram;
import ca.bc.gov.educ.api.graduation.service.GradAlgorithmService;
import ca.bc.gov.educ.api.graduation.service.GradStatusService;
import ca.bc.gov.educ.api.graduation.service.SpecialProgramService;
import ca.bc.gov.educ.api.graduation.util.GradBusinessRuleException;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Component
@NoArgsConstructor
public class ProjectedGradFinalMarksRegistrationsProcess implements AlgorithmProcess {
	
	private static Logger logger = LoggerFactory.getLogger(ProjectedGradFinalMarksRegistrationsProcess.class);
	
	@Autowired
    private ProcessorData processorData;
    
	@Autowired
	GradStatusService gradStatusService;
	
	@Autowired
	GradAlgorithmService gradAlgorithmService;
	
	@Autowired
	SpecialProgramService specialProgramService;

	
	@Override
	public ProcessorData fire() {
		
		try {
			long startTime = System.currentTimeMillis();
			logger.info("************* TIME START  ************ "+startTime);
			AlgorithmResponse algorithmResponse = new AlgorithmResponse();
			GraduationStudentRecord gradResponse = processorData.getGradResponse();
			GraduationData graduationDataStatus = gradAlgorithmService.runProjectedAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), processorData.getAccessToken());
			logger.info("**** Grad Algorithm Completed: ****");
			gradResponse = gradStatusService.processProjectedResults(gradResponse,graduationDataStatus);
			List<StudentOptionalProgram> projectedSpecialGradResponse = specialProgramService.projectedSpecialPrograms(graduationDataStatus, processorData.getStudentID(), processorData.getAccessToken());
			algorithmResponse.setStudentOptionalProgram(projectedSpecialGradResponse);
			algorithmResponse.setGraduationStudentRecord(gradResponse);
			long endTime = System.currentTimeMillis();
			long diff = (endTime - startTime)/1000;
			logger.info("************* TIME Taken  ************ "+diff+" secs");
			processorData.setAlgorithmResponse(algorithmResponse);
			return processorData;

		}catch(Exception e) {
			throw new GradBusinessRuleException(e.getMessage());
		}
	}

	@Override
    public void setInputData(ProcessorData inputData) {
		processorData = (ProcessorData)inputData;
        logger.info("ProjectedGradFinalMarksRegistraionProcess: ");
    }

}
