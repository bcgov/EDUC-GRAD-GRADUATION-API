package ca.bc.gov.educ.api.graduation.process;

import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.model.report.ReportData;
import ca.bc.gov.educ.api.graduation.service.GradAlgorithmService;
import ca.bc.gov.educ.api.graduation.service.GradStatusService;
import ca.bc.gov.educ.api.graduation.service.OptionalProgramService;
import ca.bc.gov.educ.api.graduation.service.ReportService;
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

	@Autowired
	ReportService reportService;

	
	@Override
	public ProcessorData fire() {
		ExceptionMessage exception = processorData.getException();
		long startTime = System.currentTimeMillis();
		logger.info("************* TIME START  ************ "+startTime);
		AlgorithmResponse algorithmResponse = new AlgorithmResponse();
		GraduationStudentRecord gradResponse = processorData.getGradResponse();
		GraduationData graduationDataStatus = gradAlgorithmService.runProjectedAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), processorData.getAccessToken());
		if (graduationDataStatus != null && graduationDataStatus.getException() != null && graduationDataStatus.getException().getExceptionName() != null) {
			logger.info("**** Grad Algorithm Has Errors: ****");
			algorithmResponse.setException(graduationDataStatus.getException());
			processorData.setAlgorithmResponse(algorithmResponse);
			return processorData;
		} else if (exception.getExceptionName() != null) {
			logger.info("**** Grad Algorithm errored out: ****");
			algorithmResponse.setException(exception);
			processorData.setAlgorithmResponse(algorithmResponse);
			return processorData;
		}
		logger.info("**** Grad Algorithm Completed: ****");
		//Code to prepare achievement report
		GraduationStudentRecord graduationStatusResponse = gradStatusService.saveStudentRecordProjectedRun(processorData.getStudentID(), processorData.getBatchId(), processorData.getAccessToken(), exception);
		logger.info("**** graduationStatusResponse "+graduationStatusResponse);
		gradResponse = gradStatusService.processProjectedResults(gradResponse, graduationDataStatus);
		logger.info("gradResponse {}",gradResponse);
		List<StudentOptionalProgram> projectedOptionalGradResponse = optionalProgramService.projectedOptionalPrograms(graduationDataStatus, processorData.getStudentID(), processorData.getAccessToken());
		ReportData data = reportService.prepareAchievementReportData(graduationDataStatus, projectedOptionalGradResponse, getProcessorData().getAccessToken());
		reportService.saveStudentAchivementReportJasper(gradResponse.getPen(), data, processorData.getAccessToken(), gradResponse.getStudentID(), exception, graduationDataStatus.isGraduated());
		if(exception.getExceptionName() != null) {
			algorithmResponse.setException(exception);
			processorData.setAlgorithmResponse(algorithmResponse);
			logger.info("**** Problem Generating TVR: ****");
			return processorData;
		}
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
