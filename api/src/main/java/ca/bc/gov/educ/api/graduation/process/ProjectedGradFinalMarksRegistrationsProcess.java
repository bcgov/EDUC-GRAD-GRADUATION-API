package ca.bc.gov.educ.api.graduation.process;

import java.util.List;

import ca.bc.gov.educ.api.graduation.model.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.bc.gov.educ.api.graduation.service.GradAlgorithmService;
import ca.bc.gov.educ.api.graduation.service.GradStatusService;
import ca.bc.gov.educ.api.graduation.service.OptionalProgramService;
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
	OptionalProgramService optionalProgramService;

	
	@Override
	public ProcessorData fire() {
		ExceptionMessage exception = processorData.getException();
		long startTime = System.currentTimeMillis();
		logger.info("************* TIME START  ************ "+startTime);
		AlgorithmResponse algorithmResponse = new AlgorithmResponse();
		GraduationStudentRecord gradResponse = processorData.getGradResponse();
		GraduationData graduationDataStatus = gradAlgorithmService.runProjectedAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), processorData.getAccessToken());
		if(graduationDataStatus != null && graduationDataStatus.getException() != null && graduationDataStatus.getException().getExceptionName() != null) {
			logger.info("**** Grad Algorithm Has Errors: ****");
			algorithmResponse.setException(graduationDataStatus.getException());
			processorData.setAlgorithmResponse(algorithmResponse);
			return processorData;
		}else if(exception.getExceptionName() != null) {
			logger.info("**** Grad Algorithm errored out: ****");
			algorithmResponse.setException(exception);
			processorData.setAlgorithmResponse(algorithmResponse);
			return processorData;
		}
		logger.info("**** Grad Algorithm Completed: ****");
		//Code to prepare achievement report
		GraduationStudentRecord graduationStatusResponse = gradStatusService.saveStudentRecordProjectedRun(processorData.getStudentID(),processorData.getBatchId(), processorData.getAccessToken(),exception);
		gradResponse = gradStatusService.processProjectedResults(graduationStatusResponse,graduationDataStatus);
		List<StudentOptionalProgram> projectedOptionalGradResponse = optionalProgramService.projectedOptionalPrograms(graduationDataStatus, processorData.getStudentID(), processorData.getAccessToken());
		algorithmResponse.setStudentOptionalProgram(projectedOptionalGradResponse);
		algorithmResponse.setGraduationStudentRecord(gradResponse);
		long endTime = System.currentTimeMillis();
		long diff = (endTime - startTime)/1000;
		logger.info("************* TIME Taken  ************ "+diff+" secs");
		processorData.setAlgorithmResponse(algorithmResponse);
		return processorData;
	}

	@Override
    public void setInputData(ProcessorData inputData) {
		processorData = (ProcessorData)inputData;
        logger.info("ProjectedGradFinalMarksRegistraionProcess: ");
    }

}
