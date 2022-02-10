package ca.bc.gov.educ.api.graduation.process;

import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.service.GradAlgorithmService;
import ca.bc.gov.educ.api.graduation.service.GradStatusService;
import ca.bc.gov.educ.api.graduation.service.OptionalProgramService;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@NoArgsConstructor
public class ProjectedGradFinalMarksProcess implements AlgorithmProcess {
	
	private static Logger logger = LoggerFactory.getLogger(ProjectedGradFinalMarksProcess.class);
	
	@Autowired
    private ProcessorData processorData;
    
	@Autowired
	GradStatusService gradStatusService;
	
	@Autowired
	GradAlgorithmService gradAlgorithmService;
	
	@Autowired
	OptionalProgramService optionalProgramService;

	@Autowired
	AlgorithmSupport algorithmSupport;
	
	@Override
	public ProcessorData fire() {
		ExceptionMessage exception = new ExceptionMessage();

		long startTime = System.currentTimeMillis();
		logger.info("************* TIME START  ************ {}",startTime);
		AlgorithmResponse algorithmResponse = new AlgorithmResponse();
		GraduationStudentRecord gradResponse = processorData.getGradResponse();
		GraduationData graduationDataStatus = gradAlgorithmService.runProjectedAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), processorData.getAccessToken());
		if(algorithmSupport.checkForErrors(graduationDataStatus,exception,algorithmResponse,processorData)){
			return processorData;
		}
		logger.info("**** Grad Algorithm Completed: ****");
		gradResponse = gradStatusService.processProjectedResults(gradResponse,graduationDataStatus);
		List<StudentOptionalProgram> projectedOptionalGradResponse = optionalProgramService.projectedOptionalPrograms(graduationDataStatus, processorData.getStudentID(), processorData.getAccessToken());
		algorithmResponse.setStudentOptionalProgram(projectedOptionalGradResponse);
		algorithmResponse.setGraduationStudentRecord(gradResponse);

		long endTime = System.currentTimeMillis();
		long diff = (endTime - startTime)/1000;
		logger.info("************* TIME Taken  ************ {} secs",diff);
		processorData.setAlgorithmResponse(algorithmResponse);
		return processorData;
	}

	@Override
    public void setInputData(ProcessorData inputData) {
		processorData = inputData;
        logger.info("ProjectedGradFinalMarksProcess: ");
    }

}
